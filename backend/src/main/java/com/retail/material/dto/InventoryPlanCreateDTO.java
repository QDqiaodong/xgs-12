package com.retail.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "盘点计划创建请求")
public class InventoryPlanCreateDTO {

    @NotNull(message = "门店ID不能为空")
    @Schema(description = "门店ID", required = true)
    private Long storeId;

    @NotNull(message = "盘点日期不能为空")
    @Schema(description = "盘点日期", required = true)
    private LocalDateTime planDate;

    @Schema(description = "备注")
    private String remark;
}