package com.chukun.rocket.order.mapper;

import java.sql.SQLException;
import java.util.Date;

import com.chukun.rocket.order.entity.Order;
import org.apache.ibatis.annotations.Param;


public interface OrderMapper {
    int deleteByPrimaryKey(String orderId);

    int insert(Order record) throws SQLException;

    int insertSelective(Order record);

    Order selectByPrimaryKey(String orderId);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);

	int updateOrderStatus(@Param("orderId") String orderId, @Param("orderStatus") String orderStatus, @Param("updateBy") String updateBy, @Param("updateTime") Date updateTime);
}