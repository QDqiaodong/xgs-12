package com.retail.material.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "盘点差异分析响应")
public class InventoryAnalysisVO {

    @Schema(description = "盘点计划ID")
    private Long planId;

    @Schema(description = "盘点计划编号")
    private String planNo;

    @Schema(description = "门店名称")
    private String storeName;

    @Schema(description = "盘点日期")
    private String planDate;

    @Schema(description = "总盘点项目数")
    private Integer totalItems;

    @Schema(description = "盘盈项目数")
    private Integer gainItems;

    @Schema(description = "盘亏项目数")
    private Integer lossItems;

    @Schema(description = "正常项目数")
    private Integer normalItems;

    @Schema(description = "盘盈总金额")
    private BigDecimal totalGainCost;

    @Schema(description = "盘亏总金额")
    private BigDecimal totalLossCost;

    @Schema(description = "差异明细列表")
    private List<InventoryDetailVO> differenceDetails;
}