package com.retail.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "调拨签收请求")
public class TransferReceiveDTO {

    @Schema(description = "签收明细列表")
    private List<ReceiveItemDTO> items;

    @Schema(description = "签收备注")
    private String remark;
}