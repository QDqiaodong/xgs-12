package com.retail.material.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.retail.material.entity.InventoryLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface InventoryLogMapper extends BaseMapper<InventoryLog> {

    @Select("SELECT * FROM inventory_log WHERE store_id = #{storeId} AND material_id = #{materialId} ORDER BY create_time DESC LIMIT #{limit}")
    List<InventoryLog> selectRecentLogs(@Param("storeId") Long storeId, @Param("materialId") Long materialId, @Param("limit") Integer limit);

    @Select("SELECT * FROM inventory_log WHERE store_id = #{storeId} ORDER BY create_time DESC LIMIT #{limit}")
    List<InventoryLog> selectRecentLogsByStore(@Param("storeId") Long storeId, @Param("limit") Integer limit);
}