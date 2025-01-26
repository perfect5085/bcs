package com.perfect.bcs.biz;

import cn.hutool.core.util.IdUtil;
import com.perfect.bcs.biz.type.AccountStatus;
import com.perfect.bcs.dal.domain.AccountInfoDO;
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
public class AccountInfoServiceTest {

    @Autowired
    private AccountInfoService accountInfoService;

    @Test
    public void testgetByAccountNo() throws Throwable {

        AccountInfoDO accountInfoDO = new AccountInfoDO()
                .setAccountNo(IdUtil.fastSimpleUUID())
                .setAccountName("刘国强")
                .setAccountBalance(new BigDecimal("56843.25"))
                .setAccountNote("--")
                .setAccountStatus(AccountStatus.ACTIVE)
                .setDataVersion(IdUtil.fastSimpleUUID());

        accountInfoService.save(accountInfoDO);

        AccountInfoDO accountDB = accountInfoService.getByAccountNo(accountInfoDO.getAccountNo());

        Assertions.assertEquals(accountInfoDO.getAccountNo(), accountDB.getAccountNo());
        Assertions.assertEquals(accountInfoDO.getAccountName(), accountDB.getAccountName());
        Assertions.assertEquals(accountInfoDO.getAccountBalance(), accountDB.getAccountBalance());
        Assertions.assertEquals(accountInfoDO.getAccountNote(), accountDB.getAccountNote());
        Assertions.assertEquals(accountInfoDO.getAccountStatus(), accountDB.getAccountStatus());
        Assertions.assertEquals(accountInfoDO.getDataVersion(), accountDB.getDataVersion());

    }

    @Test
    public void testupdateBalance() throws Throwable {

        AccountInfoDO accountInfoDO = new AccountInfoDO()
                .setAccountNo(IdUtil.fastSimpleUUID())
                .setAccountName("刘国强")
                .setAccountBalance(new BigDecimal("56843.25"))
                .setAccountNote("--")
                .setAccountStatus(AccountStatus.ACTIVE)
                .setDataVersion(IdUtil.fastSimpleUUID());
        accountInfoService.save(accountInfoDO);

        BigDecimal newBalance = new BigDecimal("1122.22");
        String newDataVersion = IdUtil.fastSimpleUUID();
        accountInfoService.updateBalance(accountInfoDO.getAccountNo(),
                                         newBalance,
                                         accountInfoDO.getDataVersion(),
                                         newDataVersion);

        AccountInfoDO accountDB = accountInfoService.getByAccountNo(accountInfoDO.getAccountNo());

        Assertions.assertEquals(newBalance, accountDB.getAccountBalance());
        Assertions.assertEquals(newDataVersion, accountDB.getDataVersion());
    }

}
