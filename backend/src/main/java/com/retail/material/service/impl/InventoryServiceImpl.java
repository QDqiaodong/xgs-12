package com.retail.material.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.retail.material.common.PageResult;
import com.retail.material.dto.InventoryExecuteDTO;
import com.retail.material.dto.InventoryExecuteItemDTO;
import com.retail.material.dto.InventoryPlanCreateDTO;
import com.retail.material.dto.InventoryPlanQueryDTO;
import com.retail.material.entity.*;
import com.retail.material.exception.BusinessException;
import com.retail.material.mapper.*;
import com.retail.material.service.CacheService;
import com.retail.material.service.InventoryService;
import com.retail.material.utils.SecurityUtils;
import com.retail.material.vo.InventoryAnalysisVO;
import com.retail.material.vo.InventoryDetailVO;
import com.retail.material.vo.InventoryPlanVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryPlanMapper inventoryPlanMapper;
    private final InventoryDetailMapper inventoryDetailMapper;
    private final StoreInventoryMapper storeInventoryMapper;
    private final StoreMapper storeMapper;
    private final MaterialMapper materialMapper;
    private final SysUserMapper sysUserMapper;
    private final CacheService cacheService;

    public InventoryServiceImpl(InventoryPlanMapper inventoryPlanMapper,
                                InventoryDetailMapper inventoryDetailMapper,
                                StoreInventoryMapper storeInventoryMapper,
                                StoreMapper storeMapper,
                                MaterialMapper materialMapper,
                                SysUserMapper sysUserMapper,
                                CacheService cacheService) {
        this.inventoryPlanMapper = inventoryPlanMapper;
        this.inventoryDetailMapper = inventoryDetailMapper;
        this.storeInventoryMapper = storeInventoryMapper;
        this.storeMapper = storeMapper;
        this.materialMapper = materialMapper;
        this.sysUserMapper = sysUserMapper;
        this.cacheService = cacheService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createInventoryPlan(InventoryPlanCreateDTO createDTO) {
        Store store = storeMapper.selectById(createDTO.getStoreId());
        if (store == null) {
            throw BusinessException.of("门店不存在");
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw BusinessException.of(401, "未登录");
        }

        InventoryPlan existingPlan = inventoryPlanMapper.selectLatestByStoreAndStatus(createDTO.getStoreId(), 0);
        if (existingPlan != null) {
            throw BusinessException.of("该门店存在待执行的盘点计划，请先完成");
        }

        existingPlan = inventoryPlanMapper.selectLatestByStoreAndStatus(createDTO.getStoreId(), 1);
        if (existingPlan != null) {
            throw BusinessException.of("该门店存在进行中的盘点计划，请先完成");
        }

        String planNo = generatePlanNo();

        InventoryPlan plan = new InventoryPlan();
        plan.setPlanNo(planNo);
        plan.setStoreId(createDTO.getStoreId());
        plan.setStatus(0);
        plan.setPlanDate(createDTO.getPlanDate());
        plan.setCreatorId(currentUserId);
        plan.setRemark(createDTO.getRemark());

        inventoryPlanMapper.insert(plan);

        return plan.getId();
    }

    @Override
    public PageResult<InventoryPlanVO> listInventoryPlans(InventoryPlanQueryDTO queryDTO) {
        LambdaQueryWrapper<InventoryPlan> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getStoreId() != null) {
            wrapper.eq(InventoryPlan::getStoreId, queryDTO.getStoreId());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(InventoryPlan::getStatus, queryDTO.getStatus());
        }

        wrapper.orderByDesc(InventoryPlan::getCreateTime);

        IPage<InventoryPlan> page = inventoryPlanMapper.selectPage(
                new Page<>(queryDTO.getCurrent(), queryDTO.getSize()),
                wrapper
        );

        List<InventoryPlanVO> voList = new ArrayList<>();
        for (InventoryPlan plan : page.getRecords()) {
            voList.add(convertToVO(plan));
        }

        return PageResult.of(voList, page.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInventoryPlan(Long id, InventoryPlanCreateDTO updateDTO) {
        InventoryPlan plan = inventoryPlanMapper.selectById(id);
        if (plan == null) {
            throw BusinessException.of("盘点计划不存在");
        }
        if (plan.getStatus() != 0) {
            throw BusinessException.of("只能修改待执行状态的盘点计划");
        }

        Store store = storeMapper.selectById(updateDTO.getStoreId());
        if (store == null) {
            throw BusinessException.of("门店不存在");
        }

        plan.setStoreId(updateDTO.getStoreId());
        plan.setPlanDate(updateDTO.getPlanDate());
        plan.setRemark(updateDTO.getRemark());

        inventoryPlanMapper.updateById(plan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeInventory(InventoryExecuteDTO executeDTO) {
        InventoryPlan plan = inventoryPlanMapper.selectById(executeDTO.getPlanId());
        if (plan == null) {
            throw BusinessException.of("盘点计划不存在");
        }
        if (plan.getStatus() != 0 && plan.getStatus() != 1) {
            throw BusinessException.of("盘点计划状态不正确");
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw BusinessException.of(401, "未登录");
        }

        if (plan.getStatus() == 0) {
            plan.setStatus(1);
            plan.setStartTime(LocalDateTime.now());
            inventoryPlanMapper.updateById(plan);

            List<StoreInventory> inventories = storeInventoryMapper.selectByStoreId(plan.getStoreId());
            for (StoreInventory inventory : inventories) {
                Material material = materialMapper.selectById(inventory.getMaterialId());
                if (material == null) continue;

                InventoryDetail detail = new InventoryDetail();
                detail.setPlanId(plan.getId());
                detail.setMaterialId(inventory.getMaterialId());
                detail.setMaterialName(material.getMaterialName());
                detail.setSpecification(material.getSpecification());
                detail.setUnit(material.getUnit());
                detail.setSystemQuantity(inventory.getQuantity());
                detail.setActualQuantity(0);
                detail.setDifference(0);
                detail.setUnitCost(inventory.getAvgCost());
                detail.setDifferenceCost(BigDecimal.ZERO);

                inventoryDetailMapper.insert(detail);
            }

            cacheService.cacheMaterialParams(plan.getStoreId());
        }

        List<InventoryDetail> details = inventoryDetailMapper.selectByPlanId(plan.getId());
        Map<Long, InventoryExecuteItemDTO> executeItemMap = executeDTO.getItems().stream()
                .collect(Collectors.toMap(InventoryExecuteItemDTO::getMaterialId, item -> item));

        for (InventoryDetail detail : details) {
            InventoryExecuteItemDTO executeItem = executeItemMap.get(detail.getMaterialId());
            if (executeItem != null) {
                detail.setActualQuantity(executeItem.getActualQuantity());
                detail.setDifference(executeItem.getActualQuantity() - detail.getSystemQuantity());
                detail.setDifferenceCost(detail.getUnitCost().multiply(BigDecimal.valueOf(detail.getDifference())));
                detail.setDifferenceReason(executeItem.getDifferenceReason());

                inventoryDetailMapper.updateById(detail);

                StoreInventory inventory = storeInventoryMapper.selectByStoreAndMaterial(
                        plan.getStoreId(), detail.getMaterialId());
                if (inventory != null) {
                    inventory.setQuantity(executeItem.getActualQuantity());
                    storeInventoryMapper.updateById(inventory);
                }
            }
        }

        boolean allCompleted = details.stream()
                .allMatch(d -> d.getActualQuantity() != null || executeItemMap.containsKey(d.getMaterialId()));

        if (allCompleted) {
            plan.setStatus(2);
            plan.setEndTime(LocalDateTime.now());
            inventoryPlanMapper.updateById(plan);
        }
    }

    @Override
    public List<InventoryDetailVO> getInventoryRecord(Long planId) {
        InventoryPlan plan = inventoryPlanMapper.selectById(planId);
        if (plan == null) {
            throw BusinessException.of("盘点计划不存在");
        }

        List<InventoryDetail> details = inventoryDetailMapper.selectByPlanId(planId);
        return BeanUtil.copyToList(details, InventoryDetailVO.class);
    }

    @Override
    public InventoryAnalysisVO getInventoryAnalysis(Long planId) {
        InventoryPlan plan = inventoryPlanMapper.selectById(planId);
        if (plan == null) {
            throw BusinessException.of("盘点计划不存在");
        }
        if (plan.getStatus() != 2) {
            throw BusinessException.of("盘点未完成，无法进行差异分析");
        }

        InventoryAnalysisVO vo = new InventoryAnalysisVO();
        vo.setPlanId(planId);
        vo.setPlanNo(plan.getPlanNo());

        Store store = storeMapper.selectById(plan.getStoreId());
        if (store != null) {
            vo.setStoreName(store.getStoreName());
        }

        vo.setPlanDate(DateUtil.format(plan.getPlanDate(), "yyyy-MM-dd"));

        List<InventoryDetail> details = inventoryDetailMapper.selectByPlanId(planId);
        vo.setTotalItems(details.size());

        Integer gainItems = inventoryDetailMapper.countGainByPlanId(planId);
        Integer lossItems = inventoryDetailMapper.countLossByPlanId(planId);

        vo.setGainItems(gainItems != null ? gainItems : 0);
        vo.setLossItems(lossItems != null ? lossItems : 0);
        vo.setNormalItems(details.size() - vo.getGainItems() - vo.getLossItems());

        BigDecimal totalGainCost = inventoryDetailMapper.sumGainCostByPlanId(planId);
        BigDecimal totalLossCost = inventoryDetailMapper.sumLossCostByPlanId(planId);

        vo.setTotalGainCost(totalGainCost != null ? totalGainCost : BigDecimal.ZERO);
        vo.setTotalLossCost(totalLossCost != null ? totalLossCost.abs() : BigDecimal.ZERO);

        List<InventoryDetail> differenceDetails = inventoryDetailMapper.selectDifferenceByPlanId(planId);
        vo.setDifferenceDetails(BeanUtil.copyToList(differenceDetails, InventoryDetailVO.class));

        return vo;
    }

    private InventoryPlanVO convertToVO(InventoryPlan plan) {
        InventoryPlanVO vo = BeanUtil.copyProperties(plan, InventoryPlanVO.class);

        Store store = storeMapper.selectById(plan.getStoreId());
        if (store != null) {
            vo.setStoreName(store.getStoreName());
        }

        if (plan.getCreatorId() != null) {
            String creatorName = sysUserMapper.selectRealNameById(plan.getCreatorId());
            vo.setCreatorName(creatorName);
        }

        vo.setStatusDesc(getStatusDesc(plan.getStatus()));

        return vo;
    }

    private String getStatusDesc(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待执行";
            case 1: return "执行中";
            case 2: return "已完成";
            case 3: return "已取消";
            default: return "未知";
        }
    }

    private String generatePlanNo() {
        return "PD" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss") +
                String.format("%04d", (int)(Math.random() * 10000));
    }
}
