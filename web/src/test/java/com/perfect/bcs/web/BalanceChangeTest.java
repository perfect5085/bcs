package com.perfect.bcs.web;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.perfect.bcs.biz.AccountInfoService;
import com.perfect.bcs.biz.AccountTransactionService;
import com.perfect.bcs.biz.BalanceManageService;
import com.perfect.bcs.biz.type.AccountStatus;
import com.perfect.bcs.biz.type.TransactionStatus;
import com.perfect.bcs.dal.domain.AccountInfoDO;
import com.perfect.bcs.dal.domain.AccountTransactionDO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author liangbo 梁波
 * @date 2025-01-22 22:37
 */
@Slf4j
@SpringBootTest(classes = { ApplicationJar.class })
@ExtendWith(SpringExtension.class)
public class BalanceChangeTest {

    @Autowired
    private BalanceManageService      balanceManageService;
    @Autowired
    private AccountInfoService        accountInfoService;
    @Autowired
    private AccountTransactionService accountTransactionService;

    ExecutorService executor = Executors.newFixedThreadPool(10);

    @Test
    public void testChangeBalanceByConcurrency() throws Throwable {

        LambdaQueryWrapper<AccountInfoDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccountInfoDO::getAccountStatus, AccountStatus.ACTIVE);
        Page<AccountInfoDO> page = accountInfoService.page(new Page<>(1, 20), wrapper);
        for (int i = 0; i < 10; i++) {

            List<Future<?>> futures = new ArrayList<>();

            int randomIndex = RandomUtil.randomInt(0, 20);
            List<AccountInfoDO> list = page.getRecords();

            AccountInfoDO accountInfoDO = list.get(randomIndex);
            // 实时获取最新的余额
            accountInfoDO = accountInfoService.getByAccountNo(accountInfoDO.getAccountNo());

            System.out.println(accountInfoDO.getAccountNo() + " : 账户余额 : " + accountInfoDO.getAccountBalance());
            BalanceDto balanceDto = new BalanceDto().setBalance(accountInfoDO.getAccountBalance());
            for (int j = 0; j < 10; j++) {
                ChangeBalanceRunnable runnable = new ChangeBalanceRunnable(balanceDto, balanceManageService,
                                                                           accountTransactionService, accountInfoDO.getAccountNo());

                Future<?> future = executor.submit(runnable);
                futures.add(future);
            }

            // 等待所有任务完成
            for (Future<?> future : futures) {
                try {
                    future.get(); // 等待任务执行完毕
                } catch (InterruptedException | ExecutionException e) {
                    log.error("线程异常", e);
                }
            }

            accountInfoDO = accountInfoService.getByAccountNo(accountInfoDO.getAccountNo());
            String msg = accountInfoDO.getAccountNo() + " 不一致 ";
            Assertions.assertEquals(balanceDto.getBalance(), accountInfoDO.getAccountBalance(), msg);

            Thread.sleep(1000);
        }

    }

    static class ChangeBalanceRunnable implements Runnable {

        private BalanceDto balance;

        private BalanceManageService balanceManageService;

        private AccountTransactionService accountTransactionService;

        private String accountNo;

        public ChangeBalanceRunnable(BalanceDto balance,
                                     BalanceManageService balanceManageService,
                                     AccountTransactionService accountTransactionService,
                                     String accountNo) {
            this.balance = balance;
            this.balanceManageService = balanceManageService;
            this.accountTransactionService = accountTransactionService;
            this.accountNo = accountNo;
        }

        @Override
        public void run() {
            String transactionId = IdUtil.fastSimpleUUID();
            BigDecimal amount = RandomUtil.randomBigDecimal(new BigDecimal("-100.00"), new BigDecimal("100.00"));
            amount = amount.setScale(2, RoundingMode.HALF_UP);
            try {
                balanceManageService.changeBalance(transactionId, accountNo, amount);
                System.out.println(accountNo + " : 扣款/存款 : " + amount);
                balance.add(amount);
            } catch (Throwable e) {
                AccountTransactionDO transactionDO = accountTransactionService.get(transactionId);
                if (TransactionStatus.SUCCESS.equals(transactionDO.getTransactionStatus())) {
                    System.out.println(accountNo + " : 扣款/存款 : " + amount);
                    balance.add(amount);
                }
            }
        }
    }

    @Data
    @Accessors(chain = true)
    static class BalanceDto {

        private BigDecimal balance;

        public synchronized void add(BigDecimal value) {
            balance = NumberUtil.add(balance, value);
        }
    }

    @Test
    public void testChangeBalanceByOrder() throws Throwable {

        LambdaQueryWrapper<AccountInfoDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccountInfoDO::getAccountStatus, AccountStatus.ACTIVE);
        Page<AccountInfoDO> page = accountInfoService.page(new Page<>(1, 20), wrapper);
        for (int i = 0; i < 10; i++) {
            int randomIndex = RandomUtil.randomInt(0, 20);
            List<AccountInfoDO> list = page.getRecords();

            AccountInfoDO accountInfoDO = list.get(randomIndex);
            // 实时获取最新的余额
            accountInfoDO = accountInfoService.getByAccountNo(accountInfoDO.getAccountNo());
            System.out.println(accountInfoDO.getAccountNo() + " : 账户余额 : " + accountInfoDO.getAccountBalance());
            BigDecimal balance = accountInfoDO.getAccountBalance();

            for (int j = 0; j < 10; j++) {
                String transactionId = IdUtil.fastSimpleUUID();
                String accountNo = accountInfoDO.getAccountNo();
                BigDecimal amount = RandomUtil.randomBigDecimal(new BigDecimal("-100.00"), new BigDecimal("100.00"));
                amount = amount.setScale(2, RoundingMode.HALF_UP);

                try {
                    balanceManageService.changeBalance(transactionId, accountNo, amount);
                    System.out.println(accountNo + " : 扣款/存款 : " + amount);
                    balance = NumberUtil.add(balance, amount);
                } catch (Throwable e) {
                    AccountTransactionDO transactionDO = accountTransactionService.get(transactionId);
                    if (TransactionStatus.SUCCESS.equals(transactionDO.getTransactionStatus())) {
                        System.out.println(accountNo + " : 扣款/存款 : " + amount);
                        balance = NumberUtil.add(balance, amount);
                    }
                }

                accountInfoDO = accountInfoService.getByAccountNo(accountInfoDO.getAccountNo());
                String msg = accountInfoDO.getAccountNo() + " 不一致 ";
                Assertions.assertEquals(balance, accountInfoDO.getAccountBalance(), msg);
            }

            Thread.sleep(1000);
        }

    }

}
