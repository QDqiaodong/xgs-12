package com.retail.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "入库单查询请求")
public class InboundQueryDTO {

    @Schema(description = "入库单号")
    private String orderNo;

    @Schema(description = "目标门店ID")
    private Long targetStoreId;

    @Schema(description = "状态：0待审批 1已审批 2已出库 3在途 4已签收 5已驳回")
    private Integer status;

    @Schema(description = "当前页", example = "1")
    private Integer current = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer size = 10;
}