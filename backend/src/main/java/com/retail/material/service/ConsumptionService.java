package com.retail.material.service;

import com.retail.material.common.PageResult;
import com.retail.material.dto.ConsumptionCreateDTO;
import com.retail.material.dto.ConsumptionQueryDTO;
import com.retail.material.vo.ConsumptionVO;
import com.retail.material.vo.MonthlyConsumptionVO;

import java.util.List;

public interface ConsumptionService {

    Long createConsumption(ConsumptionCreateDTO createDTO);

    PageResult<ConsumptionVO> listConsumptions(ConsumptionQueryDTO queryDTO);

    MonthlyConsumptionVO getMonthlyConsumption(Long storeId, Integer year, Integer month);

    List<ConsumptionVO> getStoreConsumptions(Long storeId);

    void updateConsumption(Long id, ConsumptionCreateDTO updateDTO);

    void deleteConsumption(Long id);
}
