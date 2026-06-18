package com.retail.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "调拨单查询请求")
public class TransferQueryDTO {

    @Schema(description = "当前页码", defaultValue = "1")
    private Long current = 1L;

    @Schema(description = "每页大小", defaultValue = "10")
    private Long size = 10L;

    @Schema(description = "调拨单号")
    private String orderNo;

    @Schema(description = "调出门店ID")
    private Long sourceStoreId;

    @Schema(description = "目标门店ID")
    private Long targetStoreId;

    @Schema(description = "状态：0待审批 1已审批 2已出库 3在途 4已签收 5已驳回")
    private Integer status;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}
