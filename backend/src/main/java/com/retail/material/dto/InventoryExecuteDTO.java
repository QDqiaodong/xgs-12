package com.retail.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "盘点执行请求")
public class InventoryExecuteDTO {

    @NotNull(message = "盘点计划ID不能为空")
    @Schema(description = "盘点计划ID", required = true)
    private Long planId;

    @Schema(description = "盘点明细列表")
    private List<InventoryExecuteItemDTO> items;
}