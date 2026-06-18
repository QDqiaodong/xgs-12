package com.retail.material.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("consumption")
public class Consumption extends BaseEntity {

    private Long storeId;
    private Long materialId;
    private Integer year;
    private Integer month;
    private Integer quantity;
    private BigDecimal totalCost;
    private String remark;
}