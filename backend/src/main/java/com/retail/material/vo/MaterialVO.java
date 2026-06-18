package com.retail.material.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "耗材信息响应")
public class MaterialVO {

    @Schema(description = "耗材ID")
    private Long id;

    @Schema(description = "耗材编码")
    private String materialCode;

    @Schema(description = "耗材名称")
    private String materialName;

    @Schema(description = "耗材分类")
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

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}