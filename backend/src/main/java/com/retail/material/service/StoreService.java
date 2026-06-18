package com.retail.material.service;

import com.retail.material.common.PageResult;
import com.retail.material.dto.StoreCreateDTO;
import com.retail.material.dto.StoreQueryDTO;
import com.retail.material.dto.StoreUpdateDTO;
import com.retail.material.vo.StoreVO;

public interface StoreService {

    PageResult<StoreVO> listStores(StoreQueryDTO queryDTO);

    StoreVO getStoreById(Long id);

    Long createStore(StoreCreateDTO createDTO);

    void updateStore(Long id, StoreUpdateDTO updateDTO);

    void deleteStore(Long id);
}