package com.retail.material.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "仪表盘响应")
public class DashboardVO {

    @Schema(description = "总门店数")
    private Long totalStores;

    @Schema(description = "总耗材种类数")
    private Long totalMaterials;

    @Schema(description = "库存总价值")
    private BigDecimal totalStockValue;

    @Schema(description = "本月消耗总额")
    private BigDecimal monthlyConsumptionCost;

    @Schema(description = "本月消耗数量")
    private Integer monthlyConsumptionQuantity;

    @Schema(description = "本月调拨单数")
    private Long monthlyTransferCount;

    @Schema(description = "待审批调拨单数")
    private Long pendingApprovalCount;

    @Schema(description = "待出库调拨单数")
    private Long pendingOutboundCount;

    @Schema(description = "待签收调拨单数")
    private Long pendingReceiveCount;

    @Schema(description = "库存预警数")
    private Long stockAlertCount;

    @Schema(description = "进行中盘点计划数")
    private Long ongoingInventoryCount;

    @Schema(description = "月度消耗趋势（月份-金额）")
    private List<Map<String, Object>> consumptionTrend;

    @Schema(description = "库存分类统计")
    private List<Map<String, Object>> stockByCategory;

    @Schema(description = "门店库存排名")
    private List<Map<String, Object>> storeStockRanking;

    @Schema(description = "库存预警列表")
    private List<StockAlertVO> stockAlerts;
}
