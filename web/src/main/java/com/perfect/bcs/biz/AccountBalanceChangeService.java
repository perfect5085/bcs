package com.perfect.bcs.biz;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.perfect.bcs.dal.domain.AccountBalanceChangeDO;
import com.perfect.bcs.dal.mapper.AccountBalanceChangeMapper;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author liangbo 梁波
 * @date 2024-02-01 15:40
 */
@Service
@Slf4j
public class AccountBalanceChangeService extends ServiceImpl<AccountBalanceChangeMapper, AccountBalanceChangeDO> {

    public boolean create(String transactionId, String accountNo, BigDecimal changeAmount) {
        AccountBalanceChangeDO transactionDO = new AccountBalanceChangeDO()
                .setTransactionId(transactionId)
                .setAccountNo(accountNo)
                .setChangeAmount(changeAmount);

        return save(transactionDO);
    }
}
