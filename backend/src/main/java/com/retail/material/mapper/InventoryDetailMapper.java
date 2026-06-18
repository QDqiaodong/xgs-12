package com.retail.material.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.retail.material.entity.InventoryDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface InventoryDetailMapper extends BaseMapper<InventoryDetail> {

    @Select("SELECT * FROM inventory_detail WHERE plan_id = #{planId} AND deleted = 0")
    List<InventoryDetail> selectByPlanId(@Param("planId") Long planId);

    @Select("SELECT * FROM inventory_detail WHERE plan_id = #{planId} AND difference != 0 AND deleted = 0")
    List<InventoryDetail> selectDifferenceByPlanId(@Param("planId") Long planId);

    @Select("SELECT COUNT(*) FROM inventory_detail WHERE plan_id = #{planId} AND difference > 0 AND deleted = 0")
    Integer countGainByPlanId(@Param("planId") Long planId);

    @Select("SELECT COUNT(*) FROM inventory_detail WHERE plan_id = #{planId} AND difference < 0 AND deleted = 0")
    Integer countLossByPlanId(@Param("planId") Long planId);

    @Select("SELECT SUM(difference_cost) FROM inventory_detail WHERE plan_id = #{planId} AND difference > 0 AND deleted = 0")
    java.math.BigDecimal sumGainCostByPlanId(@Param("planId") Long planId);

    @Select("SELECT SUM(difference_cost) FROM inventory_detail WHERE plan_id = #{planId} AND difference < 0 AND deleted = 0")
    java.math.BigDecimal sumLossCostByPlanId(@Param("planId") Long planId);
}