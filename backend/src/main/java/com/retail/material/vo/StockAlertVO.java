package com.retail.material.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "库存预警响应")
public class StockAlertVO {

    @Schema(description = "耗材ID")
    private Long materialId;

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

    @Schema(description = "安全库存")
    private Integer safetyStock;

    @Schema(description = "当前库存")
    private Integer currentStock;

    @Schema(description = "预警类型：0库存不足 1临期 2过期")
    private Integer alertType;

    @Schema(description = "预警消息")
    private String alertMessage;
}