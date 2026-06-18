package com.retail.material.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("transfer_item")
public class TransferItem extends BaseEntity {

    private Long transferOrderId;
    private Long materialId;
    private String materialName;
    private String specification;
    private String unit;
    private Integer requestQuantity;
    private Integer approvedQuantity;
    private Integer actualQuantity;
    private BigDecimal unitCost;
    private BigDecimal totalCost;
    private Integer difference;
    private String differenceReason;
}