package com.retail.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "耗材创建请求")
public class MaterialCreateDTO {

    @NotBlank(message = "耗材编码不能为空")
    @Schema(description = "耗材编码", required = true)
    private String materialCode;

    @NotBlank(message = "耗材名称不能为空")
    @Schema(description = "耗材名称", required = true)
    private String materialName;

    @NotBlank(message = "耗材分类不能为空")
    @Schema(description = "耗材分类：packaging/cleaning/equipment", required = true)
    private String category;

    @Schema(description = "规格型号")
    private String specification;

    @NotBlank(message = "计量单位不能为空")
    @Schema(description = "计量单位", required = true)
    private String unit;

    @NotNull(message = "采购成本不能为空")
    @Schema(description = "采购成本", required = true)
    private BigDecimal purchaseCost;

    @Schema(description = "保质期（天）")
    private Integer shelfLife;

    @Schema(description = "货架位置")
    private String shelfLocation;

    @Schema(description = "安全库存")
    private Integer safetyStock = 0;

    @Schema(description = "当前库存")
    private Integer currentStock = 0;

    @Schema(description = "状态：0禁用 1正常 2预警 3过期")
    private Integer status = 1;
}