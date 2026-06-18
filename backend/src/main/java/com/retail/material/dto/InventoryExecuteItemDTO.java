package com.retail.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "盘点执行明细项")
public class InventoryExecuteItemDTO {

    @NotNull(message = "耗材ID不能为空")
    @Schema(description = "耗材ID", required = true)
    private Long materialId;

    @Schema(description = "实盘数量")
    private Integer actualQuantity;

    @Schema(description = "差异原因")
    private String differenceReason;
}