package com.retail.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "签收明细项")
public class ReceiveItemDTO {

    @NotNull(message = "调拨明细ID不能为空")
    @Schema(description = "调拨明细ID", required = true)
    private Long transferItemId;

    @NotNull(message = "耗材ID不能为空")
    @Schema(description = "耗材ID", required = true)
    private Long materialId;

    @Schema(description = "签收数量")
    private Integer receiveQuantity;

    @Schema(description = "差异原因")
    private String differenceReason;
}