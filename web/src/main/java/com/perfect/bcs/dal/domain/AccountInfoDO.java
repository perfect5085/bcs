package com.perfect.bcs.dal.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 账户表
 * </p>
 *
 * @author MyBatis-Plus自动生成
 * @since 2025-01-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("account_info")
public class AccountInfoDO extends Model<AccountInfoDO> {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 账户
     */
    private String accountNo;

    /**
     * 姓名
     */
    private String accountName;

    /**
     * 余额，单位元，精确到分
     */
    private BigDecimal accountBalance;

    /**
     * 备注
     */
    private String accountNote;

    /**
     * 账户的状态： active 正常； frozen 冻结； inactive 停用;
     */
    private String accountStatus;

    /**
     * 数据版本号
     */
    private String dataVersion;

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
