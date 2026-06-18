package com.retail.material.service.impl;

import com.retail.material.entity.Material;
import com.retail.material.entity.StoreInventory;
import com.retail.material.mapper.MaterialMapper;
import com.retail.material.mapper.StoreInventoryMapper;
import com.retail.material.service.CacheService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class CacheServiceImpl implements CacheService {

    private static final String CONSUMPTION_THRESHOLD_KEY = "consumption:threshold:%s:%s:%s";
    private static final String MATERIAL_PARAMS_KEY = "material:params:%s";
    private static final int CONSUMPTION_THRESHOLD_EXPIRE_DAYS = 90;
    private static final int MATERIAL_PARAMS_EXPIRE_HOURS = 24;

    private final RedisTemplate<String, Object> redisTemplate;
    private final MaterialMapper materialMapper;
    private final StoreInventoryMapper storeInventoryMapper;

    public CacheServiceImpl(RedisTemplate<String, Object> redisTemplate,
                            MaterialMapper materialMapper,
                            StoreInventoryMapper storeInventoryMapper) {
        this.redisTemplate = redisTemplate;
        this.materialMapper = materialMapper;
        this.storeInventoryMapper = storeInventoryMapper;
    }

    @Override
    public void cacheConsumptionThreshold(Long storeId, Integer year, Integer month, Long materialId, Integer quantity) {
        String key = String.format(CONSUMPTION_THRESHOLD_KEY, storeId, year, month);
        redisTemplate.opsForZSet().add(key, materialId, quantity);
        redisTemplate.expire(key, CONSUMPTION_THRESHOLD_EXPIRE_DAYS, TimeUnit.DAYS);
    }

    @Override
    public Set<Object> getConsumptionThreshold(Long storeId, Integer year, Integer month) {
        String key = String.format(CONSUMPTION_THRESHOLD_KEY, storeId, year, month);
        return redisTemplate.opsForZSet().reverseRange(key, 0, -1);
    }

    @Override
    public Set<Object> getTopConsumptionMaterials(Long storeId, Integer year, Integer month, int topN) {
        String key = String.format(CONSUMPTION_THRESHOLD_KEY, storeId, year, month);
        return redisTemplate.opsForZSet().reverseRange(key, 0, topN - 1);
    }

    @Override
    public void removeConsumptionThreshold(Long storeId, Integer year, Integer month, Long materialId) {
        String key = String.format(CONSUMPTION_THRESHOLD_KEY, storeId, year, month);
        redisTemplate.opsForZSet().remove(key, materialId);
    }

    @Override
    public void clearConsumptionThreshold(Long storeId, Integer year, Integer month) {
        String key = String.format(CONSUMPTION_THRESHOLD_KEY, storeId, year, month);
        redisTemplate.delete(key);
    }

    @Override
    public void cacheMaterialParams(Long materialId, Integer safetyStock, BigDecimal purchaseCost,
                                    Integer shelfLife, LocalDateTime lastPurchaseTime) {
        String key = String.format(MATERIAL_PARAMS_KEY, materialId);
        Map<String, Object> params = new HashMap<>();
        params.put("safetyStock", safetyStock);
        params.put("purchaseCost", purchaseCost);
        params.put("shelfLife", shelfLife);
        if (lastPurchaseTime != null) {
            params.put("lastPurchaseTime", lastPurchaseTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        redisTemplate.opsForHash().putAll(key, params);
        redisTemplate.expire(key, MATERIAL_PARAMS_EXPIRE_HOURS, TimeUnit.HOURS);
    }

    @Override
    public Map<Object, Object> getMaterialParams(Long materialId) {
        String key = String.format(MATERIAL_PARAMS_KEY, materialId);
        Map<Object, Object> params = redisTemplate.opsForHash().entries(key);
        if (params.isEmpty()) {
            Material material = materialMapper.selectById(materialId);
            if (material != null) {
                cacheMaterialParams(materialId, material.getSafetyStock(), material.getPurchaseCost(),
                        material.getShelfLife(), LocalDateTime.now());
                return redisTemplate.opsForHash().entries(key);
            }
        }
        return params;
    }

    @Override
    public void updateMaterialParam(Long materialId, String field, Object value) {
        String key = String.format(MATERIAL_PARAMS_KEY, materialId);
        redisTemplate.opsForHash().put(key, field, value);
        redisTemplate.expire(key, MATERIAL_PARAMS_EXPIRE_HOURS, TimeUnit.HOURS);
    }

    @Override
    public void cacheMaterialParams(Long storeId) {
        List<StoreInventory> inventories = storeInventoryMapper.selectByStoreId(storeId);
        for (StoreInventory inventory : inventories) {
            Material material = materialMapper.selectById(inventory.getMaterialId());
            if (material != null) {
                cacheMaterialParams(material.getId(), material.getSafetyStock(),
                        material.getPurchaseCost(), material.getShelfLife(), LocalDateTime.now());
            }
        }
    }

    @Override
    public void clearMaterialParams(Long materialId) {
        String key = String.format(MATERIAL_PARAMS_KEY, materialId);
        redisTemplate.delete(key);
    }
}
