package com.retail.material.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "入库明细响应")
public class InboundItemVO {

    @Schema(description = "明细ID")
    private Long id;

    @Schema(description = "耗材ID")
    private Long materialId;

    @Schema(description = "耗材名称")
    private String materialName;

    @Schema(description = "规格型号")
    private String specification;

    @Schema(description = "计量单位")
    private String unit;

    @Schema(description = "申请数量")
    private Integer requestQuantity;

    @Schema(description = "审批数量")
    private Integer approvedQuantity;

    @Schema(description = "实收数量")
    private Integer actualQuantity;

    @Schema(description = "单位成本")
    private BigDecimal unitCost;

    @Schema(description = "总成本")
    private BigDecimal totalCost;

    @Schema(description = "差异数量")
    private Integer difference;

    @Schema(description = "差异原因")
    private String differenceReason;
}