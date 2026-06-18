package com.retail.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "调拨明细项")
public class TransferItemDTO {

    @NotNull(message = "耗材ID不能为空")
    @Schema(description = "耗材ID", required = true)
    private Long materialId;

    @NotNull(message = "申请数量不能为空")
    @Min(value = 1, message = "申请数量必须大于0")
    @Schema(description = "申请数量", required = true)
    private Integer requestQuantity;

    @Schema(description = "单价")
    private BigDecimal unitCost;
}