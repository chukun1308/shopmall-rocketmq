package com.chukun.rocket.order.service.consumer;

import com.chukun.rocket.common.constants.RocketMqMessageConstants;
import com.chukun.rocket.common.utils.FastJsonConvertUtil;
import com.chukun.rocket.order.dto.Pay2OrderUpdateDto;
import com.chukun.rocket.order.enums.OrderStatus;
import com.chukun.rocket.order.mapper.OrderMapper;
import com.chukun.rocket.order.service.OrderService;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class OrderPayConsumer {

    private DefaultMQPushConsumer consumer;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderService orderService;

    public OrderPayConsumer(){
        try {
            consumer = new DefaultMQPushConsumer(RocketMqMessageConstants.PAY_TO_ORDER_MESSAGE_CONSUMER_GROUP);
            consumer.setConsumeThreadMin(10);
            consumer.setConsumeThreadMax(50);
            consumer.setNamesrvAddr(RocketMqMessageConstants.DOUBLE_NAME_SERVER);
            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
            consumer.subscribe(RocketMqMessageConstants.PAY_TO_ORDER_MESSAGE_TOPIC, RocketMqMessageConstants.PAY_TO_ORDER_MESSAGE_TAG);
            consumer.registerMessageListener(new MessageListenerConcurrently2Pay());
            consumer.start();
        }catch (MQClientException e){
            throw  new RuntimeException("OrderPayConsumer start error",e);
        }
    }


    class MessageListenerConcurrently2Pay implements MessageListenerConcurrently{

        @Override
        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
            MessageExt messageExt = list.get(0);
            try{
                String topic = messageExt.getTopic();
                String msgBody = new String(messageExt.getBody(), "utf-8");
                String tags = messageExt.getTags();
                String keys = messageExt.getKeys();
                System.err.println("收到消息：" + "  topic :" + topic + "  ,tags : " + tags + "keys :" + keys + ", msg : " + msgBody);
                String orignMsgId = messageExt.getProperties().get(MessageConst.PROPERTY_ORIGIN_MESSAGE_ID);
                System.err.println("orignMsgId: " + orignMsgId);

                //通过keys 进行去重表去重 或者使用redis进行去重???? --> 不需要,幂等操作
                Pay2OrderUpdateDto orderUpdateDto = FastJsonConvertUtil.convertJSONToObject(msgBody, Pay2OrderUpdateDto.class);
                if (orderUpdateDto.getStatus() == Integer.parseInt(OrderStatus.ORDER_PAYED.getValue())) {
                    Date currentTime = new Date();
                    int count  = orderMapper.updateOrderStatus(orderUpdateDto.getOrderId(), orderUpdateDto.getStatus()+"", "admin", currentTime);
                    if(count!=0){
                        //发送物流消息
                        orderService.sendOrderlyMessage4Pkg(orderUpdateDto.getUserId(),orderUpdateDto.getOrderId());
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
    }


}
