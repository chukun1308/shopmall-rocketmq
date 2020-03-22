package com.chukun.rocket.order.service;

public interface OrderService {

	boolean createOrder(String cityId, String platformId, String userId, String supplierId, String goodsId);

	void sendOrderlyMessage2Package(String userId, String orderId);

	
	
}
