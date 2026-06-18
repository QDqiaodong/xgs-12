package com.retail.material.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.retail.material.entity.TransferItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TransferItemMapper extends BaseMapper<TransferItem> {

    @Select("SELECT * FROM transfer_item WHERE transfer_order_id = #{orderId} AND deleted = 0")
    List<TransferItem> selectByOrderId(Long orderId);
}