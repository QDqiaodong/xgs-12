package com.retail.material.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.retail.material.entity.InventoryPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface InventoryPlanMapper extends BaseMapper<InventoryPlan> {

    @Select("SELECT * FROM inventory_plan WHERE store_id = #{storeId} AND status = #{status} AND deleted = 0 LIMIT 1")
    InventoryPlan selectLatestByStoreAndStatus(@Param("storeId") Long storeId, @Param("status") Integer status);
}