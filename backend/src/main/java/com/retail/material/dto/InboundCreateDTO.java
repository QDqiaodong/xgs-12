package com.retail.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "入库单创建请求")
public class InboundCreateDTO {

    @NotNull(message = "目标门店ID不能为空")
    @Schema(description = "目标门店ID", required = true)
    private Long targetStoreId;

    @Schema(description = "调出门店ID（总仓为0）")
    private Long sourceStoreId = 0L;

    @Schema(description = "备注")
    private String remark;

    @NotEmpty(message = "入库明细不能为空")
    @Schema(description = "入库明细列表", required = true)
    private List<InboundItemDTO> items;
}