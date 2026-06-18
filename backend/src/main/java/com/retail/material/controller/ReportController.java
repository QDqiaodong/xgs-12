package com.retail.material.controller;

import com.retail.material.common.Result;
import com.retail.material.dto.ReportQueryDTO;
import com.retail.material.service.ReportService;
import com.retail.material.vo.ConsumptionReportVO;
import com.retail.material.vo.DashboardVO;
import com.retail.material.vo.StockReportVO;
import com.retail.material.vo.TransferReportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "报表管理", description = "报表相关接口")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "库存报表")
    @GetMapping("/stock")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STORE_MANAGER','STAFF')")
    public Result<List<StockReportVO>> getStockReport(ReportQueryDTO queryDTO) {
        return Result.success(reportService.getStockReport(queryDTO));
    }

    @Operation(summary = "消耗报表")
    @GetMapping("/consumption")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STORE_MANAGER','STAFF')")
    public Result<List<ConsumptionReportVO>> getConsumptionReport(ReportQueryDTO queryDTO) {
        return Result.success(reportService.getConsumptionReport(queryDTO));
    }

    @Operation(summary = "调拨报表")
    @GetMapping("/transfer")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STORE_MANAGER','STAFF')")
    public Result<List<TransferReportVO>> getTransferReport(ReportQueryDTO queryDTO) {
        return Result.success(reportService.getTransferReport(queryDTO));
    }

    @Operation(summary = "首页仪表盘数据")
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STORE_MANAGER','STAFF')")
    public Result<DashboardVO> getDashboardData() {
        return Result.success(reportService.getDashboardData());
    }
}
