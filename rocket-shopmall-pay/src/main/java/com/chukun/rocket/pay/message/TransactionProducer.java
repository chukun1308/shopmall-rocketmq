package com.chukun.rocket.pay.message;

import com.chukun.rocket.common.constants.RocketMqMessageConstants;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@Component
public class TransactionProducer implements InitializingBean {

	private TransactionMQProducer producer;
	
	private ExecutorService executorService;

	@Autowired
	private PayTransactionListener payTransactionListener;
	
	private TransactionProducer() {
		this.producer = new TransactionMQProducer(RocketMqMessageConstants.PAY_TO_PAY_BAK_MESSAGE_PRODUCER_GROUP);
		this.executorService = new ThreadPoolExecutor(2, 5, 100, TimeUnit.SECONDS,
				new ArrayBlockingQueue<Runnable>(2000), new ThreadFactory() {
					@Override
					public Thread newThread(Runnable r) {
						Thread thread = new Thread(r);
						thread.setName(RocketMqMessageConstants.PAY_TO_PAY_BAK_MESSAGE_PRODUCER_GROUP + "-check-thread");
						return thread;
					}
				});
		this.producer.setExecutorService(executorService);
		this.producer.setNamesrvAddr(RocketMqMessageConstants.DOUBLE_NAME_SERVER);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		try {
			this.producer.setTransactionListener(payTransactionListener);
			this.producer.start();
		}catch (Exception e){
			throw new RuntimeException("mq producer start error",e);
		}
	}

	public void start(TransactionListener transactionListener){
		try {
			this.producer.setTransactionListener(transactionListener);
			this.producer.start();
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public void shutdown() {
		this.producer.shutdown();
	}
	
	public TransactionSendResult sendMessage(Message message, Object argument) {
		TransactionSendResult sendResult = null;
		try {
			sendResult = this.producer.sendMessageInTransaction(message, argument);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sendResult;
	}
}
