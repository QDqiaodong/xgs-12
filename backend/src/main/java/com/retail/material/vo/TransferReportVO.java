package com.retail.material.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "调拨报表响应")
public class TransferReportVO {

    @Schema(description = "调拨单ID")
    private Long id;

    @Schema(description = "调拨单号")
    private String orderNo;

    @Schema(description = "调出门店名称")
    private String sourceStoreName;

    @Schema(description = "目标门店名称")
    private String targetStoreName;

    @Schema(description = "调拨数量")
    private Integer totalQuantity;

    @Schema(description = "调拨成本")
    private BigDecimal totalCost;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "申请时间")
    private String applyTime;

    @Schema(description = "完成时间")
    private String receiveTime;
}