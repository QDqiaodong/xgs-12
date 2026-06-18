package com.retail.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "调拨申请创建请求")
public class TransferCreateDTO {

    @NotNull(message = "调出门店ID不能为空")
    @Schema(description = "调出门店ID", required = true)
    private Long sourceStoreId;

    @NotNull(message = "目标门店ID不能为空")
    @Schema(description = "目标门店ID", required = true)
    private Long targetStoreId;

    @Schema(description = "备注")
    private String remark;

    @NotEmpty(message = "调拨明细不能为空")
    @Schema(description = "调拨明细列表", required = true)
    private List<TransferItemDTO> items;
}