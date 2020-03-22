package com.chukun.rocket.pay.service.impl;

import com.chukun.rocket.common.constants.RocketMqMessageConstants;
import com.chukun.rocket.common.utils.FastJsonConvertUtil;
import com.chukun.rocket.pay.dto.CustomerAccountUpdateDto;
import com.chukun.rocket.pay.entity.CustomerAccount;
import com.chukun.rocket.pay.mapper.CustomerAccountMapper;
import com.chukun.rocket.pay.message.CallBackMessage2OrderService;
import com.chukun.rocket.pay.message.TransactionProducer;
import com.chukun.rocket.pay.service.PayService;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class PayServiceImpl implements PayService {

    @Autowired
    private CustomerAccountMapper customerAccountMapper;
    @Autowired
    private TransactionProducer transactionProducer;
    @Autowired
    private CallBackMessage2OrderService callBackMessage2OrderService;

    @Override
    public String payment(String userId, String orderId, String accountId, double money) {
        String paymentRet="";
        try{
            BigDecimal payMoney = new BigDecimal(money);
            CustomerAccount oldAccount = customerAccountMapper.selectByPrimaryKey(accountId);
            int currentVersion = oldAccount.getVersion();
            //	要对大概率事件进行提前预判（小概率事件我们做放过,但是最后保障数据的一致性即可）
            //业务出发:
            //当前一个用户账户 只允许一个线程（一个应用端访问）
            //技术出发：
            //1 redis去重 分布式锁
            //2 数据库乐观锁去重
            //	做扣款操作的时候：获得分布式锁，看一下能否获得
            BigDecimal newBalance = oldAccount.getCurrentBalance().subtract(payMoney);
            if(newBalance.compareTo(BigDecimal.ZERO)>0){
                //组装消息，执行本地事务
                String payMessageKeys = UUID.randomUUID().toString().replaceAll("-","").substring(0,10)+"$"+System.currentTimeMillis();
                CustomerAccountUpdateDto accountUpdateDto = new CustomerAccountUpdateDto();
                accountUpdateDto.setAccountId(accountId);
                accountUpdateDto.setOrderId(orderId);
                accountUpdateDto.setUserId(userId);
                accountUpdateDto.setPayMoney(payMoney);
                //创建消息
                Message message = new Message(RocketMqMessageConstants.PAY_TO_PAY_BAK_MESSAGE_TOPIC,
                        RocketMqMessageConstants.PAY_TO_PAY_BAK_MESSAGE_TAG, payMessageKeys,
                        FastJsonConvertUtil.convertObjectToJSON(accountUpdateDto).getBytes());
                accountUpdateDto.setNewBalance(newBalance);
                accountUpdateDto.setCurrentVersion(currentVersion);
                //同步阻塞
                CountDownLatch latch = new CountDownLatch(1);
                accountUpdateDto.setLatch(latch);
                //发送消息
                TransactionSendResult transactionSendResult = transactionProducer.sendMessage(message, accountUpdateDto);
                latch.await(3, TimeUnit.SECONDS);
                if(transactionSendResult.getSendStatus()== SendStatus.SEND_OK
                         && transactionSendResult.getLocalTransactionState() == LocalTransactionState.COMMIT_MESSAGE){
                    //	回调order通知支付成功消息
                    callBackMessage2OrderService.sendOKMessage(orderId, userId);
                    paymentRet = "支付成功!";
                }else{
                    paymentRet = "支付失败!";
                }
            }else{
                paymentRet = "余额不足";
            }
        }catch (Exception e){
            e.printStackTrace();
            paymentRet = "支付失败!";
        }
        return paymentRet;
    }
}
