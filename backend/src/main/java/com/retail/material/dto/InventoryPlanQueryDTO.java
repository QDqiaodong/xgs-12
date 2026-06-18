package com.retail.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "盘点计划查询请求")
public class InventoryPlanQueryDTO {

    @Schema(description = "当前页码", defaultValue = "1")
    private Long current = 1L;

    @Schema(description = "每页大小", defaultValue = "10")
    private Long size = 10L;

    @Schema(description = "门店ID")
    private Long storeId;

    @Schema(description = "状态：0待执行 1执行中 2已完成 3已取消")
    private Integer status;
}