package com.chukun.rocket.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.chukun.rocket.common.constants.RocketMqMessageConstants;
import com.chukun.rocket.common.utils.FastJsonConvertUtil;
import com.chukun.rocket.order.dto.OrderPackageDto;
import com.chukun.rocket.order.entity.Order;
import com.chukun.rocket.order.enums.OrderStatus;
import com.chukun.rocket.order.mapper.OrderMapper;
import com.chukun.rocket.order.service.OrderService;
import com.chukun.rocket.order.service.producer.OrderlyProducer;
import com.chukun.rocket.spi.store.StoreSpiService;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderlyProducer orderlyProducer;

    @Reference(version = "1.0.0",
            application = "${dubbo.application.id}",
            interfaceClass = StoreSpiService.class,
            check = false,
            timeout = 1000,
            retries = 0
    )
    private StoreSpiService storeSpiService;
    @Override
    public boolean createOrder(String cityId, String platformId, String userId, String supplierId, String goodsId) {
        boolean orderSuccess = true;
        try{
            Order order = new Order();
            order.setOrderId(UUID.randomUUID().toString().substring(0, 32));
            order.setOrderType("1");
            order.setCityId(cityId);
            order.setPlatformId(platformId);
            order.setUserId(userId);
            order.setSupplierId(supplierId);
            order.setGoodsId(goodsId);
            order.setOrderStatus(OrderStatus.ORDER_CREATED.getValue());
            order.setRemark("");

            Date currentTime = new Date();
            order.setCreateBy("admin");
            order.setCreateTime(currentTime);
            order.setUpdateBy("admin");
            order.setUpdateTime(currentTime);

            //先查询库存的当前版本号
            int currentVersion = storeSpiService.selectVersion(supplierId,goodsId);
            //先更新库存,这里需要考虑，更新库存成功，rpc异常，也需要还回库存的操作
            int updateCount = storeSpiService.updateStoreCountByVersion(currentVersion, supplierId, goodsId, "admin", new Date());

            if(updateCount==1){
                //更新库存成功
                try {
                    //如果出现SQL异常 入库失败, 那么要对 库存的数量 和版本号进行回滚操作
                    orderMapper.insert(order);
                }catch (SQLException e){
                    //需要还回库存
                    storeSpiService.revokeStoreCount(supplierId,goodsId,"admin",new Date(),currentVersion);
                }
            }else if(updateCount==0){//	没有更新成功 1 高并发时乐观锁生效   2 库存不足
                orderSuccess = false;	//	下单标识失败
                int currentStoreCount = storeSpiService.selectStoreCount(supplierId, goodsId);
                if(currentStoreCount==0){
                    System.err.println("-----当前库存不足...");
                }else{
                    //{flag:false , messageCode: 004 , message: 乐观锁生效}
                    System.err.println("-----乐观锁生效...");
                }
            }

        }catch (Exception e){
           orderSuccess = false;
        }
        return orderSuccess;
    }

    @Override
    public void sendOrderlyMessage2Package(String userId, String orderId) {
        List<Message> messageList = new ArrayList<>();
        try{
            //创建包裹的步骤一
            OrderPackageDto createPackageDto = new OrderPackageDto();
            createPackageDto.setUserId(userId);
            createPackageDto.setOrderId(orderId);
            createPackageDto.setMessage("创建包裹操作-->01");
            String createPackageMessageKeys = UUID.randomUUID().toString()
                       .replaceAll("-","").substring(0,10)+"$"+System.currentTimeMillis();
            Message createPackageMessage = new Message(RocketMqMessageConstants.ORDER_TO_PACKAGE_MESSAGE_TOPIC,
                           RocketMqMessageConstants.ORDER_TO_PACKAGE_MESSAGE_TAG,createPackageMessageKeys, FastJsonConvertUtil.convertObjectToJSON(createPackageDto).getBytes());
            messageList.add(createPackageMessage);

            //创建包裹步骤二
            OrderPackageDto sendDeliveryFlowDto = new OrderPackageDto();
            sendDeliveryFlowDto.setUserId(userId);
            sendDeliveryFlowDto.setOrderId(orderId);
            sendDeliveryFlowDto.setMessage("发送物流通知操作-->02");

            String sendDeliveryFlowMessageKeys = UUID.randomUUID().toString()
                    .replaceAll("-","").substring(0,10)+"$"+System.currentTimeMillis();
            Message sendDeliveryFlowMessage = new Message(RocketMqMessageConstants.ORDER_TO_PACKAGE_MESSAGE_TOPIC,
                    RocketMqMessageConstants.ORDER_TO_PACKAGE_MESSAGE_TAG,sendDeliveryFlowMessageKeys, FastJsonConvertUtil.convertObjectToJSON(sendDeliveryFlowDto).getBytes());

            messageList.add(sendDeliveryFlowMessage);
            //	顺序消息投递 是应该按照 供应商ID 与topic 和 messagequeueId 进行绑定对应的
            //  supplier_id
            Order order = orderMapper.selectByPrimaryKey(orderId);
            int messageSequence = Integer.parseInt(order.getSupplierId());
            //发送顺序消息
            orderlyProducer.sendOrderlyMessages(messageList,messageSequence);
        }catch (Exception e){
           e.printStackTrace();
        }

    }
}
