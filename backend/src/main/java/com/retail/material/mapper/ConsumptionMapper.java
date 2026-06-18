package com.retail.material.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.retail.material.entity.Consumption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ConsumptionMapper extends BaseMapper<Consumption> {

    @Select("SELECT * FROM consumption WHERE store_id = #{storeId} AND year = #{year} AND month = #{month} AND deleted = 0")
    List<Consumption> selectByStoreAndMonth(@Param("storeId") Long storeId, @Param("year") Integer year, @Param("month") Integer month);

    @Select("<script>" +
            "SELECT SUM(total_cost) FROM consumption WHERE year = #{year} AND month = #{month} AND deleted = 0 " +
            "<if test='storeId != null'>AND store_id = #{storeId}</if>" +
            "</script>")
    java.math.BigDecimal sumCostByStoreAndMonth(@Param("storeId") Long storeId, @Param("year") Integer year, @Param("month") Integer month);

    @Select("<script>" +
            "SELECT SUM(quantity) FROM consumption WHERE year = #{year} AND month = #{month} AND deleted = 0 " +
            "<if test='storeId != null'>AND store_id = #{storeId}</if>" +
            "</script>")
    Integer sumQuantityByStoreAndMonth(@Param("storeId") Long storeId, @Param("year") Integer year, @Param("month") Integer month);
}