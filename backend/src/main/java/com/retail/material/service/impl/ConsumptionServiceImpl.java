package com.retail.material.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.retail.material.common.PageResult;
import com.retail.material.dto.ConsumptionCreateDTO;
import com.retail.material.dto.ConsumptionQueryDTO;
import com.retail.material.entity.Consumption;
import com.retail.material.entity.Material;
import com.retail.material.entity.Store;
import com.retail.material.entity.StoreInventory;
import com.retail.material.exception.BusinessException;
import com.retail.material.mapper.ConsumptionMapper;
import com.retail.material.mapper.MaterialMapper;
import com.retail.material.mapper.StoreInventoryMapper;
import com.retail.material.mapper.StoreMapper;
import com.retail.material.service.CacheService;
import com.retail.material.service.ConsumptionService;
import com.retail.material.utils.SecurityUtils;
import com.retail.material.vo.ConsumptionVO;
import com.retail.material.vo.MonthlyConsumptionVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConsumptionServiceImpl implements ConsumptionService {

    private final ConsumptionMapper consumptionMapper;
    private final MaterialMapper materialMapper;
    private final StoreMapper storeMapper;
    private final StoreInventoryMapper storeInventoryMapper;
    private final CacheService cacheService;

    public ConsumptionServiceImpl(ConsumptionMapper consumptionMapper,
                                  MaterialMapper materialMapper,
                                  StoreMapper storeMapper,
                                  StoreInventoryMapper storeInventoryMapper,
                                  CacheService cacheService) {
        this.consumptionMapper = consumptionMapper;
        this.materialMapper = materialMapper;
        this.storeMapper = storeMapper;
        this.storeInventoryMapper = storeInventoryMapper;
        this.cacheService = cacheService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createConsumption(ConsumptionCreateDTO createDTO) {
        Store store = storeMapper.selectById(createDTO.getStoreId());
        if (store == null) {
            throw BusinessException.of("门店不存在");
        }

        Material material = materialMapper.selectById(createDTO.getMaterialId());
        if (material == null) {
            throw BusinessException.of("耗材不存在");
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw BusinessException.of(401, "未登录");
        }

        StoreInventory inventory = storeInventoryMapper.selectByStoreAndMaterial(
                createDTO.getStoreId(), createDTO.getMaterialId());
        if (inventory == null || inventory.getQuantity() < createDTO.getQuantity()) {
            throw BusinessException.of("库存不足: " + material.getMaterialName());
        }

        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        BigDecimal unitCost = createDTO.getUnitCost() != null ? createDTO.getUnitCost() : inventory.getAvgCost();
        BigDecimal totalCost = unitCost.multiply(BigDecimal.valueOf(createDTO.getQuantity()));

        Consumption consumption = new Consumption();
        consumption.setStoreId(createDTO.getStoreId());
        consumption.setMaterialId(createDTO.getMaterialId());
        consumption.setYear(year);
        consumption.setMonth(month);
        consumption.setQuantity(createDTO.getQuantity());
        consumption.setTotalCost(totalCost);
        consumption.setRemark(createDTO.getRemark());

        consumptionMapper.insert(consumption);

        inventory.setQuantity(inventory.getQuantity() - createDTO.getQuantity());
        storeInventoryMapper.updateById(inventory);

        cacheService.cacheConsumptionThreshold(createDTO.getStoreId(), year, month,
                createDTO.getMaterialId(), createDTO.getQuantity());

        return consumption.getId();
    }

    @Override
    public PageResult<ConsumptionVO> listConsumptions(ConsumptionQueryDTO queryDTO) {
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

        wrapper.orderByDesc(Consumption::getCreateTime);

        IPage<Consumption> page = consumptionMapper.selectPage(
                new Page<>(queryDTO.getCurrent(), queryDTO.getSize()),
                wrapper
        );

        List<ConsumptionVO> voList = new ArrayList<>();
        for (Consumption consumption : page.getRecords()) {
            voList.add(convertToVO(consumption));
        }

        return PageResult.of(voList, page.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    public MonthlyConsumptionVO getMonthlyConsumption(Long storeId, Integer year, Integer month) {
        Store store = storeMapper.selectById(storeId);
        if (store == null) {
            throw BusinessException.of("门店不存在");
        }

        List<Consumption> consumptions = consumptionMapper.selectByStoreAndMonth(storeId, year, month);

        MonthlyConsumptionVO vo = new MonthlyConsumptionVO();
        vo.setStoreId(storeId);
        vo.setStoreName(store.getStoreName());
        vo.setYear(year);
        vo.setMonth(month);

        Integer totalQuantity = consumptionMapper.sumQuantityByStoreAndMonth(storeId, year, month);
        BigDecimal totalCost = consumptionMapper.sumCostByStoreAndMonth(storeId, year, month);

        vo.setTotalQuantity(totalQuantity != null ? totalQuantity : 0);
        vo.setTotalCost(totalCost != null ? totalCost : BigDecimal.ZERO);

        List<ConsumptionVO> detailVOs = new ArrayList<>();
        for (Consumption consumption : consumptions) {
            detailVOs.add(convertToVO(consumption));
        }
        vo.setDetails(detailVOs);

        return vo;
    }

    @Override
    public List<ConsumptionVO> getStoreConsumptions(Long storeId) {
        Store store = storeMapper.selectById(storeId);
        if (store == null) {
            throw BusinessException.of("门店不存在");
        }

        LambdaQueryWrapper<Consumption> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Consumption::getStoreId, storeId);
        wrapper.orderByDesc(Consumption::getCreateTime);

        List<Consumption> consumptions = consumptionMapper.selectList(wrapper);

        List<ConsumptionVO> voList = new ArrayList<>();
        for (Consumption consumption : consumptions) {
            voList.add(convertToVO(consumption));
        }

        return voList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConsumption(Long id, ConsumptionCreateDTO updateDTO) {
        Consumption consumption = consumptionMapper.selectById(id);
        if (consumption == null) {
            throw BusinessException.of("消耗记录不存在");
        }

        int oldQuantity = consumption.getQuantity();

        Store store = storeMapper.selectById(updateDTO.getStoreId());
        if (store == null) {
            throw BusinessException.of("门店不存在");
        }

        Material material = materialMapper.selectById(updateDTO.getMaterialId());
        if (material == null) {
            throw BusinessException.of("耗材不存在");
        }

        StoreInventory inventory = storeInventoryMapper.selectByStoreAndMaterial(
                updateDTO.getStoreId(), updateDTO.getMaterialId());

        int quantityDiff = updateDTO.getQuantity() - oldQuantity;
        if (quantityDiff > 0 && (inventory == null || inventory.getQuantity() < quantityDiff)) {
            throw BusinessException.of("库存不足: " + material.getMaterialName());
        }

        BigDecimal unitCost = updateDTO.getUnitCost() != null ? updateDTO.getUnitCost() :
                (inventory != null ? inventory.getAvgCost() : material.getPurchaseCost());
        BigDecimal totalCost = unitCost.multiply(BigDecimal.valueOf(updateDTO.getQuantity()));

        consumption.setStoreId(updateDTO.getStoreId());
        consumption.setMaterialId(updateDTO.getMaterialId());
        consumption.setQuantity(updateDTO.getQuantity());
        consumption.setTotalCost(totalCost);
        consumption.setRemark(updateDTO.getRemark());

        consumptionMapper.updateById(consumption);

        if (inventory != null) {
            inventory.setQuantity(inventory.getQuantity() - quantityDiff);
            storeInventoryMapper.updateById(inventory);
        }

        cacheService.cacheConsumptionThreshold(updateDTO.getStoreId(), consumption.getYear(),
                consumption.getMonth(), updateDTO.getMaterialId(), updateDTO.getQuantity());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConsumption(Long id) {
        Consumption consumption = consumptionMapper.selectById(id);
        if (consumption == null) {
            throw BusinessException.of("消耗记录不存在");
        }

        StoreInventory inventory = storeInventoryMapper.selectByStoreAndMaterial(
                consumption.getStoreId(), consumption.getMaterialId());

        if (inventory != null) {
            inventory.setQuantity(inventory.getQuantity() + consumption.getQuantity());
            storeInventoryMapper.updateById(inventory);
        }

        consumptionMapper.deleteById(id);

        cacheService.removeConsumptionThreshold(consumption.getStoreId(), consumption.getYear(),
                consumption.getMonth(), consumption.getMaterialId());
    }

    private ConsumptionVO convertToVO(Consumption consumption) {
        ConsumptionVO vo = BeanUtil.copyProperties(consumption, ConsumptionVO.class);

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

        return vo;
    }
}
