package com.retail.material.service;

import com.retail.material.common.PageResult;
import com.retail.material.dto.InventoryExecuteDTO;
import com.retail.material.dto.InventoryPlanCreateDTO;
import com.retail.material.dto.InventoryPlanQueryDTO;
import com.retail.material.vo.InventoryAnalysisVO;
import com.retail.material.vo.InventoryDetailVO;
import com.retail.material.vo.InventoryPlanVO;

import java.util.List;

public interface InventoryService {

    Long createInventoryPlan(InventoryPlanCreateDTO createDTO);

    PageResult<InventoryPlanVO> listInventoryPlans(InventoryPlanQueryDTO queryDTO);

    void updateInventoryPlan(Long id, InventoryPlanCreateDTO updateDTO);

    void executeInventory(InventoryExecuteDTO executeDTO);

    List<InventoryDetailVO> getInventoryRecord(Long planId);

    InventoryAnalysisVO getInventoryAnalysis(Long planId);
}
