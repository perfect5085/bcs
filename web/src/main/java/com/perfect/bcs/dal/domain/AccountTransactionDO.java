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
 * 账户交易表
 * </p>
 *
 * @author MyBatis-Plus自动生成
 * @since 2025-01-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("account_transaction")
public class AccountTransactionDO extends Model<AccountTransactionDO> {

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
     * 来源账户
     */
    private String sourceAccountNo;

    /**
     * 目标账户
     */
    private String targetAccountNo;

    /**
     * 交易金额
     */
    private BigDecimal transactionAmount;

    /**
     * 交易开始时间
     */
    private Date transactionStartTime;

    /**
     * 交易结束时间
     */
    private Date transactionEndTime;

    /**
     * 交易的状态： started 开始； success 成功； failed 失败; 
     */
    private String transactionStatus;

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
