package com.retail.material.service;

import com.retail.material.dto.ReportQueryDTO;
import com.retail.material.vo.ConsumptionReportVO;
import com.retail.material.vo.DashboardVO;
import com.retail.material.vo.StockReportVO;
import com.retail.material.vo.TransferReportVO;

import java.util.List;

public interface ReportService {

    List<StockReportVO> getStockReport(ReportQueryDTO queryDTO);

    List<ConsumptionReportVO> getConsumptionReport(ReportQueryDTO queryDTO);

    List<TransferReportVO> getTransferReport(ReportQueryDTO queryDTO);

    DashboardVO getDashboardData();
}
