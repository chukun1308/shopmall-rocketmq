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

            //?????????????????????????????????
            int currentVersion = storeSpiService.selectVersion(supplierId,goodsId);
            //???????????????,??????????????????????????????????????????rpc???????????????????????????????????????
            int updateCount = storeSpiService.updateStoreCountByVersion(currentVersion, supplierId, goodsId, "admin", new Date());

            if(updateCount==1){
                //??????????????????
                try {
                    //????????????SQL?????? ????????????, ???????????? ??????????????? ??????????????????????????????
                    orderMapper.insert(order);
                }catch (SQLException e){
                    //??????????????????
                    storeSpiService.revokeStoreCount(supplierId,goodsId,"admin",new Date(),currentVersion);
                }
            }else if(updateCount==0){//	?????????????????? 1 ???????????????????????????   2 ????????????
                orderSuccess = false;	//	??????????????????
                int currentStoreCount = storeSpiService.selectStoreCount(supplierId, goodsId);
                if(currentStoreCount==0){
                    System.err.println("-----??????????????????...");
                }else{
                    //{flag:false , messageCode: 004 , message: ???????????????}
                    System.err.println("-----???????????????...");
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
            //????????????????????????
            OrderPackageDto createPackageDto = new OrderPackageDto();
            createPackageDto.setUserId(userId);
            createPackageDto.setOrderId(orderId);
            createPackageDto.setMessage("??????????????????-->01");
            String createPackageMessageKeys = UUID.randomUUID().toString()
                       .replaceAll("-","").substring(0,10)+"$"+System.currentTimeMillis();
            Message createPackageMessage = new Message(RocketMqMessageConstants.ORDER_TO_PACKAGE_MESSAGE_TOPIC,
                           RocketMqMessageConstants.ORDER_TO_PACKAGE_MESSAGE_TAG,createPackageMessageKeys, FastJsonConvertUtil.convertObjectToJSON(createPackageDto).getBytes());
            messageList.add(createPackageMessage);

            //?????????????????????
            OrderPackageDto sendDeliveryFlowDto = new OrderPackageDto();
            sendDeliveryFlowDto.setUserId(userId);
            sendDeliveryFlowDto.setOrderId(orderId);
            sendDeliveryFlowDto.setMessage("????????????????????????-->02");

            String sendDeliveryFlowMessageKeys = UUID.randomUUID().toString()
                    .replaceAll("-","").substring(0,10)+"$"+System.currentTimeMillis();
            Message sendDeliveryFlowMessage = new Message(RocketMqMessageConstants.ORDER_TO_PACKAGE_MESSAGE_TOPIC,
                    RocketMqMessageConstants.ORDER_TO_PACKAGE_MESSAGE_TAG,sendDeliveryFlowMessageKeys, FastJsonConvertUtil.convertObjectToJSON(sendDeliveryFlowDto).getBytes());

            messageList.add(sendDeliveryFlowMessage);
            //	?????????????????? ??????????????? ?????????ID ???topic ??? messagequeueId ?????????????????????
            //  supplier_id
            Order order = orderMapper.selectByPrimaryKey(orderId);
            int messageSequence = Integer.parseInt(order.getSupplierId());
            //??????????????????
            orderlyProducer.sendOrderlyMessages(messageList,messageSequence);
        }catch (Exception e){
           e.printStackTrace();
        }

    }
}
