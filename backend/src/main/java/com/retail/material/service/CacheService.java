package com.retail.material.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

public interface CacheService {

    void cacheConsumptionThreshold(Long storeId, Integer year, Integer month, Long materialId, Integer quantity);

    Set<Object> getConsumptionThreshold(Long storeId, Integer year, Integer month);

    Set<Object> getTopConsumptionMaterials(Long storeId, Integer year, Integer month, int topN);

    void removeConsumptionThreshold(Long storeId, Integer year, Integer month, Long materialId);

    void clearConsumptionThreshold(Long storeId, Integer year, Integer month);

    void cacheMaterialParams(Long materialId, Integer safetyStock, java.math.BigDecimal purchaseCost,
                             Integer shelfLife, LocalDateTime lastPurchaseTime);

    Map<Object, Object> getMaterialParams(Long materialId);

    void updateMaterialParam(Long materialId, String field, Object value);

    void cacheMaterialParams(Long storeId);

    void clearMaterialParams(Long materialId);
}
