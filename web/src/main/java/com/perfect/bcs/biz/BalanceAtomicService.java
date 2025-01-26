package com.perfect.bcs.biz;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.perfect.bcs.biz.common.BizException;
import com.perfect.bcs.biz.type.TransactionStatus;
import com.perfect.bcs.dal.domain.AccountInfoDO;
import com.perfect.bcs.dal.domain.AccountTransactionDO;
import java.math.BigDecimal;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author liangbo 梁波
 * @date 2025-01-22 21:45
 */
@Service
@Slf4j
public class BalanceAtomicService {

    @Autowired
    private AccountInfoService          accountInfoService;
    @Autowired
    private AccountTransactionService   accountTransactionService;
    @Autowired
    private AccountBalanceChangeService accountBalanceChangeService;

    /**
     * 两个账户之间转账【原子化操作，要么全部成功，要么全部失败（回滚数据）】
     *
     * @param sourceAccountInfoDO
     * @param targetAccountInfoDO
     * @param transactionId
     * @param amount
     */
    @Transactional(propagation = Propagation.REQUIRED,
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Throwable.class)
    public void transferBalance(AccountInfoDO sourceAccountInfoDO, AccountInfoDO targetAccountInfoDO,
                                String transactionId, BigDecimal amount) {
        String newDataVersion = IdUtil.fastSimpleUUID();

        BigDecimal sourceBalance = NumberUtil.sub(sourceAccountInfoDO.getAccountBalance(), amount);
        boolean sourceUpdateFlag = accountInfoService.updateBalance(sourceAccountInfoDO.getAccountNo(),
                                                                    sourceBalance,
                                                                    sourceAccountInfoDO.getDataVersion(),
                                                                    newDataVersion);

        BigDecimal targetBalance = NumberUtil.add(targetAccountInfoDO.getAccountBalance(), amount);
        boolean targetUpdateFlag = accountInfoService.updateBalance(targetAccountInfoDO.getAccountNo(),
                                                                    targetBalance,
                                                                    targetAccountInfoDO.getDataVersion(),
                                                                    newDataVersion);
        if (!sourceUpdateFlag || !targetUpdateFlag) {
            throw new RuntimeException("无法更新账户余额，很可能未获取到最新版本的数据");
        }

        boolean sourceChangeFlag = accountBalanceChangeService.create(transactionId,
                                                                      sourceAccountInfoDO.getAccountNo(),
                                                                      NumberUtil.mul(amount, new BigDecimal("-1")));
        boolean targetChangeFlag = accountBalanceChangeService.create(transactionId,
                                                                      targetAccountInfoDO.getAccountNo(),
                                                                      amount);
        if (!sourceChangeFlag || !targetChangeFlag) {
            throw new RuntimeException("无法更新账户余额变动记录，很可能数据库有问题");
        }

        AccountTransactionDO transactionDO = accountTransactionService.get(transactionId);
        // 如果交易超过10分钟，也属于交易失败
        if (DateUtil.between(transactionDO.getTransactionStartTime(), new Date(), DateUnit.MINUTE) > 10) {
            accountTransactionService.end(transactionId, TransactionStatus.FAILED);
            throw new BizException(1006, transactionId);
        }

        boolean transactionFlag = accountTransactionService.end(transactionId, TransactionStatus.SUCCESS);
        if (!transactionFlag) {
            throw new RuntimeException("无法更新交易记录，很可能数据库有问题");
        }
        String msg = StrUtil.format("账户 {} 向 账户 {} 转账 {} 元，成功！",
                                    sourceAccountInfoDO.getAccountNo(),
                                    targetAccountInfoDO.getAccountNo(),
                                    amount);
        log.info(msg);
    }

    /**
     * 单个账户修改余额【原子化操作，要么全部成功，要么全部失败（回滚数据）】
     *
     * @param accountInfoDO
     * @param transactionId
     * @param amount
     */
    @Transactional(propagation = Propagation.REQUIRED,
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Throwable.class)
    public void changeBalance(AccountInfoDO accountInfoDO, String transactionId, BigDecimal amount) {

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

        AccountTransactionDO transactionDO = accountTransactionService.get(transactionId);
        // 如果交易超过10分钟，也属于交易失败
        if (DateUtil.between(transactionDO.getTransactionStartTime(), new Date(), DateUnit.MINUTE) > 10) {
            accountTransactionService.end(transactionId, TransactionStatus.FAILED);
            throw new BizException(1006, transactionId);
        }

        boolean transactionFlag = accountTransactionService.end(transactionId, TransactionStatus.SUCCESS);
        if (!transactionFlag) {
            throw new RuntimeException("无法更新交易记录，很可能数据库有问题");
        }

    }

}
