package com.retail.material.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.retail.material.entity.StoreInventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StoreInventoryMapper extends BaseMapper<StoreInventory> {

    @Select("SELECT * FROM store_inventory WHERE store_id = #{storeId} AND material_id = #{materialId} AND deleted = 0")
    StoreInventory selectByStoreAndMaterial(@Param("storeId") Long storeId, @Param("materialId") Long materialId);

    @Select("SELECT * FROM store_inventory WHERE store_id = #{storeId} AND deleted = 0")
    List<StoreInventory> selectByStoreId(@Param("storeId") Long storeId);

    @Select("SELECT * FROM store_inventory WHERE material_id = #{materialId} AND deleted = 0")
    List<StoreInventory> selectByMaterialId(@Param("materialId") Long materialId);
}