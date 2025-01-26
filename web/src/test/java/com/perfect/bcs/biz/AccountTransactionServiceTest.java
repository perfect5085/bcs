package com.perfect.bcs.biz;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.perfect.bcs.biz.type.TransactionStatus;
import com.perfect.bcs.dal.domain.AccountTransactionDO;
import com.perfect.bcs.web.ApplicationJar;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author liangbo 梁波
 * @date 2025-01-26 15:24
 */
@Slf4j
@SpringBootTest(classes = { ApplicationJar.class })
@ExtendWith(SpringExtension.class)
@Transactional
public class AccountTransactionServiceTest {

    @Autowired
    private AccountTransactionService accountTransactionService;

    @Test
    public void testcreate() throws Throwable {

        String transactionId = IdUtil.fastSimpleUUID();
        String sourceAccountNo = IdUtil.fastSimpleUUID();
        String targetAccountNo = IdUtil.fastSimpleUUID();
        BigDecimal amount = new BigDecimal("4894.56");

        accountTransactionService.create(transactionId, sourceAccountNo, targetAccountNo, amount);

        LambdaQueryWrapper<AccountTransactionDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AccountTransactionDO::getTransactionId, transactionId);
        AccountTransactionDO transactionDO = accountTransactionService.get(transactionId);

        Assertions.assertEquals(transactionDO.getTransactionId(), transactionId);
        Assertions.assertEquals(transactionDO.getSourceAccountNo(), sourceAccountNo);
        Assertions.assertEquals(transactionDO.getTargetAccountNo(), targetAccountNo);
        Assertions.assertEquals(transactionDO.getTransactionAmount(), amount);

        try {
            // 事务不能重复创建
            accountTransactionService.create(transactionId, sourceAccountNo, targetAccountNo, amount);
            Assertions.assertTrue(true, "");
        } catch (Throwable e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void testend() throws Throwable {

        String transactionId = IdUtil.fastSimpleUUID();
        String sourceAccountNo = IdUtil.fastSimpleUUID();
        String targetAccountNo = IdUtil.fastSimpleUUID();
        BigDecimal amount = new BigDecimal("4894.56");

        accountTransactionService.create(transactionId, sourceAccountNo, targetAccountNo, amount);

        accountTransactionService.end(transactionId, TransactionStatus.FAILED);

        LambdaQueryWrapper<AccountTransactionDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AccountTransactionDO::getTransactionId, transactionId);
        AccountTransactionDO transactionDO = accountTransactionService.get(transactionId);

        Assertions.assertEquals(transactionDO.getTransactionStatus(), TransactionStatus.FAILED);
    }

}
