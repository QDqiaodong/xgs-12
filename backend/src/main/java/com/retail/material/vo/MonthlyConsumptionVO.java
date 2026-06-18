package com.retail.material.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "月度消耗统计响应")
public class MonthlyConsumptionVO {

    @Schema(description = "门店ID")
    private Long storeId;

    @Schema(description = "门店名称")
    private String storeName;

    @Schema(description = "年份")
    private Integer year;

    @Schema(description = "月份")
    private Integer month;

    @Schema(description = "总消耗数量")
    private Integer totalQuantity;

    @Schema(description = "总成本")
    private BigDecimal totalCost;

    @Schema(description = "消耗明细列表")
    private List<ConsumptionVO> details;
}