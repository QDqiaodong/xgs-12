package com.retail.material.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inventory_plan")
public class InventoryPlan extends BaseEntity {

    private String planNo;
    private Long storeId;
    private Integer status;
    private LocalDateTime planDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long creatorId;
    private String remark;
}