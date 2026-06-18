package com.retail.material.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.retail.material.common.PageResult;
import com.retail.material.dto.StoreCreateDTO;
import com.retail.material.dto.StoreQueryDTO;
import com.retail.material.dto.StoreUpdateDTO;
import com.retail.material.entity.Store;
import com.retail.material.exception.BusinessException;
import com.retail.material.mapper.StoreMapper;
import com.retail.material.service.StoreService;
import com.retail.material.vo.StoreVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class StoreServiceImpl implements StoreService {

    private final StoreMapper storeMapper;

    public StoreServiceImpl(StoreMapper storeMapper) {
        this.storeMapper = storeMapper;
    }

    @Override
    public PageResult<StoreVO> listStores(StoreQueryDTO queryDTO) {
        LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(queryDTO.getStoreCode())) {
            wrapper.like(Store::getStoreCode, queryDTO.getStoreCode());
        }
        if (StringUtils.hasText(queryDTO.getStoreName())) {
            wrapper.like(Store::getStoreName, queryDTO.getStoreName());
        }
        if (StringUtils.hasText(queryDTO.getStoreType())) {
            wrapper.eq(Store::getStoreType, queryDTO.getStoreType());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Store::getStatus, queryDTO.getStatus());
        }
        
        wrapper.orderByDesc(Store::getCreateTime);
        
        IPage<Store> page = storeMapper.selectPage(
            new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), 
            wrapper
        );
        
        return PageResult.of(
            BeanUtil.copyToList(page.getRecords(), StoreVO.class),
            page.getTotal(),
            page.getSize(),
            page.getCurrent()
        );
    }

    @Override
    public StoreVO getStoreById(Long id) {
        Store store = storeMapper.selectById(id);
        if (store == null) {
            throw BusinessException.of("门店不存在");
        }
        return BeanUtil.copyProperties(store, StoreVO.class);
    }

    @Override
    public Long createStore(StoreCreateDTO createDTO) {
        LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Store::getStoreCode, createDTO.getStoreCode());
        if (storeMapper.selectCount(wrapper) > 0) {
            throw BusinessException.of("门店编码已存在");
        }
        
        Store store = BeanUtil.copyProperties(createDTO, Store.class);
        storeMapper.insert(store);
        return store.getId();
    }

    @Override
    public void updateStore(Long id, StoreUpdateDTO updateDTO) {
        Store store = storeMapper.selectById(id);
        if (store == null) {
            throw BusinessException.of("门店不存在");
        }
        
        BeanUtil.copyProperties(updateDTO, store, "id");
        storeMapper.updateById(store);
    }

    @Override
    public void deleteStore(Long id) {
        Store store = storeMapper.selectById(id);
        if (store == null) {
            throw BusinessException.of("门店不存在");
        }
        storeMapper.deleteById(id);
    }
}