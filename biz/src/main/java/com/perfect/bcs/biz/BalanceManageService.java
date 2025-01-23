package com.perfect.bcs.biz;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.perfect.bcs.biz.type.AccountStatus;
import com.perfect.bcs.biz.type.TransactionStatus;
import com.perfect.bcs.dal.domain.AccountInfoDO;
import com.shuhong.common.shared.api.BizException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public BigDecimal getBalance(String accountNo) {

        AccountInfoDO accountInfoDO = getAccountInfo(accountNo);
        if (null == accountInfoDO) {
            throw new BizException(1000, accountNo);
        }

        return Optional.ofNullable(accountInfoDO)
                       .map(AccountInfoDO::getAccountBalance)
                       .orElse(null);
    }

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

                // 最多重试3次
                int retryIndex = 3;
                boolean isSuccess = false;
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
                        log.error("存钱异常", e);
                        Thread.sleep(1000);
                    }
                }
                if (!isSuccess) {
                    throw new BizException(1002);
                }
            }
        } catch (Throwable e) {
            log.error("修改账户余额异常！", e);
            throw new BizException(1002);
        } finally {
            lock.unlock();
        }

    }

    public void transferBalance(String transactionId, String sourceAccountNo,
                                String targetAccountNo, BigDecimal amount) {

        checkTransaction(transactionId);
        boolean flag = accountTransactionService.create(transactionId, sourceAccountNo, targetAccountNo, amount);
        if (!flag) {
            throw new BizException(1004, transactionId);
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

                        innerTransferBalance(sourceAccountInfoDO, targetAccountInfoDO, transactionId, amount);

                        isSuccess = true;
                        break;
                    } catch (Throwable e) {
                        log.error("转账异常", e);
                        Thread.sleep(1000);
                    }
                }
                if (!isSuccess) {
                    throw new BizException(1002);
                }
            }
        } catch (Throwable e) {
            log.error("转账异常！", e);
            throw new BizException(1002);
        } finally {
            lock.unlock();
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    private void innerTransferBalance(AccountInfoDO sourceAccountInfoDO, AccountInfoDO targetAccountInfoDO,
                                      String transactionId, BigDecimal amount) {
        String newDataVersion = IdUtil.fastSimpleUUID();

        BigDecimal sourceBalance = NumberUtil.sub(sourceAccountInfoDO.getAccountBalance(), amount);
        boolean sourceUpdateFlag = accountInfoService.updateBalance(sourceAccountInfoDO.getAccountNo(), sourceBalance,
                                                                    sourceAccountInfoDO.getDataVersion(),
                                                                    newDataVersion);

        BigDecimal targetBalance = NumberUtil.add(targetAccountInfoDO.getAccountBalance(), amount);
        boolean targetUpdateFlag = accountInfoService.updateBalance(targetAccountInfoDO.getAccountNo(), targetBalance,
                                                                    targetAccountInfoDO.getDataVersion(),
                                                                    newDataVersion);
        if (!sourceUpdateFlag || !targetUpdateFlag) {
            throw new RuntimeException("无法更新账户余额，很可能未获取到最新版本的数据");
        }

        boolean sourceChangeFlag = accountBalanceChangeService.create(transactionId, sourceAccountInfoDO.getAccountNo(),
                                                                      NumberUtil.mul(amount, new BigDecimal("-1")));
        boolean targetChangeFlag = accountBalanceChangeService.create(transactionId, targetAccountInfoDO.getAccountNo(),
                                                                      amount);
        if (!sourceChangeFlag || !targetChangeFlag) {
            throw new RuntimeException("无法更新账户余额变动记录，很可能数据库有问题");
        }

        boolean transactionFlag = accountTransactionService.end(transactionId, TransactionStatus.SUCCESS);
        if (!transactionFlag) {
            throw new RuntimeException("无法更新交易记录，很可能数据库有问题");
        }

    }

    private void deposite(AccountInfoDO accountInfoDO, String transactionId, BigDecimal amount) {
        innerChangeBalance(accountInfoDO, transactionId, amount);
    }

    private void withdraw(AccountInfoDO accountInfoDO, String transactionId, BigDecimal amount) {
        BigDecimal accountBalance = accountInfoDO.getAccountBalance();

        BigDecimal newBalance = NumberUtil.add(accountBalance, amount);
        // 取款的数额大于账户的余额
        if (NumberUtil.isLess(newBalance, BigDecimal.ZERO)) {

            // 主动刷新一下余额，防止缓存数据不及时
            accountInfoDO = refreshAccountInfo(accountInfoDO.getAccountNo());
            accountBalance = accountInfoDO.getAccountBalance();
            newBalance = NumberUtil.add(accountBalance, amount);

            // 取款的数额大于账户的余额，抛出异常
            if (NumberUtil.isLess(newBalance, BigDecimal.ZERO)) {

                accountTransactionService.end(transactionId, TransactionStatus.FAILED);

                throw new BizException(1003, accountInfoDO.getAccountNo());
            } else {
                innerChangeBalance(accountInfoDO, transactionId, amount);
            }

        }
    }

    @Transactional(rollbackFor = Throwable.class)
    private void innerChangeBalance(AccountInfoDO accountInfoDO, String transactionId, BigDecimal amount) {

        BigDecimal balance = NumberUtil.add(accountInfoDO.getAccountBalance(), amount);
        boolean updateFlag = accountInfoService.updateBalance(accountInfoDO.getAccountNo(), balance,
                                                              accountInfoDO.getDataVersion(), IdUtil.fastSimpleUUID());
        if (!updateFlag) {
            throw new RuntimeException("无法更新账户余额，很可能未获取到最新版本的数据");
        }

        boolean changeFlag = accountBalanceChangeService.create(transactionId, accountInfoDO.getAccountNo(), amount);
        if (!changeFlag) {
            throw new RuntimeException("无法更新账户余额变动记录，很可能数据库有问题");
        }

        boolean transactionFlag = accountTransactionService.end(transactionId, TransactionStatus.SUCCESS);
        if (!transactionFlag) {
            throw new RuntimeException("无法更新交易记录，很可能数据库有问题");
        }

    }

    private void checkTransaction(String transactionId) {
        String key = getCacheKeyAccountBalanceTransaction(transactionId);
        String transactionFlag = cacheService.get(key);
        if (StrUtil.isNotBlank(transactionFlag)) {
            throw new BizException(1004, transactionId);
        }

        // 缓存交易60秒，防止短时间重复提交
        cacheService.set(key, "1", 60);
    }

    private AccountInfoDO getAccountInfo(String accountNo) {
        String accountInfoStr = cacheService.get(accountNo);

        if (StrUtil.isBlank(accountInfoStr)) {
            return refreshAccountInfo(accountNo);
        }
        return JSONUtil.toBean(accountInfoStr, AccountInfoDO.class);
    }

    private AccountInfoDO refreshAccountInfo(String accountNo) {

        AccountInfoDO accountInfoDO = accountInfoService.getByAccountNo(accountNo);
        if (null == accountInfoDO) {
            log.warn("账号 {} 不存在，请进一步确认账号是否正确！", accountNo);
            return null;
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
