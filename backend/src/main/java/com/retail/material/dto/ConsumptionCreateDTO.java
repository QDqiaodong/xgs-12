package com.retail.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "消耗登记请求")
public class ConsumptionCreateDTO {

    @NotNull(message = "门店ID不能为空")
    @Schema(description = "门店ID", required = true)
    private Long storeId;

    @NotNull(message = "耗材ID不能为空")
    @Schema(description = "耗材ID", required = true)
    private Long materialId;

    @NotNull(message = "消耗数量不能为空")
    @Min(value = 1, message = "消耗数量必须大于0")
    @Schema(description = "消耗数量", required = true)
    private Integer quantity;

    @Schema(description = "单价")
    private BigDecimal unitCost;

    @Schema(description = "备注")
    private String remark;
}