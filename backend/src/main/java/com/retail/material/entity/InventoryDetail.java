package com.retail.material.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inventory_detail")
public class InventoryDetail extends BaseEntity {

    private Long planId;
    private Long materialId;
    private String materialName;
    private String specification;
    private String unit;
    private Integer systemQuantity;
    private Integer actualQuantity;
    private Integer difference;
    private BigDecimal unitCost;
    private BigDecimal differenceCost;
    private String differenceReason;
}