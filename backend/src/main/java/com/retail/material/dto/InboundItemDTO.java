package com.retail.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "入库明细项")
public class InboundItemDTO {

    @Schema(description = "耗材ID", required = true)
    private Long materialId;

    @Schema(description = "耗材名称", required = true)
    private String materialName;

    @Schema(description = "规格型号")
    private String specification;

    @Schema(description = "计量单位", required = true)
    private String unit;

    @Schema(description = "申请数量", required = true)
    private Integer requestQuantity;

    @Schema(description = "单位成本", required = true)
    private BigDecimal unitCost;
}