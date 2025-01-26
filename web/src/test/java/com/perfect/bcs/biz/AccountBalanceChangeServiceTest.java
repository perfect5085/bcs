package com.perfect.bcs.biz;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.perfect.bcs.dal.domain.AccountBalanceChangeDO;
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
public class AccountBalanceChangeServiceTest {

    @Autowired
    private AccountBalanceChangeService accountBalanceChangeService;

    @Test
    public void testcreate() throws Throwable {

        String transactionId = IdUtil.fastSimpleUUID();
        String accountNo = IdUtil.fastSimpleUUID();
        BigDecimal amount = new BigDecimal("4894.56");

        accountBalanceChangeService.create(transactionId, accountNo, amount);

        LambdaQueryWrapper<AccountBalanceChangeDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AccountBalanceChangeDO::getTransactionId, transactionId)
                          .eq(AccountBalanceChangeDO::getAccountNo, accountNo);
        AccountBalanceChangeDO changeDO = accountBalanceChangeService.getOne(lambdaQueryWrapper);

        Assertions.assertEquals(changeDO.getAccountNo(), accountNo);
        Assertions.assertEquals(changeDO.getTransactionId(), transactionId);
        Assertions.assertEquals(changeDO.getChangeAmount(), amount);

    }

}
