package com.perfect.bcs.dal.domain;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 账户余额变动表
 * </p>
 *
 * @author MyBatis-Plus自动生成
 * @since 2025-01-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("account_balance_change")
public class AccountBalanceChangeDO extends Model<AccountBalanceChangeDO> {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 交易ID
     */
    private String transactionId;

    /**
     * 账户
     */
    private String accountNo;

    /**
     * 变动金额：正值表示增加；负值表示扣减
     */
    private BigDecimal changeAmount;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 最后修改时间
     */
    private Date modifyTime;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
