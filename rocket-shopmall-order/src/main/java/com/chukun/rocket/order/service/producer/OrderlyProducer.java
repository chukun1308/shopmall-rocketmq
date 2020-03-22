package com.chukun.rocket.order.service.producer;


import java.util.List;

import com.chukun.rocket.common.constants.RocketMqMessageConstants;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.stereotype.Component;

@Component
public class OrderlyProducer {

	private DefaultMQProducer producer;
	

	
	private OrderlyProducer() {
		this.producer = new DefaultMQProducer(RocketMqMessageConstants.ORDER_TO_PACKAGE_MESSAGE_PRODUCER_GROUP);
		this.producer.setNamesrvAddr(RocketMqMessageConstants.DOUBLE_NAME_SERVER);
		this.producer.setSendMsgTimeout(3000);
		start();
	}
	
	public void start() {
		try {
			this.producer.start();
		} catch (MQClientException e) {
			e.printStackTrace();
		}
	}
	
	public void shutdown() {
		this.producer.shutdown();
	}

	/**
	 * 发送顺序消息
	 * @param messageList
	 * @param messageQueueNumber
	 */
	public void sendOrderlyMessages(List<Message> messageList, int messageQueueNumber) {
		for(Message me : messageList) {
			try {
				this.producer.send(me, new MessageQueueSelector() {
					@Override
					public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
						Integer id = (Integer)arg%mqs.size();
						return mqs.get(id);
					}
				}, messageQueueNumber);
			} catch (MQClientException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RemotingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MQBrokerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
