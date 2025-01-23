package com.perfect.bcs.biz.type;

/**
 * 账户的状态
 *
 * @author liangbo 梁波
 * @date 2025-01-23 09:14
 */
public interface AccountStatus {

    /**
     * active 正常
     */
    String ACTIVE = "active";
    /**
     * frozen 冻结
     */
    String FROZEN  = "frozen";
    /**
     * inactive 停用;
     */
    String INACTIVE = "inactive";
}
