package com.retail.material.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.retail.material.dto.ReportQueryDTO;
import com.retail.material.entity.*;
import com.retail.material.mapper.*;
import com.retail.material.service.ReportService;
import com.retail.material.vo.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private final StoreInventoryMapper storeInventoryMapper;
    private final ConsumptionMapper consumptionMapper;
    private final TransferOrderMapper transferOrderMapper;
    private final TransferItemMapper transferItemMapper;
    private final StoreMapper storeMapper;
    private final MaterialMapper materialMapper;
    private final InventoryPlanMapper inventoryPlanMapper;

    public ReportServiceImpl(StoreInventoryMapper storeInventoryMapper,
                             ConsumptionMapper consumptionMapper,
                             TransferOrderMapper transferOrderMapper,
                             TransferItemMapper transferItemMapper,
                             StoreMapper storeMapper,
                             MaterialMapper materialMapper,
                             InventoryPlanMapper inventoryPlanMapper) {
        this.storeInventoryMapper = storeInventoryMapper;
        this.consumptionMapper = consumptionMapper;
        this.transferOrderMapper = transferOrderMapper;
        this.transferItemMapper = transferItemMapper;
        this.storeMapper = storeMapper;
        this.materialMapper = materialMapper;
        this.inventoryPlanMapper = inventoryPlanMapper;
    }

    @Override
    public List<StockReportVO> getStockReport(ReportQueryDTO queryDTO) {
        List<StoreInventory> inventories;

        if (queryDTO.getStoreId() != null) {
            inventories = storeInventoryMapper.selectByStoreId(queryDTO.getStoreId());
        } else if (queryDTO.getMaterialId() != null) {
            inventories = storeInventoryMapper.selectByMaterialId(queryDTO.getMaterialId());
        } else {
            inventories = storeInventoryMapper.selectList(null);
        }

        if (StringUtils.hasText(queryDTO.getCategory())) {
            List<Long> materialIds = materialMapper.selectList(
                    new LambdaQueryWrapper<Material>().eq(Material::getCategory, queryDTO.getCategory())
            ).stream().map(Material::getId).collect(Collectors.toList());
            inventories = inventories.stream()
                    .filter(inv -> materialIds.contains(inv.getMaterialId()))
                    .collect(Collectors.toList());
        }

        List<StockReportVO> voList = new ArrayList<>();
        for (StoreInventory inventory : inventories) {
            StockReportVO vo = convertToStockReportVO(inventory);
            voList.add(vo);
        }

        return voList;
    }

    @Override
    public List<ConsumptionReportVO> getConsumptionReport(ReportQueryDTO queryDTO) {
        LambdaQueryWrapper<Consumption> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getStoreId() != null) {
            wrapper.eq(Consumption::getStoreId, queryDTO.getStoreId());
        }
        if (queryDTO.getYear() != null) {
            wrapper.eq(Consumption::getYear, queryDTO.getYear());
        }
        if (queryDTO.getMonth() != null) {
            wrapper.eq(Consumption::getMonth, queryDTO.getMonth());
        }
        if (queryDTO.getMaterialId() != null) {
            wrapper.eq(Consumption::getMaterialId, queryDTO.getMaterialId());
        }

        wrapper.orderByDesc(Consumption::getYear, Consumption::getMonth, Consumption::getCreateTime);

        List<Consumption> consumptions = consumptionMapper.selectList(wrapper);

        if (StringUtils.hasText(queryDTO.getCategory())) {
            List<Long> materialIds = materialMapper.selectList(
                    new LambdaQueryWrapper<Material>().eq(Material::getCategory, queryDTO.getCategory())
            ).stream().map(Material::getId).collect(Collectors.toList());
            consumptions = consumptions.stream()
                    .filter(c -> materialIds.contains(c.getMaterialId()))
                    .collect(Collectors.toList());
        }

        List<ConsumptionReportVO> voList = new ArrayList<>();
        for (Consumption consumption : consumptions) {
            ConsumptionReportVO vo = BeanUtil.copyProperties(consumption, ConsumptionReportVO.class);

            Store store = storeMapper.selectById(consumption.getStoreId());
            if (store != null) {
                vo.setStoreName(store.getStoreName());
            }

            Material material = materialMapper.selectById(consumption.getMaterialId());
            if (material != null) {
                vo.setMaterialName(material.getMaterialName());
                vo.setSpecification(material.getSpecification());
                vo.setUnit(material.getUnit());
            }

            voList.add(vo);
        }

        return voList;
    }

    @Override
    public List<TransferReportVO> getTransferReport(ReportQueryDTO queryDTO) {
        LambdaQueryWrapper<TransferOrder> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getStoreId() != null) {
            wrapper.and(w -> w.eq(TransferOrder::getSourceStoreId, queryDTO.getStoreId())
                    .or().eq(TransferOrder::getTargetStoreId, queryDTO.getStoreId()));
        }
        if (StringUtils.hasText(queryDTO.getStartDate())) {
            wrapper.ge(TransferOrder::getApplyTime, queryDTO.getStartDate());
        }
        if (StringUtils.hasText(queryDTO.getEndDate())) {
            wrapper.le(TransferOrder::getApplyTime, queryDTO.getEndDate() + " 23:59:59");
        }

        wrapper.orderByDesc(TransferOrder::getApplyTime);

        List<TransferOrder> orders = transferOrderMapper.selectList(wrapper);

        List<TransferReportVO> voList = new ArrayList<>();
        for (TransferOrder order : orders) {
            TransferReportVO vo = new TransferReportVO();
            vo.setId(order.getId());
            vo.setOrderNo(order.getOrderNo());
            vo.setTotalCost(order.getTotalCost());
            vo.setStatusDesc(getStatusDesc(order.getStatus()));
            vo.setApplyTime(DateUtil.format(order.getApplyTime(), "yyyy-MM-dd HH:mm:ss"));
            if (order.getReceiveTime() != null) {
                vo.setReceiveTime(DateUtil.format(order.getReceiveTime(), "yyyy-MM-dd HH:mm:ss"));
            }

            if (order.getSourceStoreId() != null && order.getSourceStoreId() > 0) {
                Store sourceStore = storeMapper.selectById(order.getSourceStoreId());
                if (sourceStore != null) {
                    vo.setSourceStoreName(sourceStore.getStoreName());
                }
            } else {
                vo.setSourceStoreName("总部总仓");
            }

            Store targetStore = storeMapper.selectById(order.getTargetStoreId());
            if (targetStore != null) {
                vo.setTargetStoreName(targetStore.getStoreName());
            }

            List<TransferItem> items = transferItemMapper.selectByOrderId(order.getId());
            int totalQuantity = items.stream()
                    .mapToInt(item -> item.getActualQuantity() != null ? item.getActualQuantity() : 0)
                    .sum();
            vo.setTotalQuantity(totalQuantity);

            voList.add(vo);
        }

        return voList;
    }

    @Override
    public DashboardVO getDashboardData() {
        DashboardVO vo = new DashboardVO();

        Long totalStores = storeMapper.selectCount(null);
        vo.setTotalStores(totalStores);

        Long totalMaterials = materialMapper.selectCount(null);
        vo.setTotalMaterials(totalMaterials);

        List<StoreInventory> inventories = storeInventoryMapper.selectList(null);
        BigDecimal totalStockValue = inventories.stream()
                .map(inv -> inv.getAvgCost() != null && inv.getQuantity() != null ?
                        inv.getAvgCost().multiply(BigDecimal.valueOf(inv.getQuantity())) : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalStockValue(totalStockValue);

        LocalDateTime now = LocalDateTime.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        BigDecimal monthlyConsumptionCost = consumptionMapper.sumCostByStoreAndMonth(null, currentYear, currentMonth);
        vo.setMonthlyConsumptionCost(monthlyConsumptionCost != null ? monthlyConsumptionCost : BigDecimal.ZERO);

        Integer monthlyConsumptionQuantity = consumptionMapper.sumQuantityByStoreAndMonth(null, currentYear, currentMonth);
        vo.setMonthlyConsumptionQuantity(monthlyConsumptionQuantity != null ? monthlyConsumptionQuantity : 0);

        LocalDateTime monthStart = LocalDateTime.of(currentYear, currentMonth, 1, 0, 0);
        Long monthlyTransferCount = transferOrderMapper.selectCount(
                new LambdaQueryWrapper<TransferOrder>().ge(TransferOrder::getApplyTime, monthStart)
        );
        vo.setMonthlyTransferCount(monthlyTransferCount);

        Long pendingApprovalCount = transferOrderMapper.selectCount(
                new LambdaQueryWrapper<TransferOrder>().eq(TransferOrder::getStatus, 0)
        );
        vo.setPendingApprovalCount(pendingApprovalCount);

        Long pendingOutboundCount = transferOrderMapper.selectCount(
                new LambdaQueryWrapper<TransferOrder>().eq(TransferOrder::getStatus, 1)
        );
        vo.setPendingOutboundCount(pendingOutboundCount);

        Long pendingReceiveCount = transferOrderMapper.selectCount(
                new LambdaQueryWrapper<TransferOrder>().eq(TransferOrder::getStatus, 2)
        );
        vo.setPendingReceiveCount(pendingReceiveCount);

        List<StockAlertVO> stockAlerts = getStockAlerts();
        vo.setStockAlerts(stockAlerts);
        vo.setStockAlertCount((long) stockAlerts.size());

        Long ongoingInventoryCount = inventoryPlanMapper.selectCount(
                new LambdaQueryWrapper<InventoryPlan>().eq(InventoryPlan::getStatus, 1)
        );
        vo.setOngoingInventoryCount(ongoingInventoryCount);

        vo.setConsumptionTrend(getConsumptionTrend());
        vo.setStockByCategory(getStockByCategory());
        vo.setStoreStockRanking(getStoreStockRanking());

        return vo;
    }

    private List<StockAlertVO> getStockAlerts() {
        List<StockAlertVO> alerts = new ArrayList<>();
        List<Material> materials = materialMapper.selectList(null);

        for (Material material : materials) {
            if (material.getSafetyStock() != null && material.getCurrentStock() != null
                    && material.getCurrentStock() < material.getSafetyStock()) {
                StockAlertVO alert = BeanUtil.copyProperties(material, StockAlertVO.class);
                alert.setAlertType(0);
                alert.setAlertMessage("库存不足，当前库存: " + material.getCurrentStock() +
                        "，安全库存: " + material.getSafetyStock());
                alerts.add(alert);
            }
        }

        return alerts;
    }

    private List<Map<String, Object>> getConsumptionTrend() {
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 5; i >= 0; i--) {
            LocalDateTime date = now.minusMonths(i);
            int year = date.getYear();
            int month = date.getMonthValue();

            BigDecimal cost = consumptionMapper.sumCostByStoreAndMonth(null, year, month);
            Integer quantity = consumptionMapper.sumQuantityByStoreAndMonth(null, year, month);

            Map<String, Object> item = new HashMap<>();
            item.put("month", year + "-" + String.format("%02d", month));
            item.put("cost", cost != null ? cost : BigDecimal.ZERO);
            item.put("quantity", quantity != null ? quantity : 0);
            trend.add(item);
        }

        return trend;
    }

    private List<Map<String, Object>> getStockByCategory() {
        List<Material> materials = materialMapper.selectList(null);
        Map<String, BigDecimal> categoryValueMap = new HashMap<>();
        Map<String, Integer> categoryCountMap = new HashMap<>();

        for (Material material : materials) {
            String category = material.getCategory() != null ? material.getCategory() : "未分类";
            List<StoreInventory> inventories = storeInventoryMapper.selectByMaterialId(material.getId());

            BigDecimal value = inventories.stream()
                    .map(inv -> inv.getAvgCost() != null && inv.getQuantity() != null ?
                            inv.getAvgCost().multiply(BigDecimal.valueOf(inv.getQuantity())) : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int count = inventories.stream()
                    .mapToInt(inv -> inv.getQuantity() != null ? inv.getQuantity() : 0)
                    .sum();

            categoryValueMap.put(category, categoryValueMap.getOrDefault(category, BigDecimal.ZERO).add(value));
            categoryCountMap.put(category, categoryCountMap.getOrDefault(category, 0) + count);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : categoryValueMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("category", entry.getKey());
            item.put("value", entry.getValue());
            item.put("quantity", categoryCountMap.get(entry.getKey()));
            result.add(item);
        }

        result.sort((a, b) -> ((BigDecimal) b.get("value")).compareTo((BigDecimal) a.get("value")));
        return result;
    }

    private List<Map<String, Object>> getStoreStockRanking() {
        List<Store> stores = storeMapper.selectList(null);
        List<Map<String, Object>> ranking = new ArrayList<>();

        for (Store store : stores) {
            List<StoreInventory> inventories = storeInventoryMapper.selectByStoreId(store.getId());

            BigDecimal value = inventories.stream()
                    .map(inv -> inv.getAvgCost() != null && inv.getQuantity() != null ?
                            inv.getAvgCost().multiply(BigDecimal.valueOf(inv.getQuantity())) : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int count = inventories.stream()
                    .mapToInt(inv -> inv.getQuantity() != null ? inv.getQuantity() : 0)
                    .sum();

            Map<String, Object> item = new HashMap<>();
            item.put("storeId", store.getId());
            item.put("storeName", store.getStoreName());
            item.put("stockValue", value);
            item.put("stockQuantity", count);
            ranking.add(item);
        }

        ranking.sort((a, b) -> ((BigDecimal) b.get("stockValue")).compareTo((BigDecimal) a.get("stockValue")));
        return ranking.size() > 10 ? ranking.subList(0, 10) : ranking;
    }

    private StockReportVO convertToStockReportVO(StoreInventory inventory) {
        StockReportVO vo = BeanUtil.copyProperties(inventory, StockReportVO.class);

        Store store = storeMapper.selectById(inventory.getStoreId());
        if (store != null) {
            vo.setStoreName(store.getStoreName());
        }

        Material material = materialMapper.selectById(inventory.getMaterialId());
        if (material != null) {
            vo.setMaterialName(material.getMaterialName());
            vo.setSpecification(material.getSpecification());
            vo.setUnit(material.getUnit());
            vo.setSafetyStock(material.getSafetyStock());

            if (material.getSafetyStock() != null && inventory.getQuantity() != null) {
                if (inventory.getQuantity() <= 0) {
                    vo.setStockStatus("短缺");
                } else if (inventory.getQuantity() < material.getSafetyStock()) {
                    vo.setStockStatus("预警");
                } else {
                    vo.setStockStatus("正常");
                }
            }
        }

        if (inventory.getQuantity() != null && inventory.getLockedQuantity() != null) {
            vo.setAvailableQuantity(inventory.getQuantity() - inventory.getLockedQuantity());
        }

        if (inventory.getAvgCost() != null && inventory.getQuantity() != null) {
            vo.setStockValue(inventory.getAvgCost().multiply(BigDecimal.valueOf(inventory.getQuantity())));
        }

        return vo;
    }

    private String getStatusDesc(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待审批";
            case 1: return "已审批";
            case 2: return "已出库";
            case 3: return "在途";
            case 4: return "已签收";
            case 5: return "已驳回";
            default: return "未知";
        }
    }
}
