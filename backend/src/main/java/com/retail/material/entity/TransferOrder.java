package com.retail.material.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("transfer_order")
public class TransferOrder extends BaseEntity {

    private String orderNo;
    private Long sourceStoreId;
    private Long targetStoreId;
    private Integer status;
    private BigDecimal totalCost;
    private Long applicantId;
    private LocalDateTime applyTime;
    private Long approverId;
    private LocalDateTime approveTime;
    private LocalDateTime outboundTime;
    private LocalDateTime receiveTime;
    private String remark;
}