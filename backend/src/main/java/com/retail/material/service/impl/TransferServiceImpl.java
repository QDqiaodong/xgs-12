package com.retail.material.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.retail.material.common.PageResult;
import com.retail.material.dto.*;
import com.retail.material.entity.*;
import com.retail.material.exception.BusinessException;
import com.retail.material.mapper.*;
import com.retail.material.service.TransferService;
import com.retail.material.utils.SecurityUtils;
import com.retail.material.vo.TransferItemVO;
import com.retail.material.vo.TransferVO;
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
public class TransferServiceImpl implements TransferService {

    private final TransferOrderMapper transferOrderMapper;
    private final TransferItemMapper transferItemMapper;
    private final MaterialMapper materialMapper;
    private final StoreMapper storeMapper;
    private final StoreInventoryMapper storeInventoryMapper;
    private final SysUserMapper sysUserMapper;

    public TransferServiceImpl(TransferOrderMapper transferOrderMapper,
                               TransferItemMapper transferItemMapper,
                               MaterialMapper materialMapper,
                               StoreMapper storeMapper,
                               StoreInventoryMapper storeInventoryMapper,
                               SysUserMapper sysUserMapper) {
        this.transferOrderMapper = transferOrderMapper;
        this.transferItemMapper = transferItemMapper;
        this.materialMapper = materialMapper;
        this.storeMapper = storeMapper;
        this.storeInventoryMapper = storeInventoryMapper;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTransfer(TransferCreateDTO createDTO) {
        Store sourceStore = storeMapper.selectById(createDTO.getSourceStoreId());
        if (sourceStore == null) {
            throw BusinessException.of("调出门店不存在");
        }
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

        for (TransferItemDTO itemDTO : createDTO.getItems()) {
            Material material = materialMapper.selectById(itemDTO.getMaterialId());
            if (material == null) {
                throw BusinessException.of("耗材不存在: " + itemDTO.getMaterialId());
            }

            if (createDTO.getSourceStoreId() > 0) {
                StoreInventory inventory = storeInventoryMapper.selectByStoreAndMaterial(
                        createDTO.getSourceStoreId(), itemDTO.getMaterialId());
                if (inventory == null || inventory.getQuantity() < itemDTO.getRequestQuantity()) {
                    throw BusinessException.of("库存不足: " + material.getMaterialName());
                }
            }

            TransferItem item = new TransferItem();
            item.setMaterialId(itemDTO.getMaterialId());
            item.setMaterialName(material.getMaterialName());
            item.setSpecification(material.getSpecification());
            item.setUnit(material.getUnit());
            item.setRequestQuantity(itemDTO.getRequestQuantity());
            item.setApprovedQuantity(0);
            item.setUnitCost(material.getPurchaseCost());

            BigDecimal itemTotalCost = material.getPurchaseCost().multiply(BigDecimal.valueOf(itemDTO.getRequestQuantity()));
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

    @Override
    public PageResult<TransferVO> listTransfers(TransferQueryDTO queryDTO) {
        LambdaQueryWrapper<TransferOrder> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getOrderNo())) {
            wrapper.like(TransferOrder::getOrderNo, queryDTO.getOrderNo());
        }
        if (queryDTO.getSourceStoreId() != null) {
            wrapper.eq(TransferOrder::getSourceStoreId, queryDTO.getSourceStoreId());
        }
        if (queryDTO.getTargetStoreId() != null) {
            wrapper.eq(TransferOrder::getTargetStoreId, queryDTO.getTargetStoreId());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(TransferOrder::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getStartTime() != null) {
            wrapper.ge(TransferOrder::getApplyTime, queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            wrapper.le(TransferOrder::getApplyTime, queryDTO.getEndTime());
        }

        wrapper.orderByDesc(TransferOrder::getCreateTime);

        IPage<TransferOrder> page = transferOrderMapper.selectPage(
                new Page<>(queryDTO.getCurrent(), queryDTO.getSize()),
                wrapper
        );

        List<TransferVO> voList = new ArrayList<>();
        for (TransferOrder order : page.getRecords()) {
            voList.add(convertToVO(order));
        }

        return PageResult.of(voList, page.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    public TransferVO getTransferById(Long id) {
        TransferOrder order = transferOrderMapper.selectById(id);
        if (order == null) {
            throw BusinessException.of("调拨单不存在");
        }

        TransferVO vo = convertToVO(order);

        List<TransferItem> items = transferItemMapper.selectByOrderId(id);
        vo.setItems(BeanUtil.copyToList(items, TransferItemVO.class));

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveTransfer(Long id, TransferApproveDTO approveDTO) {
        TransferOrder order = transferOrderMapper.selectById(id);
        if (order == null) {
            throw BusinessException.of("调拨单不存在");
        }
        if (order.getStatus() != 0) {
            throw BusinessException.of("调拨单状态不正确，无法审批");
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw BusinessException.of(401, "未登录");
        }

        List<TransferItem> items = transferItemMapper.selectByOrderId(id);

        if (approveDTO.getApproved()) {
            order.setStatus(1);
            for (TransferItem item : items) {
                item.setApprovedQuantity(item.getRequestQuantity());
                transferItemMapper.updateById(item);
            }
        } else {
            order.setStatus(5);
        }

        order.setApproverId(currentUserId);
        order.setApproveTime(LocalDateTime.now());
        order.setRemark(approveDTO.getRemark());
        transferOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void outboundTransfer(Long id, TransferOutboundDTO outboundDTO) {
        TransferOrder order = transferOrderMapper.selectById(id);
        if (order == null) {
            throw BusinessException.of("调拨单不存在");
        }
        if (order.getStatus() != 1) {
            throw BusinessException.of("调拨单状态不正确，无法出库");
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw BusinessException.of(401, "未登录");
        }

        List<TransferItem> items = transferItemMapper.selectByOrderId(id);
        Map<Long, OutboundItemDTO> outboundItemMap = outboundDTO.getItems().stream()
                .collect(Collectors.toMap(OutboundItemDTO::getTransferItemId, item -> item));

        BigDecimal totalCost = BigDecimal.ZERO;

        for (TransferItem item : items) {
            OutboundItemDTO outboundItem = outboundItemMap.get(item.getId());
            if (outboundItem == null) {
                throw BusinessException.of("请提供所有明细的出库数量");
            }
            if (outboundItem.getActualQuantity() > item.getApprovedQuantity()) {
                throw BusinessException.of("出库数量不能超过审批数量: " + item.getMaterialName());
            }

            if (order.getSourceStoreId() > 0) {
                StoreInventory inventory = storeInventoryMapper.selectByStoreAndMaterial(
                        order.getSourceStoreId(), item.getMaterialId());
                if (inventory == null || inventory.getQuantity() < outboundItem.getActualQuantity()) {
                    throw BusinessException.of("库存不足: " + item.getMaterialName());
                }

                inventory.setQuantity(inventory.getQuantity() - outboundItem.getActualQuantity());
                storeInventoryMapper.updateById(inventory);
            }

            item.setActualQuantity(outboundItem.getActualQuantity());
            item.setDifference(item.getApprovedQuantity() - outboundItem.getActualQuantity());
            item.setDifferenceReason(outboundItem.getDifferenceReason());

            BigDecimal itemTotalCost = item.getUnitCost().multiply(BigDecimal.valueOf(outboundItem.getActualQuantity()));
            item.setTotalCost(itemTotalCost);
            totalCost = totalCost.add(itemTotalCost);

            transferItemMapper.updateById(item);
        }

        order.setStatus(2);
        order.setTotalCost(totalCost);
        order.setOutboundTime(LocalDateTime.now());
        transferOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receiveTransfer(Long id, TransferReceiveDTO receiveDTO) {
        TransferOrder order = transferOrderMapper.selectById(id);
        if (order == null) {
            throw BusinessException.of("调拨单不存在");
        }
        if (order.getStatus() != 2) {
            throw BusinessException.of("调拨单状态不正确，无法签收");
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw BusinessException.of(401, "未登录");
        }

        List<TransferItem> items = transferItemMapper.selectByOrderId(id);
        Map<Long, ReceiveItemDTO> receiveItemMap = receiveDTO.getItems().stream()
                .collect(Collectors.toMap(ReceiveItemDTO::getTransferItemId, item -> item));

        for (TransferItem item : items) {
            ReceiveItemDTO receiveItem = receiveItemMap.get(item.getId());
            if (receiveItem == null) {
                throw BusinessException.of("请提供所有明细的签收数量");
            }
            if (receiveItem.getReceiveQuantity() > item.getActualQuantity()) {
                throw BusinessException.of("签收数量不能超过出库数量: " + item.getMaterialName());
            }

            StoreInventory inventory = storeInventoryMapper.selectByStoreAndMaterial(
                    order.getTargetStoreId(), item.getMaterialId());

            if (inventory == null) {
                inventory = new StoreInventory();
                inventory.setStoreId(order.getTargetStoreId());
                inventory.setMaterialId(item.getMaterialId());
                inventory.setQuantity(receiveItem.getReceiveQuantity());
                inventory.setLockedQuantity(0);
                inventory.setAvgCost(item.getUnitCost());
                storeInventoryMapper.insert(inventory);
            } else {
                int newQuantity = inventory.getQuantity() + receiveItem.getReceiveQuantity();
                BigDecimal newAvgCost = inventory.getAvgCost()
                        .multiply(BigDecimal.valueOf(inventory.getQuantity()))
                        .add(item.getUnitCost().multiply(BigDecimal.valueOf(receiveItem.getReceiveQuantity())))
                        .divide(BigDecimal.valueOf(newQuantity), 2, java.math.RoundingMode.HALF_UP);

                inventory.setQuantity(newQuantity);
                inventory.setAvgCost(newAvgCost);
                storeInventoryMapper.updateById(inventory);
            }

            item.setDifference(item.getActualQuantity() - receiveItem.getReceiveQuantity());
            item.setDifferenceReason(receiveItem.getDifferenceReason());
            transferItemMapper.updateById(item);
        }

        order.setStatus(4);
        order.setReceiveTime(LocalDateTime.now());
        order.setRemark(receiveDTO.getRemark());
        transferOrderMapper.updateById(order);
    }

    @Override
    public List<TransferVO> getPendingApprovalList() {
        LambdaQueryWrapper<TransferOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TransferOrder::getStatus, 0);
        wrapper.orderByDesc(TransferOrder::getApplyTime);

        List<TransferOrder> orders = transferOrderMapper.selectList(wrapper);
        List<TransferVO> voList = new ArrayList<>();
        for (TransferOrder order : orders) {
            voList.add(convertToVO(order));
        }
        return voList;
    }

    @Override
    public List<TransferVO> getPendingOutboundList() {
        LambdaQueryWrapper<TransferOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TransferOrder::getStatus, 1);
        wrapper.orderByDesc(TransferOrder::getApproveTime);

        List<TransferOrder> orders = transferOrderMapper.selectList(wrapper);
        List<TransferVO> voList = new ArrayList<>();
        for (TransferOrder order : orders) {
            voList.add(convertToVO(order));
        }
        return voList;
    }

    @Override
    public List<TransferVO> getPendingReceiveList() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        SysUser user = sysUserMapper.selectById(currentUserId);

        LambdaQueryWrapper<TransferOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TransferOrder::getStatus, 2);
        if (user != null && user.getStoreId() != null) {
            wrapper.eq(TransferOrder::getTargetStoreId, user.getStoreId());
        }
        wrapper.orderByDesc(TransferOrder::getOutboundTime);

        List<TransferOrder> orders = transferOrderMapper.selectList(wrapper);
        List<TransferVO> voList = new ArrayList<>();
        for (TransferOrder order : orders) {
            voList.add(convertToVO(order));
        }
        return voList;
    }

    private TransferVO convertToVO(TransferOrder order) {
        TransferVO vo = BeanUtil.copyProperties(order, TransferVO.class);

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

        if (order.getApplicantId() != null) {
            String applicantName = sysUserMapper.selectRealNameById(order.getApplicantId());
            vo.setApplicantName(applicantName);
        }

        if (order.getApproverId() != null) {
            String approverName = sysUserMapper.selectRealNameById(order.getApproverId());
            vo.setApproverName(approverName);
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
        return "TR" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss") +
                String.format("%04d", (int)(Math.random() * 10000));
    }
}
