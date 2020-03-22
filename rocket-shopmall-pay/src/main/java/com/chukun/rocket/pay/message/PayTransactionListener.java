package com.chukun.rocket.pay.message;

import com.chukun.rocket.pay.dto.CustomerAccountUpdateDto;
import com.chukun.rocket.pay.mapper.CustomerAccountMapper;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

@Component
public class PayTransactionListener implements TransactionListener {

    @Autowired
    private CustomerAccountMapper accountMapper;

    @Override
    public LocalTransactionState executeLocalTransaction(Message message, Object o) {
        System.err.println("执行本地事务单元------------");
        CountDownLatch currentCountDown = null;
        try {
            CustomerAccountUpdateDto accountUpdateDto = (CustomerAccountUpdateDto) o;
            String accountId = accountUpdateDto.getAccountId();
            BigDecimal newBalance = accountUpdateDto.getNewBalance();
            int currentVersion = accountUpdateDto.getCurrentVersion();
            Date currentDate = new Date();
            int count = accountMapper.updateBalance(accountId, newBalance, currentVersion, currentDate);
            if(count!=0){
                currentCountDown.countDown();
                return LocalTransactionState.COMMIT_MESSAGE;
            }else{
                currentCountDown.countDown();
                return LocalTransactionState.ROLLBACK_MESSAGE;
            }
        }catch (Exception e){
          e.printStackTrace();
          currentCountDown.countDown();
          return LocalTransactionState.ROLLBACK_MESSAGE;
        }
    }

    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
        return null;
    }
}
