package com.chukun.rocket.pay.message;

import java.util.Date;
import java.util.List;
import com.chukun.rocket.common.constants.RocketMqMessageConstants;
import com.chukun.rocket.common.utils.FastJsonConvertUtil;
import com.chukun.rocket.pay.dto.CustomerAccountUpdateDto;
import com.chukun.rocket.pay.entity.PlatformAccount;
import com.chukun.rocket.pay.mapper.PlatformAccountMapper;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PayBakConsumer {
	
	@Autowired
	private PlatformAccountMapper platformAccountMapper;

	private DefaultMQPushConsumer consumer;
	
	private PayBakConsumer() {
		try {
			this.consumer = new DefaultMQPushConsumer(RocketMqMessageConstants.PAY_TO_PAY_BAK_MESSAGE_CONSUMER_GROUP);
			this.consumer.setConsumeThreadMin(10);
			this.consumer.setConsumeThreadMax(30);
			this.consumer.setNamesrvAddr(RocketMqMessageConstants.DOUBLE_NAME_SERVER);
			this.consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
			this.consumer.subscribe(RocketMqMessageConstants.PAY_TO_PAY_BAK_MESSAGE_TOPIC, RocketMqMessageConstants.PAY_TO_PAY_BAK_MESSAGE_TAG);
			this.consumer.registerMessageListener(new MessageListenerConcurrently2Pay());
			this.consumer.start();
		} catch (MQClientException e) {
			e.printStackTrace();
		}
	}
	
	class MessageListenerConcurrently2Pay implements MessageListenerConcurrently {

		@Override
		public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
			MessageExt msg = msgs.get(0);
			try {
				String topic = msg.getTopic();
				String tags = msg.getTags();
				String keys = msg.getKeys();
				String body = new String(msg.getBody(), RemotingHelper.DEFAULT_CHARSET);
				System.err.println("收到事务消息, topic: " + topic + ", tags: " + tags + ", keys: " + keys + ", body: " + body);
			
				//	消息一单过来的时候（去重 幂等操作）
				//	数据库主键去重<去重表 keys>
				// 	insert table --> insert ok & primary key
				CustomerAccountUpdateDto accountUpdateDto = FastJsonConvertUtil.convertJSONToObject(body, CustomerAccountUpdateDto.class);
				
				PlatformAccount pa = platformAccountMapper.selectByPrimaryKey("platform001");	//	当前平台的一个账号
				pa.setCurrentBalance(pa.getCurrentBalance().add(accountUpdateDto.getPayMoney()));
				Date currentTime = new Date();
				pa.setVersion(pa.getVersion() + 1);
				pa.setDateTime(currentTime);
				pa.setUpdateTime(currentTime);
				platformAccountMapper.updateByPrimaryKeySelective(pa);
			} catch (Exception e) {
				e.printStackTrace();
				//msg.getReconsumeTimes();
				//	如果处理多次操作还是失败, 记录失败日志（做补偿 回顾 人工处理）
				return ConsumeConcurrentlyStatus.RECONSUME_LATER;
			}
			return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
