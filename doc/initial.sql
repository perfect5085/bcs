CREATE TABLE `account_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `account_no` varchar(32) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '账户',
  `account_name` varchar(150) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '姓名',
  `account_balance` decimal(20,2) DEFAULT '0.00' COMMENT '余额，单位元，精确到分',
  `account_note` varchar(150) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '备注',
  `account_status` varchar(20) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '账户的状态： active 正常； frozen 冻结； inactive 停用; ',
  `data_version` varchar(36) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '数据版本号',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modify_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `idx_account` (`account_no`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE,
  KEY `idx_modify_time` (`modify_time`) USING BTREE
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='账户表';

CREATE TABLE `account_balance_change` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `transaction_id` varchar(36) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '交易ID',
  `account_no` varchar(32) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '账户',
  `change_amount` decimal(20,2) NOT NULL COMMENT '变动金额：正值表示增加；负值表示扣减。单位元，精确到分。',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modify_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_transaction_id` (`transaction_id`) USING BTREE,
  KEY `idx_account_no` (`account_no`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE,
  KEY `idx_modify_time` (`modify_time`) USING BTREE
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='账户余额变动表';

CREATE TABLE `account_transaction` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `transaction_id` varchar(36) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '交易ID',
  `source_account_no` varchar(32) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '来源账户',
  `target_account_no` varchar(32) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '目标账户',
  `transaction_amount` decimal(20,2) NOT NULL COMMENT '交易金额，正值表示增加；负值表示扣减。单位元，精确到分。',
  `transaction_start_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '交易开始时间',
  `transaction_end_time` timestamp NULL DEFAULT NULL COMMENT '交易结束时间',
  `transaction_status` varchar(20) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '交易的状态： started 开始； success 成功； failed 失败; ',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modify_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `idx_transaction_id` (`transaction_id`) USING BTREE,
  KEY `idx_source_account_no` (`source_account_no`) USING BTREE,
  KEY `idx_target_account_no` (`target_account_no`) USING BTREE,
  KEY `idx_transaction_start_time` (`transaction_start_time`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE,
  KEY `idx_modify_time` (`modify_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='账户交易表';