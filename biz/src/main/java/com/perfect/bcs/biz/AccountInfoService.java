package com.perfect.bcs.biz;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.perfect.bcs.dal.domain.AccountInfoDO;
import com.perfect.bcs.dal.mapper.AccountInfoMapper;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author liangbo 梁波
 * @date 2024-02-01 15:40
 */
@Service
@Slf4j
public class AccountInfoService extends ServiceImpl<AccountInfoMapper, AccountInfoDO> {

    public AccountInfoDO getByAccountNo(String accountNo) {

        return lambdaQuery().eq(AccountInfoDO::getAccountNo, accountNo).one();

    }

    public boolean updateBalance(String accountNo, BigDecimal balance,
                                 String oldDataVersion, String newDataVersion) {

        return lambdaUpdate().eq(AccountInfoDO::getAccountNo, accountNo)
                             .eq(AccountInfoDO::getDataVersion, oldDataVersion)
                             .set(AccountInfoDO::getAccountBalance, balance)
                             .set(AccountInfoDO::getDataVersion, newDataVersion)
                             .update();
    }

}
