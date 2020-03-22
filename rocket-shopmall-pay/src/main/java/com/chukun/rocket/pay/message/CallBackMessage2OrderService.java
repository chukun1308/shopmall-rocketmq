package com.chukun.rocket.pay.message;

import com.chukun.rocket.common.constants.RocketMqMessageConstants;
import com.chukun.rocket.common.utils.FastJsonConvertUtil;
import com.chukun.rocket.pay.dto.Pay2OrderUpdateDto;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class CallBackMessage2OrderService {

    @Autowired
    private SyncProducer syncProducer;


    public void sendOKMessage(String orderId, String userId) {

        Pay2OrderUpdateDto orderUpdateDto = new Pay2OrderUpdateDto();
        orderUpdateDto.setUserId(userId);
        orderUpdateDto.setOrderId(orderId);
        orderUpdateDto.setStatus(2); //ok

        String keys = UUID.randomUUID().toString().replaceAll("-","").substring(0,10) + "$" + System.currentTimeMillis();
        Message message = new Message(RocketMqMessageConstants.PAY_TO_ORDER_MESSAGE_TOPIC, RocketMqMessageConstants.PAY_TO_ORDER_MESSAGE_TAG,
                keys, FastJsonConvertUtil.convertObjectToJSON(orderUpdateDto).getBytes());

         syncProducer.sendMessage(message);
    }
}
