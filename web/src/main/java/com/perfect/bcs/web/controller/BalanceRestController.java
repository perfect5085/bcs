package com.perfect.bcs.web.controller;

import cn.hutool.core.util.NumberUtil;
import com.perfect.bcs.biz.AccountTransactionService;
import com.perfect.bcs.biz.BalanceManageService;
import com.perfect.bcs.biz.common.BizException;
import com.perfect.bcs.dal.domain.AccountTransactionDO;
import com.perfect.bcs.web.controller.common.CommonController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.math.BigDecimal;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liangbo 梁波
 * @date 2025-01-22 22:37
 */
@Api(tags = "账户余额管理")
@RestController
@RequestMapping(value = "/open/balance", produces = "application/json")
@Slf4j
@Validated
public class BalanceRestController extends CommonController {

    @Autowired
    private BalanceManageService      balanceManageService;
    @Autowired
    private AccountTransactionService accountTransactionService;

    @ApiOperation(value = "查看账户余额")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accountNo", value = "账户Id", required = true) })
    @GetMapping(value = "/get-by-account")
    public BigDecimal get(@RequestParam @NotBlank String accountNo) throws Throwable {

        return balanceManageService.getBalance(accountNo);
    }

    @ApiOperation(value = "查看交易的状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "transactionId", value = "交易ID", required = true), })
    @GetMapping(value = "/get-status-by-transaction-id")
    public String getTransactionStatus(@RequestParam @NotBlank String transactionId) throws Throwable {

        AccountTransactionDO transactionDO = accountTransactionService.get(transactionId);
        if (null == transactionDO) {
            throw new BizException(1007, transactionId);
        }
        return transactionDO.getTransactionStatus();
    }

    @ApiOperation(value = "重置超时交易的状态")
    @PostMapping(value = "/reset-timeout-transaction-status")
    public void resetTimeoutTransactionStatus() throws Throwable {

        balanceManageService.resetTimeoutTransactionStatus();

    }

    @ApiOperation(value = "取钱（扣款）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "transactionId", value = "交易ID，每次交易ID必须唯一，重复交易ID只执行一次", required = true),
            @ApiImplicitParam(name = "accountNo", value = "账户Id", required = true),
            @ApiImplicitParam(name = "amount", value = "取钱数目，单位元，精确到分。取钱不能是负数", required = true), })
    @PostMapping(value = "/withdraw-by-account")
    public void withdraw(@NotBlank String transactionId,
                         @NotBlank String accountNo,
                         @NotNull
                         @DecimalMin(value = "0", message = "取款金额不能为负数！")
                         @DecimalMax(value = "1000000", message = "一次取款金额不能超过100万元！")
                         @Digits(integer = 7, fraction = 2, message = "金额精确到分，小数点后2位！")
                         BigDecimal amount)
            throws Throwable {

        balanceManageService.changeBalance(transactionId, accountNo, NumberUtil.mul(amount, new BigDecimal("-1")));
    }

    @ApiOperation(value = "存钱")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "transactionId", value = "交易ID，每次交易ID必须唯一，重复交易ID只执行一次", required = true),
            @ApiImplicitParam(name = "accountNo", value = "账户Id", required = true),
            @ApiImplicitParam(name = "amount", value = "存钱数目，单位元，精确到分。存钱不能是负数", required = true), })
    @PostMapping(value = "/deposit-by-account")
    public void deposit(@NotBlank String transactionId,
                        @NotBlank String accountNo,
                        @NotNull
                        @DecimalMin(value = "0", message = "存款金额不能为负数！")
                        @DecimalMax(value = "1000000", message = "一次存款金额不能超过100万元！")
                        @Digits(integer = 7, fraction = 2, message = "金额精确到分，小数点后2位！")
                        BigDecimal amount) throws Throwable {

        balanceManageService.changeBalance(transactionId, accountNo, amount);

    }

    @ApiOperation(value = "两个账户之间转账：从来源账户转账到目标账户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "transactionId", value = "交易ID，每次交易ID必须唯一，重复交易ID只执行一次", required = true),
            @ApiImplicitParam(name = "sourceAccountNo", value = "来源账户Id", required = true),
            @ApiImplicitParam(name = "targetAccountNo", value = "目标账户Id", required = true),
            @ApiImplicitParam(name = "amount", value = "转账数目，单位元，精确到分。转账不能是负数", required = true), })
    @PostMapping(value = "/source-transfer-target")
    public void transfer(@NotBlank String transactionId,
                         @NotBlank String sourceAccountNo,
                         @NotBlank String targetAccountNo,
                         @NotNull
                         @DecimalMin(value = "0", message = "转账金额不能为负数！")
                         @DecimalMax(value = "1000000", message = "一次转账金额不能超过100万元！")
                         @Digits(integer = 7, fraction = 2, message = "金额精确到分，小数点后2位！")
                         BigDecimal amount) throws Throwable {

        balanceManageService.transferBalance(transactionId, sourceAccountNo, targetAccountNo, amount);
    }

}