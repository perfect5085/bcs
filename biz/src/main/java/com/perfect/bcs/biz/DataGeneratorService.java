package com.perfect.bcs.biz;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.perfect.bcs.biz.type.AccountStatus;
import com.perfect.bcs.dal.domain.AccountInfoDO;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author liangbo 梁波
 * @date 2024-02-01 15:40
 */
@Service
@Slf4j
public class DataGeneratorService {

    @Autowired
    private AccountInfoService          accountInfoService;
    @Autowired
    private AccountTransactionService   accountTransactionService;
    @Autowired
    private AccountBalanceChangeService accountBalanceChangeService;

    private static final String BANK_CODE = "621661";

    public void generateAccounts(int count) {
        List<AccountInfoDO> accountList = new ArrayList<>();

        int batchSize = 1000;

        for (int i = 0; i < count; i++) {
            accountList.clear();
            for (int j = 0; j < batchSize; j++) {
                long accountLong = RandomUtil.randomLong(1000000000000L, 9999999999999L);

                String accountNo = BANK_CODE + accountLong;
                BigDecimal balance = RandomUtil.randomBigDecimal(BigDecimal.ZERO, new BigDecimal("1000000.00"));

                int statusInt = RandomUtil.randomInt(0, 100);
                String status = AccountStatus.ACTIVE;
                if (1 == statusInt) {
                    status = AccountStatus.FROZEN;
                }
                if (2 == statusInt) {
                    status = AccountStatus.INACTIVE;
                }

                AccountInfoDO accountInfoDO = new AccountInfoDO()
                        .setAccountNo(accountNo)
                        .setAccountName(MockDataUtil.getName())
                        .setAccountBalance(balance)
                        .setAccountNote("")
                        .setAccountStatus(status)
                        .setDataVersion(IdUtil.fastSimpleUUID());
                accountList.add(accountInfoDO);
            }

            try {
                accountInfoService.saveBatch(accountList);
            } catch (Throwable e) {
                log.error("批量保存异常", e);
                for (AccountInfoDO accountInfoDO : accountList) {
                    try {
                        accountInfoService.save(accountInfoDO);
                    } catch (Throwable ex) {
                        log.error("单个保存异常", ex);
                    }
                }
            }
        }
    }

}
