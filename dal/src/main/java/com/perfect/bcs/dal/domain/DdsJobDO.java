package com.perfect.bcs.dal.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * dds作业
 * </p>
 *
 * @author MyBatis-Plus自动生成
 * @since 2022-05-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dds_job")
public class DdsJobDO extends Model<DdsJobDO> {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 医院编码
     */
    private String hospCode;

    /**
     * 名称
     */
    private String name;

    /**
     * 编码
     */
    private String code;

    /**
     * 定时表达式
     */
    private String cron;

    /**
     * 数据开始时间
     */
    private String dataStartTime;

    /**
     * 数据结束时间
     */
    private String dataEndTime;

    /**
     * 数据回拨时间单位：day（天）， hour（小时）， minute（分钟）
     */
    private String dataTimeBackUnit;

    /**
     * 数据回拨时间数值
     */
    private Integer dataTimeBackNumber;

    /**
     * 数据ID列表
     */
    private String dataId;

    /**
     * 关联的任务code列表，逗号分隔
     */
    private String taskCodeList;

    /**
     * 作业状态：wait 等待， running 运行，success 成功，failure 失败
     */
    private String runStatus;

    /**
     * 备注
     */
    private String note;

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
