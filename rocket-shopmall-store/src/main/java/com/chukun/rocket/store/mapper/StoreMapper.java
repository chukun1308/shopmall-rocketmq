package com.chukun.rocket.store.mapper;

import java.util.Date;

import com.chukun.rocket.store.entity.Store;
import org.apache.ibatis.annotations.Param;


public interface StoreMapper {
    int deleteByPrimaryKey(String storeId);

    int insert(Store record);

    int insertSelective(Store record);

    Store selectByPrimaryKey(String storeId);

    int updateByPrimaryKeySelective(Store record);

    int updateByPrimaryKey(Store record);

	int selectVersion(@Param("supplierId") String supplierId, @Param("goodsId") String goodsId);

	int updateStoreCountByVersion(@Param("version") int version, @Param("supplierId") String supplierId, @Param("goodsId") String goodsId, @Param("updateBy") String updateBy, @Param("updateTime") Date updateTime);

	int selectStoreCount(@Param("supplierId") String supplierId, @Param("goodsId") String goodsId);

	int revokeStoreCount(@Param("supplierId") String supplierId, @Param("goodsId") String goodsId, @Param("updateBy") String updateBy, @Param("updateTime") Date updateTime,@Param("version") int version);

}