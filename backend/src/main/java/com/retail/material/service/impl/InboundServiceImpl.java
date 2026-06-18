package com.retail.material.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.retail.material.common.PageResult;
import com.retail.material.dto.InboundCreateDTO;
import com.retail.material.dto.InboundItemDTO;
import com.retail.material.dto.InboundQueryDTO;
import com.retail.material.entity.Material;
import com.retail.material.entity.Store;
import com.retail.material.entity.TransferItem;
import com.retail.material.entity.TransferOrder;
import com.retail.material.exception.BusinessException;
import com.retail.material.mapper.MaterialMapper;
import com.retail.material.mapper.StoreMapper;
import com.retail.material.mapper.TransferItemMapper;
import com.retail.material.mapper.TransferOrderMapper;
import com.retail.material.service.InboundService;
import com.retail.material.utils.SecurityUtils;
import com.retail.material.vo.InboundItemVO;
import com.retail.material.vo.InboundVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class InboundServiceImpl implements InboundService {

    private final TransferOrderMapper transferOrderMapper;
    private final TransferItemMapper transferItemMapper;
    private final MaterialMapper materialMapper;
    private final StoreMapper storeMapper;

    public InboundServiceImpl(TransferOrderMapper transferOrderMapper,
                             TransferItemMapper transferItemMapper,
                             MaterialMapper materialMapper,
                             StoreMapper storeMapper) {
        this.transferOrderMapper = transferOrderMapper;
        this.transferItemMapper = transferItemMapper;
        this.materialMapper = materialMapper;
        this.storeMapper = storeMapper;
    }

    @Override
    public PageResult<InboundVO> listInbounds(InboundQueryDTO queryDTO) {
        LambdaQueryWrapper<TransferOrder> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(queryDTO.getOrderNo())) {
            wrapper.like(TransferOrder::getOrderNo, queryDTO.getOrderNo());
        }
        if (queryDTO.getTargetStoreId() != null) {
            wrapper.eq(TransferOrder::getTargetStoreId, queryDTO.getTargetStoreId());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(TransferOrder::getStatus, queryDTO.getStatus());
        }
        
        wrapper.orderByDesc(TransferOrder::getCreateTime);
        
        IPage<TransferOrder> page = transferOrderMapper.selectPage(
            new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), 
            wrapper
        );
        
        List<InboundVO> voList = new ArrayList<>();
        for (TransferOrder order : page.getRecords()) {
            InboundVO vo = convertToVO(order);
            voList.add(vo);
        }
        
        return PageResult.of(voList, page.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    public InboundVO getInboundById(Long id) {
        TransferOrder order = transferOrderMapper.selectById(id);
        if (order == null) {
            throw BusinessException.of("入库单不存在");
        }
        
        InboundVO vo = convertToVO(order);
        
        List<TransferItem> items = transferItemMapper.selectByOrderId(id);
        vo.setItems(BeanUtil.copyToList(items, InboundItemVO.class));
        
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createInbound(InboundCreateDTO createDTO) {
        Store targetStore = storeMapper.selectById(createDTO.getTargetStoreId());
        if (targetStore == null) {
            throw BusinessException.of("目标门店不存在");
        }
        
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw BusinessException.of(401, "未登录");
        }
        
        String orderNo = generateOrderNo();
        
        TransferOrder order = new TransferOrder();
        order.setOrderNo(orderNo);
        order.setSourceStoreId(createDTO.getSourceStoreId());
        order.setTargetStoreId(createDTO.getTargetStoreId());
        order.setStatus(0);
        order.setApplicantId(currentUserId);
        order.setApplyTime(LocalDateTime.now());
        order.setRemark(createDTO.getRemark());
        
        BigDecimal totalCost = BigDecimal.ZERO;
        List<TransferItem> items = new ArrayList<>();
        
        for (InboundItemDTO itemDTO : createDTO.getItems()) {
            Material material = materialMapper.selectById(itemDTO.getMaterialId());
            if (material == null) {
                throw BusinessException.of("耗材不存在: " + itemDTO.getMaterialId());
            }
            
            TransferItem item = new TransferItem();
            item.setMaterialId(itemDTO.getMaterialId());
            item.setMaterialName(material.getMaterialName());
            item.setSpecification(material.getSpecification());
            item.setUnit(material.getUnit());
            item.setRequestQuantity(itemDTO.getRequestQuantity());
            item.setApprovedQuantity(itemDTO.getRequestQuantity());
            item.setUnitCost(itemDTO.getUnitCost());
            
            BigDecimal itemTotalCost = itemDTO.getUnitCost().multiply(BigDecimal.valueOf(itemDTO.getRequestQuantity()));
            item.setTotalCost(itemTotalCost);
            totalCost = totalCost.add(itemTotalCost);
            
            items.add(item);
        }
        
        order.setTotalCost(totalCost);
        transferOrderMapper.insert(order);
        
        for (TransferItem item : items) {
            item.setTransferOrderId(order.getId());
            transferItemMapper.insert(item);
        }
        
        return order.getId();
    }

    private InboundVO convertToVO(TransferOrder order) {
        InboundVO vo = BeanUtil.copyProperties(order, InboundVO.class);
        
        if (order.getSourceStoreId() != null && order.getSourceStoreId() > 0) {
            Store sourceStore = storeMapper.selectById(order.getSourceStoreId());
            if (sourceStore != null) {
                vo.setSourceStoreName(sourceStore.getStoreName());
            }
        } else {
            vo.setSourceStoreName("总部总仓");
        }
        
        if (order.getTargetStoreId() != null) {
            Store targetStore = storeMapper.selectById(order.getTargetStoreId());
            if (targetStore != null) {
                vo.setTargetStoreName(targetStore.getStoreName());
            }
        }
        
        vo.setStatusDesc(getStatusDesc(order.getStatus()));
        
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

    private String generateOrderNo() {
        return "IN" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss") + 
               String.format("%04d", (int)(Math.random() * 10000));
    }
}