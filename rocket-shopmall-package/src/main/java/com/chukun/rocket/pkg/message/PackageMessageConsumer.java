package com.chukun.rocket.pkg.message;

import com.chukun.rocket.common.constants.RocketMqMessageConstants;
import com.chukun.rocket.common.utils.FastJsonConvertUtil;
import com.chukun.rocket.pkg.dto.OrderPackageDto;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
public class PackageMessageConsumer {

    private DefaultMQPushConsumer consumer;

    private PackageMessageConsumer() {
        try {
            this.consumer = new DefaultMQPushConsumer(RocketMqMessageConstants.ORDER_TO_PACKAGE_MESSAGE_CONSUMER_GROUP);
            this.consumer.setConsumeThreadMin(10);
            this.consumer.setConsumeThreadMin(30);
            this.consumer.setNamesrvAddr(RocketMqMessageConstants.DOUBLE_NAME_SERVER);
            this.consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
            this.consumer.subscribe(RocketMqMessageConstants.ORDER_TO_PACKAGE_MESSAGE_TOPIC, RocketMqMessageConstants.ORDER_TO_PACKAGE_MESSAGE_TAG);
            this.consumer.setMessageListener(new PackageOrderlyListener());
            this.consumer.start();
        }catch (MQClientException e){
            throw new RuntimeException("PackageMessageConsumer start error",e);
        }
    }

    class PackageOrderlyListener implements MessageListenerOrderly {

        Random random = new Random();

        @Override
        public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {

            for(MessageExt msg: msgs) {
                try {
                    String topic = msg.getTopic();
                    String msgBody = new String(msg.getBody(), "utf-8");
                    String tags = msg.getTags();
                    String keys = msg.getKeys();
                    System.err.println("收到消息：" + "  topic :" + topic + "  ,tags : " + tags + "keys :" + keys + ", msg : " + msgBody);

                    OrderPackageDto packageDto = FastJsonConvertUtil.convertJSONToObject(msgBody, OrderPackageDto.class);
                    //	模拟实际的业务耗时操作
                    //	PS: 创建包裹信息  、对物流的服务调用（异步调用）
                    TimeUnit.SECONDS.sleep(random.nextInt(3) + 1);

                    System.err.println("业务操作: " + packageDto.getMessage());

                } catch (Exception e) {
                    e.printStackTrace();
                    return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                }
            }

            return ConsumeOrderlyStatus.SUCCESS;
        }

    }
}
