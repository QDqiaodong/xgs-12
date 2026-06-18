package com.retail.material.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "盘点明细响应")
public class InventoryDetailVO {

    @Schema(description = "明细ID")
    private Long id;

    @Schema(description = "耗材ID")
    private Long materialId;

    @Schema(description = "耗材名称")
    private String materialName;

    @Schema(description = "规格型号")
    private String specification;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "系统数量")
    private Integer systemQuantity;

    @Schema(description = "实盘数量")
    private Integer actualQuantity;

    @Schema(description = "差异数量")
    private Integer difference;

    @Schema(description = "单价")
    private BigDecimal unitCost;

    @Schema(description = "差异成本")
    private BigDecimal differenceCost;

    @Schema(description = "差异原因")
    private String differenceReason;
}