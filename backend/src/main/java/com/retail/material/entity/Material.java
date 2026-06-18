package com.retail.material.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("material")
public class Material extends BaseEntity {

    private String materialCode;
    private String materialName;
    private String category;
    private String specification;
    private String unit;
    private BigDecimal purchaseCost;
    private Integer shelfLife;
    private String shelfLocation;
    private Integer safetyStock;
    private Integer currentStock;
    private Integer status;
}