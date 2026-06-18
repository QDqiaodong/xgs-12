package com.retail.material.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "门店库存响应")
public class StoreInventoryVO {

    @Schema(description = "库存ID")
    private Long id;

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
}