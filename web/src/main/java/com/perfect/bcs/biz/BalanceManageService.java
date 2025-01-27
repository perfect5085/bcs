package com.perfect.bcs.biz;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.perfect.bcs.biz.common.BizException;
import com.perfect.bcs.biz.type.AccountStatus;
import com.perfect.bcs.biz.type.TransactionStatus;
import com.perfect.bcs.dal.domain.AccountInfoDO;
import com.perfect.bcs.dal.domain.AccountTransactionDO;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @author liangbo 梁波
 * @date 2025-01-22 21:45
 */
@Service
@Slf4j
public class BalanceManageService {

    @Autowired
    private AccountInfoService          accountInfoService;
    @Autowired
    private AccountTransactionService   accountTransactionService;
    @Autowired
    private AccountBalanceChangeService accountBalanceChangeService;
    @Autowired
    private BalanceAtomicService        balanceAtomicService;
    @Autowired
    private CacheService                cacheService;
    @Autowired
    private RedissonClient              redissonClient;

    /**
     * 账户信息缓存Key前缀
     */
    private static final String CACHE_KEY_ACCOUNT_INFO_PREFIX = "acc_info_get_";

    /**
     * 交易缓存的前缀
     */
    private static final String CACHE_KEY_ACCOUNT_BALANCE_TRANSACTION_PREFIX = "acc_blc_t_v";

    /**
     * 交易分布锁的前缀
     */
    private static final String LOCK_KEY_ACCOUNT_BALANCE_TRANSACTION_PREFIX = "acc_blc_t_l";

    /**
     * 定时任务的锁
     */
    private static final String LOCK_KEY_CHECK_TRANSACTION_STATUS = "che_tra_st";

    private static boolean checkTaskFlag = false;

    /**
     * 定时检查交易是否超时，如果超时重置为失败
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    @PostConstruct
    public void resetTimeoutTransactionStatus() {

        if (checkTaskFlag) {
            log.info("定时检查交易是否超时任务结束（有任务在执行中，未结束）");
            return;
        }

        checkTaskFlag = true;
        log.info("定时检查交易是否超时任务开始");
        // 防止多个任务同时执行
        RLock lock = null;
        try {
            lock = redissonClient.getLock(LOCK_KEY_CHECK_TRANSACTION_STATUS);
            // 尝试在10秒之内获取锁， 手动指定过期时间为 600 秒
            if (lock.tryLock(10, 600, TimeUnit.SECONDS)) {
                int pageIndex = 1;
                int pageSize = 1000;

                Date now = new Date();
                // 向前推5分钟。
                Date endDate = DateUtil.offsetMinute(now, -5);
                // 向前推65分钟。
                Date startDate = DateUtil.offsetMinute(now, -65);

                while (true) {
                    Page<AccountTransactionDO> page =
                            accountTransactionService.getPageOfStarted(pageIndex, pageSize,
                                                                       startDate, endDate);
                    pageIndex++;
                    if (CollUtil.isEmpty(page.getRecords())) {
                        break;
                    }

                    // 数量一般很少
                    for (AccountTransactionDO record : page.getRecords()) {
                        try {
                            // 如果交易超过10分钟，也属于交易失败
                            if (DateUtil.between(record.getTransactionStartTime(), new Date(), DateUnit.SECOND) > 60) {
                                accountTransactionService.end(record.getTransactionId(), TransactionStatus.FAILED);
                            }
                        } catch (Throwable e) {
                            log.error("重置交易失败异常", e);
                        }
                    }
                }

            }
        } catch (Throwable e) {
            log.error("定时检查交易状态异常！", e);
        } finally {
            log.info("定时检查交易是否超时任务结束");
            checkTaskFlag = false;
            if (null != lock && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }

    /**
     * 获取账户余额
     *
     * @param accountNo 账户
     * @return
     */
    public BigDecimal getBalance(String accountNo) {

        AccountInfoDO accountInfoDO = getAccountInfo(accountNo);
        if (null == accountInfoDO) {
            throw new BizException(1000, accountNo);
        }

        return Optional.ofNullable(accountInfoDO)
                       .map(AccountInfoDO::getAccountBalance)
                       .orElse(null);
    }

    /**
     * 修改账户的余额【取款/存钱】
     *
     * @param transactionId 事务ID
     * @param accountNo     账户
     * @param amount        修改金额： 正数【取款】，负数【存钱】
     */
    public void changeBalance(String transactionId, String accountNo, BigDecimal amount) {

        checkTransaction(transactionId);
        boolean flag = accountTransactionService.create(transactionId, "", accountNo, amount);
        if (!flag) {
            throw new BizException(1004, transactionId);
        }

        AccountInfoDO accountInfoDO = getAccountInfo(accountNo);
        if (null == accountInfoDO) {
            throw new BizException(1000, accountNo);
        }

        if (!AccountStatus.ACTIVE.equals(accountInfoDO.getAccountStatus())) {
            throw new BizException(1001, accountNo);
        }

        String lockKey = getLockKeyAccountBalanceTransaction(transactionId);
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试在10秒之内获取锁， 手动指定过期时间为 30 秒
            if (lock.tryLock(10, 30, TimeUnit.SECONDS)) {
                boolean isSuccess = false;
                // 最多重试3次
                int retryIndex = 3;
                while (retryIndex > 0) {
                    retryIndex--;
                    try {
                        // 扣款（取款）操作
                        if (NumberUtil.isLess(amount, BigDecimal.ZERO)) {
                            withdraw(accountInfoDO, transactionId, amount);
                        } else {
                            // 存钱操作
                            deposite(accountInfoDO, transactionId, amount);
                        }

                        isSuccess = true;
                        break;
                    } catch (Throwable e) {
                        log.error("存钱/取钱异常", e);
                        // 很有可能数据不是最新的，更新一下最新的数据
                        accountInfoDO = refreshAccountInfo(accountNo);
                        Thread.sleep(RandomUtil.randomInt(500, 3000));
                    }
                }
                if (!isSuccess) {
                    accountTransactionService.end(transactionId, TransactionStatus.FAILED);
                    throw new BizException(1002);
                } else {
                    // 更新缓存余额
                    refreshAccountInfo(accountNo);
                }
            }
        } catch (Throwable e) {
            log.error("修改账户余额异常！", e);
            throw new BizException(1002);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }

    /**
     * 账户之间转账
     *
     * @param transactionId   事务ID
     * @param sourceAccountNo 来源账户
     * @param targetAccountNo 目标账户
     * @param amount          从来源账户转给目标账户的金额：不能为负数
     */
    public void transferBalance(String transactionId, String sourceAccountNo,
                                String targetAccountNo, BigDecimal amount) {

        checkTransaction(transactionId);
        boolean flag = accountTransactionService.create(transactionId, sourceAccountNo, targetAccountNo, amount);
        if (!flag) {
            throw new BizException(1004, transactionId);
        }
        if (StrUtil.equals(sourceAccountNo, targetAccountNo)) {
            throw new BizException(1005, sourceAccountNo);
        }

        AccountInfoDO sourceAccountInfoDO = getAccountInfo(sourceAccountNo);
        AccountInfoDO targetAccountInfoDO = getAccountInfo(targetAccountNo);
        if (null == sourceAccountInfoDO) {
            throw new BizException(1000, sourceAccountNo);
        }
        if (null == targetAccountInfoDO) {
            throw new BizException(1000, targetAccountNo);
        }

        if (!AccountStatus.ACTIVE.equals(sourceAccountInfoDO.getAccountStatus())) {
            throw new BizException(1001, sourceAccountNo);
        }
        if (!AccountStatus.ACTIVE.equals(targetAccountInfoDO.getAccountStatus())) {
            throw new BizException(1001, targetAccountNo);
        }

        String lockKey = getLockKeyAccountBalanceTransaction(transactionId);
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 尝试在10秒之内获取锁， 手动指定过期时间为 30 秒
            if (lock.tryLock(10, 30, TimeUnit.SECONDS)) {

                // 最多重试3次
                int retryIndex = 3;
                boolean isSuccess = false;
                while (retryIndex > 0) {
                    retryIndex--;
                    try {

                        balanceAtomicService.transferBalance(sourceAccountInfoDO, targetAccountInfoDO,
                                                             transactionId,
                                                             amount);
                        isSuccess = true;
                        break;

                    } catch (Throwable e) {
                        log.error("转账异常", e);
                        // 很有可能数据不是最新的，更新一下最新的数据
                        sourceAccountInfoDO = refreshAccountInfo(sourceAccountNo);
                        targetAccountInfoDO = refreshAccountInfo(targetAccountNo);
                        Thread.sleep(RandomUtil.randomInt(500, 3000));
                    }
                }

                if (!isSuccess) {
                    accountTransactionService.end(transactionId, TransactionStatus.FAILED);
                    throw new BizException(1002);
                } else {
                    // 更新缓存余额
                    refreshAccountInfo(sourceAccountNo);
                    refreshAccountInfo(targetAccountNo);
                }

            }
        } catch (Throwable e) {
            log.error("转账异常！", e);
            throw new BizException(1002);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 存钱
     *
     * @param accountInfoDO 账户
     * @param transactionId 事务ID
     * @param amount        金额
     */
    private void deposite(AccountInfoDO accountInfoDO, String transactionId, BigDecimal amount) {

        balanceAtomicService.changeBalance(accountInfoDO, transactionId, amount);
    }

    /**
     * 取钱
     *
     * @param accountInfoDO 账户
     * @param transactionId 事务ID
     * @param amount        金额
     */
    private void withdraw(AccountInfoDO accountInfoDO, String transactionId, BigDecimal amount) {
        BigDecimal accountBalance = accountInfoDO.getAccountBalance();

        BigDecimal newBalance = NumberUtil.add(accountBalance, amount);
        // 取款的数额大于账户的余额
        if (NumberUtil.isLess(newBalance, BigDecimal.ZERO)) {
            // 主动刷新一下余额，防止缓存数据不及时
            accountInfoDO = refreshAccountInfo(accountInfoDO.getAccountNo());
            accountBalance = accountInfoDO.getAccountBalance();
            newBalance = NumberUtil.add(accountBalance, amount);
        }

        // 取款的数额大于账户的余额，抛出异常
        if (NumberUtil.isLess(newBalance, BigDecimal.ZERO)) {

            accountTransactionService.end(transactionId, TransactionStatus.FAILED);

            throw new BizException(1003, accountInfoDO.getAccountNo());
        } else {
            balanceAtomicService.changeBalance(accountInfoDO, transactionId, amount);
        }
    }

    /**
     * 检测当前事务是否正在执行
     *
     * @param transactionId
     */
    private void checkTransaction(String transactionId) {
        String key = getCacheKeyAccountBalanceTransaction(transactionId);
        String transactionFlag = cacheService.get(key);
        if (StrUtil.isNotBlank(transactionFlag)) {
            throw new BizException(1004, transactionId);
        }

        // 缓存交易60秒，防止短时间重复提交
        cacheService.set(key, "1", 60);
    }

    /**
     * 获取账户信息
     *
     * @param accountNo 账户
     * @return
     */
    private AccountInfoDO getAccountInfo(String accountNo) {
        String cacheKey = getCacheKeyAccountInfo(accountNo);
        String accountInfoStr = cacheService.get(cacheKey);

        if (StrUtil.isBlank(accountInfoStr)) {
            return refreshAccountInfo(accountNo);
        }
        return JSONUtil.toBean(accountInfoStr, AccountInfoDO.class);
    }

    /**
     * 强制刷新缓存
     *
     * @param accountNo 账户
     * @return
     */
    private AccountInfoDO refreshAccountInfo(String accountNo) {

        AccountInfoDO accountInfoDO = accountInfoService.getByAccountNo(accountNo);
        if (null == accountInfoDO) {
            throw new BizException(1000, accountNo);
        }

        String cacheKey = getCacheKeyAccountInfo(accountNo);
        String accountInfoStr = JSONUtil.toJsonStr(accountInfoDO);
        // 更新缓存
        cacheService.set(cacheKey, accountInfoStr, 180);

        return accountInfoDO;
    }

    private String getCacheKeyAccountInfo(String accountNo) {
        return CACHE_KEY_ACCOUNT_INFO_PREFIX + accountNo;
    }

    private String getLockKeyAccountBalanceTransaction(String transactionId) {
        return LOCK_KEY_ACCOUNT_BALANCE_TRANSACTION_PREFIX + transactionId;
    }

    private String getCacheKeyAccountBalanceTransaction(String transactionId) {
        return CACHE_KEY_ACCOUNT_BALANCE_TRANSACTION_PREFIX + transactionId;
    }

}
