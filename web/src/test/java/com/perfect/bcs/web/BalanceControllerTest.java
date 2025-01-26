package com.perfect.bcs.web;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import com.perfect.bcs.biz.AccountBalanceChangeService;
import com.perfect.bcs.biz.AccountInfoService;
import com.perfect.bcs.biz.type.AccountStatus;
import com.perfect.bcs.dal.domain.AccountInfoDO;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author liangbo 梁波
 * @date 2025-01-26 15:24
 */
@Slf4j
@SpringBootTest(classes = { ApplicationJar.class })
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@Transactional
public class BalanceControllerTest {

    @Autowired
    private MockMvc                     mockMvc;
    @Autowired
    private AccountInfoService          accountInfoService;
    @Autowired
    private AccountBalanceChangeService accountBalanceChangeService;

    @Test
    public void testGetByAccount() throws Throwable {

        // 准备测试数据
        AccountInfoDO accountInfo = new AccountInfoDO()
                .setAccountNo(IdUtil.fastSimpleUUID())
                .setAccountName("刘国强")
                .setAccountBalance(new BigDecimal("56843.25"))
                .setAccountNote("--")
                .setAccountStatus(AccountStatus.ACTIVE)
                .setDataVersion(IdUtil.fastSimpleUUID());
        accountInfoService.save(accountInfo);

        // 发起 GET 请求并验证结果
        mockMvc.perform(MockMvcRequestBuilders.get("/open/balance/get-by-account")
                                              .param("accountNo", accountInfo.getAccountNo())
                                              .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk()) // 验证 HTTP 状态码
               .andExpect(jsonPath("$.code").value(0))
               .andExpect(jsonPath("$.data").value(accountInfo.getAccountBalance()))
               .andDo(print());

    }

    @Test
    public void testwithdraw() throws Throwable {

        // 准备测试数据
        AccountInfoDO accountInfo = new AccountInfoDO()
                .setAccountNo(IdUtil.fastSimpleUUID())
                .setAccountName("刘国强")
                .setAccountBalance(new BigDecimal("56843.25"))
                .setAccountNote("--")
                .setAccountStatus(AccountStatus.ACTIVE)
                .setDataVersion(IdUtil.fastSimpleUUID());
        accountInfoService.save(accountInfo);

        String transactionId = IdUtil.fastSimpleUUID();
        BigDecimal amount = new BigDecimal("20.00");
        BigDecimal balance = NumberUtil.sub(accountInfo.getAccountBalance(), amount);

        // 发起 GET 请求并验证结果
        mockMvc.perform(MockMvcRequestBuilders.post("/open/balance/withdraw-by-account")
                                              .param("transactionId", transactionId)
                                              .param("accountNo", accountInfo.getAccountNo())
                                              .param("amount", amount.toString())
                                              .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk()) // 验证 HTTP 状态码
               .andExpect(jsonPath("$.code").value(0))
               .andDo(print());

        AccountInfoDO accountDB = accountInfoService.getByAccountNo(accountInfo.getAccountNo());
        Assertions.assertEquals(balance, accountDB.getAccountBalance(), "取款之后，余额不对！");
    }

    @Test
    public void testDeposit() throws Throwable {

        // 准备测试数据
        AccountInfoDO accountInfo = new AccountInfoDO()
                .setAccountNo(IdUtil.fastSimpleUUID())
                .setAccountName("刘国强")
                .setAccountBalance(new BigDecimal("56843.25"))
                .setAccountNote("--")
                .setAccountStatus(AccountStatus.ACTIVE)
                .setDataVersion(IdUtil.fastSimpleUUID());
        accountInfoService.save(accountInfo);

        String transactionId = IdUtil.fastSimpleUUID();
        BigDecimal amount = new BigDecimal("20.00");
        BigDecimal balance = NumberUtil.add(accountInfo.getAccountBalance(), amount);

        // 发起 GET 请求并验证结果
        mockMvc.perform(MockMvcRequestBuilders.post("/open/balance/deposit-by-account")
                                              .param("transactionId", transactionId)
                                              .param("accountNo", accountInfo.getAccountNo())
                                              .param("amount", amount.toString())
                                              .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk()) // 验证 HTTP 状态码
               .andExpect(jsonPath("$.code").value(0))
               .andDo(print());

        AccountInfoDO accountDB = accountInfoService.getByAccountNo(accountInfo.getAccountNo());
        Assertions.assertEquals(balance, accountDB.getAccountBalance(), "存款之后，余额不对！");
    }

    @Test
    public void testTransfer() throws Throwable {

        // 准备测试数据
        AccountInfoDO soruceAccountInfo = new AccountInfoDO()
                .setAccountNo(IdUtil.fastSimpleUUID())
                .setAccountName("刘国强")
                .setAccountBalance(new BigDecimal("56843.25"))
                .setAccountNote("--")
                .setAccountStatus(AccountStatus.ACTIVE)
                .setDataVersion(IdUtil.fastSimpleUUID());
        accountInfoService.save(soruceAccountInfo);

        AccountInfoDO targetAccountInfo = new AccountInfoDO()
                .setAccountNo(IdUtil.fastSimpleUUID())
                .setAccountName("刘冬冬")
                .setAccountBalance(new BigDecimal("96843.25"))
                .setAccountNote("--")
                .setAccountStatus(AccountStatus.ACTIVE)
                .setDataVersion(IdUtil.fastSimpleUUID());
        accountInfoService.save(targetAccountInfo);

        String transactionId = IdUtil.fastSimpleUUID();
        BigDecimal amount = new BigDecimal("20.00");
        BigDecimal sourceBalance = NumberUtil.sub(soruceAccountInfo.getAccountBalance(), amount);
        BigDecimal targetBalance = NumberUtil.add(targetAccountInfo.getAccountBalance(), amount);

        // 发起 GET 请求并验证结果
        mockMvc.perform(MockMvcRequestBuilders.post("/open/balance/source-transfer-target")
                                              .param("transactionId", transactionId)
                                              .param("sourceAccountNo", soruceAccountInfo.getAccountNo())
                                              .param("targetAccountNo", targetAccountInfo.getAccountNo())
                                              .param("amount", amount.toString())
                                              .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk()) // 验证 HTTP 状态码
               .andExpect(jsonPath("$.code").value(0))
               .andDo(print());

        AccountInfoDO sourceAccountDB = accountInfoService.getByAccountNo(soruceAccountInfo.getAccountNo());
        Assertions.assertEquals(sourceBalance, sourceAccountDB.getAccountBalance(), "余额不对！");

        AccountInfoDO targetAccountDB = accountInfoService.getByAccountNo(targetAccountInfo.getAccountNo());
        Assertions.assertEquals(targetBalance, targetAccountDB.getAccountBalance(), "余额不对！");
    }

}
