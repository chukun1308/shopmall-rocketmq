package com.chukun.rocket.pay.message;

import com.chukun.rocket.common.constants.RocketMqMessageConstants;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.stereotype.Component;

@Component
public class SyncProducer {

    private DefaultMQProducer producer;


    private SyncProducer() {
        this.producer = new DefaultMQProducer(RocketMqMessageConstants.PAY_TO_ORDER_MESSAGE_PRODUCER_GROUP);
        this.producer.setNamesrvAddr(RocketMqMessageConstants.DOUBLE_NAME_SERVER);
        this.producer.setRetryTimesWhenSendFailed(3);
        start();
    }

    public void start() {
        try {
            this.producer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }

    public SendResult sendMessage(Message message) {
        SendResult sendResult = null;
        try {
            sendResult = this.producer.send(message);
        } catch (MQClientException e) {
            e.printStackTrace();
        } catch (RemotingException e) {
            e.printStackTrace();
        } catch (MQBrokerException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return sendResult;
    }
}
