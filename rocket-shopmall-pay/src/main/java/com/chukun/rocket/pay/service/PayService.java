package com.chukun.rocket.pay.service;

public interface PayService {

	String payment(String userId, String orderId, String accountId, double money);
}
