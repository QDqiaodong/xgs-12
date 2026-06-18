package com.retail.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "调拨出库请求")
public class TransferOutboundDTO {

    @Schema(description = "出库明细列表")
    private List<OutboundItemDTO> items;
}