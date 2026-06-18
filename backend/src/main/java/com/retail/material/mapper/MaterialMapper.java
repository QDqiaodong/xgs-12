package com.retail.material.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.retail.material.entity.Material;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MaterialMapper extends BaseMapper<Material> {

    @Select("SELECT * FROM material WHERE current_stock < safety_stock AND status = 1 AND deleted = 0")
    List<Material> selectLowStockMaterials();
}