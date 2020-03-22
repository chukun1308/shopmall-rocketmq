package com.chukun.rocket.store.service.provider;

import com.alibaba.dubbo.config.annotation.Service;
import com.chukun.rocket.spi.store.StoreSpiService;
import com.chukun.rocket.store.mapper.StoreMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

@Service(
        version = "1.0.0",
        application = "${dubbo.application.id}",
        protocol = "${dubbo.protocol.id}",
        registry = "${dubbo.registry.id}"
)
public class StoreServiceProvider implements StoreSpiService {

    @Autowired
    private StoreMapper storeMapper;

    @Override
    public int selectVersion(String supplierId, String goodsId) {
        return storeMapper.selectVersion(supplierId,goodsId);
    }

    @Override
    public int updateStoreCountByVersion(int version, String supplierId, String goodsId, String updateBy, Date updateTime) {
        return storeMapper.updateStoreCountByVersion(version,supplierId,goodsId,updateBy,updateTime);
    }

    @Override
    public int selectStoreCount(String supplierId, String goodsId) {
        return storeMapper.selectStoreCount(supplierId,goodsId);
    }

    @Override
    public int revokeStoreCount(String supplierId, String goodsId, String updateBy, Date updateTime, int version) {
        return storeMapper.revokeStoreCount(supplierId,goodsId,updateBy,updateTime,version);
    }
}
