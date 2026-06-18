package com.retail.material.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "消耗报表响应")
public class ConsumptionReportVO {

    @Schema(description = "门店ID")
    private Long storeId;

    @Schema(description = "门店名称")
    private String storeName;

    @Schema(description = "耗材ID")
    private Long materialId;

    @Schema(description = "耗材名称")
    private String materialName;

    @Schema(description = "规格型号")
    private String specification;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "消耗数量")
    private Integer quantity;

    @Schema(description = "消耗成本")
    private BigDecimal totalCost;

    @Schema(description = "年份")
    private Integer year;

    @Schema(description = "月份")
    private Integer month;
}