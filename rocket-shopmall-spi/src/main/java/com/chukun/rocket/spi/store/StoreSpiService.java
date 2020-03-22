package com.chukun.rocket.spi.store;

import java.util.Date;

/**
 * 库存对外提供的rpc接口
 */
public interface StoreSpiService {

    /**
     * 查询库存的版本号
     * @param supplierId
     * @param goodsId
     * @return
     */
     int selectVersion(String supplierId, String goodsId);

    /**
     * 更新库存
     * @param version
     * @param supplierId
     * @param goodsId
     * @param updateBy
     * @param updateTime
     * @return
     */
     int updateStoreCountByVersion(int version, String supplierId, String goodsId, String updateBy,
                                         Date updateTime);

    /**
     * 查询库存
     * @param supplierId
     * @param goodsId
     * @return
     */
     int selectStoreCount(String supplierId, String goodsId);

    /**
     * 下单失败，还回库存
     * @param supplierId
     * @param goodsId
     * @param updateBy
     * @param updateTime
     * @param version
     * @return
     */
     int revokeStoreCount(String supplierId, String goodsId, String updateBy,
                          Date updateTime,int version);
}
