package com.retail.material.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("store_inventory")
public class StoreInventory extends BaseEntity {

    private Long storeId;
    private Long materialId;
    private Integer quantity;
    private Integer lockedQuantity;
    private BigDecimal avgCost;
}