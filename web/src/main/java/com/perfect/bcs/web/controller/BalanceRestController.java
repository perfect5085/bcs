package com.perfect.bcs.web.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.NumberUtil;
import com.perfect.bcs.biz.BalanceManageService;
import com.perfect.bcs.web.controller.common.CommonController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liangbo 梁波
 * @date 2025-01-22 22:37
 */
@Api(tags = "账户余额管理")
@RestController
@RequestMapping(value = "/open/balance/", produces = "application/json")
@Slf4j
public class BalanceRestController extends CommonController {

    @Autowired
    private BalanceManageService balanceManageService;

    @ApiOperation(value = "查看账户余额")
    @ApiImplicitParams({ @ApiImplicitParam(name = "accountNo", value = "账户Id", required = true), })
    @GetMapping(value = "get.json")
    public BigDecimal get(String accountNo) throws Throwable {
        Assert.notBlank(accountNo, "accountNo 不能为空！！！");

        return balanceManageService.getBalance(accountNo);
    }

    @ApiOperation(value = "取钱（扣款）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "transactionId", value = "交易ID，每次交易ID必须唯一，重复交易ID只执行一次", required = true),
            @ApiImplicitParam(name = "accountNo", value = "账户Id", required = true),
            @ApiImplicitParam(name = "amount", value = "取钱数目，单位元，精确到分。取钱不能是负数", required = true), })
    @PostMapping(value = "withdraw.json")
    public void withdraw(String transactionId, String accountNo, BigDecimal amount) throws Throwable {
        Assert.notBlank(transactionId, "transactionId 不能为空！！！");
        Assert.notBlank(accountNo, "accountNo 不能为空！！！");
        Assert.notNull(amount, "amount 不能为空！！！");
        Assert.isTrue(NumberUtil.isLess(amount, BigDecimal.ZERO), "amount 取款金额不能为负数！！！ ");

        balanceManageService.changeBalance(transactionId, accountNo, NumberUtil.mul(amount, new BigDecimal("-1")));
    }

    @ApiOperation(value = "存钱")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "transactionId", value = "交易ID，每次交易ID必须唯一，重复交易ID只执行一次", required = true),
            @ApiImplicitParam(name = "accountNo", value = "账户Id", required = true),
            @ApiImplicitParam(name = "amount", value = "存钱数目，单位元，精确到分。存钱不能是负数", required = true), })
    @PostMapping(value = "deposit.json")
    public void deposit(String transactionId, String accountNo, BigDecimal amount) throws Throwable {
        Assert.notBlank(transactionId, "transactionId 不能为空！！！");
        Assert.notBlank(accountNo, "accountNo 不能为空！！！");
        Assert.notNull(amount, "amount 不能为空！！！");
        Assert.isTrue(NumberUtil.isLess(amount, BigDecimal.ZERO), "amount 存款金额不能为负数！！！ ");

        balanceManageService.changeBalance(transactionId, accountNo, amount);

    }

    @ApiOperation(value = "两个账户之间转账：从来源账户转账到目标账户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "transactionId", value = "交易ID，每次交易ID必须唯一，重复交易ID只执行一次", required = true),
            @ApiImplicitParam(name = "sourceAccountNo", value = "来源账户Id", required = true),
            @ApiImplicitParam(name = "targetAccountNo", value = "目标账户Id", required = true),
            @ApiImplicitParam(name = "amount", value = "转账数目，单位元，精确到分。转账不能是负数", required = true), })
    @RequestMapping(value = "transfer.json")
    public void transfer(String transactionId, String sourceAccountNo, String targetAccountNo, BigDecimal amount)
            throws Throwable {
        Assert.notBlank(transactionId, "transactionId 不能为空！！！");
        Assert.notBlank(sourceAccountNo, "sourceAccountNo 不能为空！！！");
        Assert.notBlank(targetAccountNo, "targetAccountNo 不能为空！！！");
        Assert.notNull(amount, "amount 不能为空！！！");
        Assert.isTrue(NumberUtil.isLess(amount, BigDecimal.ZERO), "amount 转账金额不能为负数！！！ ");

        balanceManageService.transferBalance(transactionId, sourceAccountNo, targetAccountNo, amount);
    }

}