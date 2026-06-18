package com.retail.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "月度消耗查询请求")
public class ConsumptionQueryDTO {

    @Schema(description = "当前页码", defaultValue = "1")
    private Long current = 1L;

    @Schema(description = "每页大小", defaultValue = "10")
    private Long size = 10L;

    @Schema(description = "门店ID")
    private Long storeId;

    @Schema(description = "年份")
    private Integer year;

    @Schema(description = "月份")
    private Integer month;

    @Schema(description = "耗材ID")
    private Long materialId;
}