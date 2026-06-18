package com.retail.material.service;

import com.retail.material.common.PageResult;
import com.retail.material.dto.MaterialCreateDTO;
import com.retail.material.dto.MaterialQueryDTO;
import com.retail.material.dto.MaterialUpdateDTO;
import com.retail.material.vo.MaterialVO;
import com.retail.material.vo.StockAlertVO;

import java.util.List;

public interface MaterialService {

    PageResult<MaterialVO> listMaterials(MaterialQueryDTO queryDTO);

    MaterialVO getMaterialById(Long id);

    Long createMaterial(MaterialCreateDTO createDTO);

    void updateMaterial(Long id, MaterialUpdateDTO updateDTO);

    void deleteMaterial(Long id);

    List<StockAlertVO> getStockAlerts();
}