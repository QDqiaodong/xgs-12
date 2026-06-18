package com.retail.material.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "库存报表响应")
public class StockReportVO {

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

    @Schema(description = "库存数量")
    private Integer quantity;

    @Schema(description = "锁定数量")
    private Integer lockedQuantity;

    @Schema(description = "可用数量")
    private Integer availableQuantity;

    @Schema(description = "平均成本")
    private BigDecimal avgCost;

    @Schema(description = "库存价值")
    private BigDecimal stockValue;

    @Schema(description = "安全库存")
    private Integer safetyStock;

    @Schema(description = "库存状态：正常/预警/短缺")
    private String stockStatus;
}