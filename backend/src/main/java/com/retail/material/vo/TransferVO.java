package com.retail.material.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "调拨单响应")
public class TransferVO {

    @Schema(description = "调拨单ID")
    private Long id;

    @Schema(description = "调拨单号")
    private String orderNo;

    @Schema(description = "调出门店ID")
    private Long sourceStoreId;

    @Schema(description = "调出门店名称")
    private String sourceStoreName;

    @Schema(description = "目标门店ID")
    private Long targetStoreId;

    @Schema(description = "目标门店名称")
    private String targetStoreName;

    @Schema(description = "状态：0待审批 1已审批 2已出库 3在途 4已签收 5已驳回")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "总成本")
    private BigDecimal totalCost;

    @Schema(description = "申请人ID")
    private Long applicantId;

    @Schema(description = "申请人姓名")
    private String applicantName;

    @Schema(description = "申请时间")
    private LocalDateTime applyTime;

    @Schema(description = "审批人ID")
    private Long approverId;

    @Schema(description = "审批人姓名")
    private String approverName;

    @Schema(description = "审批时间")
    private LocalDateTime approveTime;

    @Schema(description = "出库时间")
    private LocalDateTime outboundTime;

    @Schema(description = "签收时间")
    private LocalDateTime receiveTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "调拨明细列表")
    private List<TransferItemVO> items;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}