package com.perfect.bcs.biz;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.perfect.bcs.biz.type.TransactionStatus;
import com.perfect.bcs.dal.domain.AccountTransactionDO;
import com.perfect.bcs.dal.mapper.AccountTransactionMapper;
import java.math.BigDecimal;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author liangbo 梁波
 * @date 2024-02-01 15:40
 */
@Service
@Slf4j
public class AccountTransactionService extends ServiceImpl<AccountTransactionMapper, AccountTransactionDO> {

    public AccountTransactionDO get(String transactionId) {

        return lambdaQuery().eq(AccountTransactionDO::getTransactionId, transactionId)
                            .one();
    }

    public boolean create(String transactionId, String sourceAccountNo, String targetAccountNo,
                          BigDecimal transactionAmount) {

        try {
            AccountTransactionDO transactionDO = new AccountTransactionDO()
                    .setTransactionId(transactionId)
                    .setSourceAccountNo(sourceAccountNo)
                    .setTargetAccountNo(targetAccountNo)
                    .setTransactionAmount(transactionAmount)
                    .setTransactionStartTime(new Date())
                    .setTransactionStatus(TransactionStatus.STARTED);

            return save(transactionDO);
        } // 通过 transactionId 的唯一性，来避免交易的重复执行。
        // 重复执行，会抛出异常，具体的异常依赖于底层的框架
        catch (Throwable e) {
            log.error("创建交易异常", e);
            return false;
        }
    }

    public boolean end(String transactionId, String transactionStatus) {

        return lambdaUpdate().eq(AccountTransactionDO::getTransactionId, transactionId)
                             .set(AccountTransactionDO::getTransactionEndTime, new Date())
                             .set(AccountTransactionDO::getTransactionStatus, transactionStatus)
                             .update();
    }
}
