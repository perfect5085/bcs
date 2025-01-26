package com.perfect.bcs.biz;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.perfect.bcs.biz.type.AccountStatus;
import com.perfect.bcs.biz.type.TransactionStatus;
import com.perfect.bcs.dal.domain.AccountInfoDO;
import com.perfect.bcs.dal.domain.AccountTransactionDO;
import com.perfect.bcs.web.ApplicationJar;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
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
import org.testng.collections.Lists;

/**
 * @author liangbo 梁波
 * @date 2025-01-22 22:37
 */
@Slf4j
@SpringBootTest(classes = { ApplicationJar.class })
@ExtendWith(SpringExtension.class)
public class BalanceTransferTest {

    @Autowired
    private BalanceManageService      balanceManageService;
    @Autowired
    private AccountInfoService        accountInfoService;
    @Autowired
    private AccountTransactionService accountTransactionService;

    ExecutorService executor = Executors.newFixedThreadPool(10);

    @Test
    public void testTransferByConcurrency() throws Throwable {

        LambdaQueryWrapper<AccountInfoDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccountInfoDO::getAccountStatus, AccountStatus.ACTIVE);
        Page<AccountInfoDO> page = accountInfoService.page(new Page<>(1, 20), wrapper);
        for (int i = 0; i < 10; i++) {

            List<Future<BigDecimal>> futures = new ArrayList<>();

            int targetIndex = RandomUtil.randomInt(0, 20);
            List<AccountInfoDO> list = page.getRecords();

            AccountInfoDO targetAccountInfoDO = list.get(targetIndex);
            // 实时获取最新的余额
            targetAccountInfoDO = accountInfoService.getByAccountNo(targetAccountInfoDO.getAccountNo());
            System.out.println(
                    targetAccountInfoDO.getAccountNo() + " : 账户余额 : " + targetAccountInfoDO.getAccountBalance());
            BalanceDto balanceDto = new BalanceDto()
                    .setSourceBalance(BigDecimal.ZERO)
                    .setTargetBalance(targetAccountInfoDO.getAccountBalance());

            // 抽十个账户给这个账户转账
            for (int j = 0; j < 10; j++) {

                int sourceIndex = RandomUtil.randomInt(0, 20);
                AccountInfoDO sourceAccountInfoDO = list.get(sourceIndex);
                // 实时获取最新的余额
                sourceAccountInfoDO = accountInfoService.getByAccountNo(sourceAccountInfoDO.getAccountNo());
                System.out.println(
                        sourceAccountInfoDO.getAccountNo() + " : 账户余额 : "
                                + sourceAccountInfoDO.getAccountBalance());

                TransferBalanceTask runnable =
                        new TransferBalanceTask(balanceDto, balanceManageService,
                                                accountTransactionService,
                                                sourceAccountInfoDO.getAccountNo(),
                                                targetAccountInfoDO.getAccountNo());

                Future<BigDecimal> future = executor.submit(runnable);
                futures.add(future);
            }

            // 等待所有任务完成
            List<BigDecimal> amountList = Lists.newArrayList();
            for (Future<BigDecimal> future : futures) {
                try {
                    // 等待任务执行完毕
                    amountList.add(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    log.error("线程异常", e);
                }
            }

            System.out.println("向账户：" + targetAccountInfoDO.getAccountNo() + " 转账完成");

            targetAccountInfoDO = accountInfoService.getByAccountNo(targetAccountInfoDO.getAccountNo());
            String msg = targetAccountInfoDO.getAccountNo() + " 不一致 ";
            Assertions.assertEquals(balanceDto.getTargetBalance(), targetAccountInfoDO.getAccountBalance(), msg);

            Thread.sleep(1000);
        }

    }

    static class TransferBalanceTask implements Callable<BigDecimal> {

        private BalanceDto balance;

        private BalanceManageService balanceManageService;

        private AccountTransactionService accountTransactionService;

        private String sourceAccountNo;
        private String targetAccountNo;

        public TransferBalanceTask(BalanceDto balance,
                                   BalanceManageService balanceManageService,
                                   AccountTransactionService accountTransactionService,
                                   String sourceAccountNo, String targetAccountNo) {
            this.balance = balance;
            this.balanceManageService = balanceManageService;
            this.accountTransactionService = accountTransactionService;
            this.sourceAccountNo = sourceAccountNo;
            this.targetAccountNo = targetAccountNo;
        }

        @Override
        public BigDecimal call() {
            String transactionId = IdUtil.fastSimpleUUID();
            BigDecimal amount = RandomUtil.randomBigDecimal(new BigDecimal("00.00"), new BigDecimal("100.00"));
            amount = amount.setScale(2, RoundingMode.HALF_UP);
            try {

                balanceManageService.transferBalance(transactionId, sourceAccountNo, targetAccountNo, amount);

                System.out.println(sourceAccountNo + " : 减少 : " + amount);
                System.out.println(targetAccountNo + " : 增加 : " + amount);
                balance.transfer(amount);
            } catch (Throwable e) {
                AccountTransactionDO transactionDO = accountTransactionService.get(transactionId);

                if (TransactionStatus.SUCCESS.equals(transactionDO.getTransactionStatus())) {
                    System.out.println(sourceAccountNo + " : 减少 : " + amount);
                    System.out.println(targetAccountNo + " : 增加 : " + amount);
                    balance.transfer(amount);
                }
            }

            return amount;
        }
    }

    @Data
    @Accessors(chain = true)
    static class BalanceDto {

        private BigDecimal sourceBalance;
        private BigDecimal targetBalance;

        public synchronized void transfer(BigDecimal amount) {
            sourceBalance = NumberUtil.sub(sourceBalance, amount);
            targetBalance = NumberUtil.add(targetBalance, amount);
        }
    }

    @Test
    public void testChangeBalanceByOrder() throws Throwable {

        LambdaQueryWrapper<AccountInfoDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccountInfoDO::getAccountStatus, AccountStatus.ACTIVE);
        Page<AccountInfoDO> page = accountInfoService.page(new Page<>(1, 20), wrapper);
        for (int i = 0; i < 10; i++) {
            int targetIndex = RandomUtil.randomInt(0, 20);
            List<AccountInfoDO> list = page.getRecords();

            AccountInfoDO targetAccountInfoDO = list.get(targetIndex);
            // 实时获取最新的余额
            targetAccountInfoDO = accountInfoService.getByAccountNo(targetAccountInfoDO.getAccountNo());
            System.out.println(
                    targetAccountInfoDO.getAccountNo() + " : 账户余额 : " + targetAccountInfoDO.getAccountBalance());
            BigDecimal balance = targetAccountInfoDO.getAccountBalance();

            for (int j = 0; j < 10; j++) {

                int sourceIndex = RandomUtil.randomInt(0, 20);
                AccountInfoDO soruceAccountInfoDO = list.get(sourceIndex);

                String transactionId = IdUtil.fastSimpleUUID();
                String sourceAccountNo = soruceAccountInfoDO.getAccountNo();
                String targetAccountNo = targetAccountInfoDO.getAccountNo();
                BigDecimal amount = RandomUtil.randomBigDecimal(new BigDecimal("00.00"), new BigDecimal("100.00"));
                amount = amount.setScale(2, RoundingMode.HALF_UP);

                try {
                    balanceManageService.transferBalance(transactionId, sourceAccountNo, targetAccountNo, amount);

                    System.out.println(sourceAccountNo + " : 减少 : " + amount);
                    System.out.println(targetAccountNo + " : 增加 : " + amount);
                    balance = NumberUtil.add(balance, amount);
                } catch (Throwable e) {
                    AccountTransactionDO transactionDO = accountTransactionService.get(transactionId);
                    if (TransactionStatus.SUCCESS.equals(transactionDO.getTransactionStatus())) {

                        System.out.println(sourceAccountNo + " : 减少 : " + amount);
                        System.out.println(targetAccountNo + " : 增加 : " + amount);
                        balance = NumberUtil.add(balance, amount);
                    }
                }

                targetAccountInfoDO = accountInfoService.getByAccountNo(targetAccountInfoDO.getAccountNo());
                String msg = targetAccountInfoDO.getAccountNo() + " 不一致 ";
                Assertions.assertEquals(balance, targetAccountInfoDO.getAccountBalance(), msg);
            }

            Thread.sleep(1000);
        }

    }

}
