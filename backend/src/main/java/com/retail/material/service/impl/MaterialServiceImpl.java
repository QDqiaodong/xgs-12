package com.retail.material.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.retail.material.common.PageResult;
import com.retail.material.dto.MaterialCreateDTO;
import com.retail.material.dto.MaterialQueryDTO;
import com.retail.material.dto.MaterialUpdateDTO;
import com.retail.material.entity.Material;
import com.retail.material.exception.BusinessException;
import com.retail.material.mapper.MaterialMapper;
import com.retail.material.service.MaterialService;
import com.retail.material.vo.MaterialVO;
import com.retail.material.vo.StockAlertVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class MaterialServiceImpl implements MaterialService {

    private final MaterialMapper materialMapper;

    public MaterialServiceImpl(MaterialMapper materialMapper) {
        this.materialMapper = materialMapper;
    }

    @Override
    public PageResult<MaterialVO> listMaterials(MaterialQueryDTO queryDTO) {
        LambdaQueryWrapper<Material> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(queryDTO.getMaterialCode())) {
            wrapper.like(Material::getMaterialCode, queryDTO.getMaterialCode());
        }
        if (StringUtils.hasText(queryDTO.getMaterialName())) {
            wrapper.like(Material::getMaterialName, queryDTO.getMaterialName());
        }
        if (StringUtils.hasText(queryDTO.getCategory())) {
            wrapper.eq(Material::getCategory, queryDTO.getCategory());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Material::getStatus, queryDTO.getStatus());
        }
        
        wrapper.orderByDesc(Material::getCreateTime);
        
        IPage<Material> page = materialMapper.selectPage(
            new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), 
            wrapper
        );
        
        return PageResult.of(
            BeanUtil.copyToList(page.getRecords(), MaterialVO.class),
            page.getTotal(),
            page.getSize(),
            page.getCurrent()
        );
    }

    @Override
    public MaterialVO getMaterialById(Long id) {
        Material material = materialMapper.selectById(id);
        if (material == null) {
            throw BusinessException.of("耗材不存在");
        }
        return BeanUtil.copyProperties(material, MaterialVO.class);
    }

    @Override
    public Long createMaterial(MaterialCreateDTO createDTO) {
        LambdaQueryWrapper<Material> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Material::getMaterialCode, createDTO.getMaterialCode());
        if (materialMapper.selectCount(wrapper) > 0) {
            throw BusinessException.of("耗材编码已存在");
        }
        
        Material material = BeanUtil.copyProperties(createDTO, Material.class);
        materialMapper.insert(material);
        return material.getId();
    }

    @Override
    public void updateMaterial(Long id, MaterialUpdateDTO updateDTO) {
        Material material = materialMapper.selectById(id);
        if (material == null) {
            throw BusinessException.of("耗材不存在");
        }
        
        BeanUtil.copyProperties(updateDTO, material, "id");
        
        if (material.getCurrentStock() < material.getSafetyStock()) {
            material.setStatus(2);
        }
        
        materialMapper.updateById(material);
    }

    @Override
    public void deleteMaterial(Long id) {
        Material material = materialMapper.selectById(id);
        if (material == null) {
            throw BusinessException.of("耗材不存在");
        }
        materialMapper.deleteById(id);
    }

    @Override
    public List<StockAlertVO> getStockAlerts() {
        List<Material> lowStockMaterials = materialMapper.selectLowStockMaterials();
        List<StockAlertVO> alerts = new ArrayList<>();
        
        for (Material material : lowStockMaterials) {
            StockAlertVO alert = BeanUtil.copyProperties(material, StockAlertVO.class);
            alert.setMaterialId(material.getId());
            alert.setAlertType(0);
            alert.setAlertMessage(String.format("库存不足：当前库存 %d，安全库存 %d", 
                material.getCurrentStock(), material.getSafetyStock()));
            alerts.add(alert);
        }
        
        return alerts;
    }
}