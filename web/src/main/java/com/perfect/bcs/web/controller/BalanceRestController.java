package com.perfect.bcs.web.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.NumberUtil;
import com.perfect.bcs.biz.BalanceManageService;
import com.perfect.bcs.web.controller.common.CommonController;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/open/balance/", produces = "application/json")
@Slf4j
public class BalanceRestController extends CommonController {

    @Autowired
    private BalanceManageService balanceManageService;

    @GetMapping(value = "get.json")
    public BigDecimal get(String accountNo) throws Throwable {
        Assert.notBlank(accountNo, "accountNo 不能为空！！！");

        return balanceManageService.getBalance(accountNo);
    }

    @PostMapping(value = "withdraw.json")
    public void withdraw(String transactionId, String accountNo, BigDecimal amount) throws Throwable {
        Assert.notBlank(transactionId, "transactionId 不能为空！！！");
        Assert.notBlank(accountNo, "accountNo 不能为空！！！");
        Assert.notNull(amount, "amount 不能为空！！！");
        Assert.isTrue(NumberUtil.isLess(amount, BigDecimal.ZERO), "amount 取款金额不能为负数！！！ ");

        balanceManageService.changeBalance(transactionId, accountNo, NumberUtil.mul(amount, new BigDecimal("-1")));
    }

    @PostMapping(value = "deposit.json")
    public void deposit(String transactionId, String accountNo, BigDecimal amount) throws Throwable {
        Assert.notBlank(transactionId, "transactionId 不能为空！！！");
        Assert.notBlank(accountNo, "accountNo 不能为空！！！");
        Assert.notNull(amount, "amount 不能为空！！！");
        Assert.isTrue(NumberUtil.isLess(amount, BigDecimal.ZERO), "amount 存款金额不能为负数！！！ ");

        balanceManageService.changeBalance(transactionId, accountNo, amount);

    }

    @RequestMapping(value = "transfer.json")
    public void transfer(String transactionId, String sourceAccountNo, String targetAccountNo, BigDecimal amount)
            throws Throwable {
        Assert.notBlank(transactionId, "transactionId 不能为空！！！");
        Assert.notBlank(sourceAccountNo, "sourceAccountNo 不能为空！！！");
        Assert.notBlank(targetAccountNo, "targetAccountNo 不能为空！！！");
        Assert.notNull(amount, "amount 不能为空！！！");

        balanceManageService.transferBalance(transactionId, sourceAccountNo, targetAccountNo, amount);
    }

}