package com.retail.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "耗材更新请求")
public class MaterialUpdateDTO {

    @Schema(description = "耗材名称")
    private String materialName;

    @Schema(description = "耗材分类：packaging/cleaning/equipment")
    private String category;

    @Schema(description = "规格型号")
    private String specification;

    @Schema(description = "计量单位")
    private String unit;

    @Schema(description = "采购成本")
    private BigDecimal purchaseCost;

    @Schema(description = "保质期（天）")
    private Integer shelfLife;

    @Schema(description = "货架位置")
    private String shelfLocation;

    @Schema(description = "安全库存")
    private Integer safetyStock;

    @Schema(description = "当前库存")
    private Integer currentStock;

    @Schema(description = "状态：0禁用 1正常 2预警 3过期")
    private Integer status;
}