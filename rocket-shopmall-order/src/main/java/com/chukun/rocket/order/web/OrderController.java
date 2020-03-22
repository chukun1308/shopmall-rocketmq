package com.chukun.rocket.order.web;

import com.chukun.rocket.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {


    @Autowired
    private OrderService orderService;

    @RequestMapping("/createOrder")
    public String createOrder(@RequestParam("cityId")String cityId,
                              @RequestParam("platformId")String platformId,
                              @RequestParam("userId")String userId,
                              @RequestParam("supplierId")String supplierId,
                              @RequestParam("goodsId")String goodsId) throws Exception {

        return orderService.createOrder(cityId, platformId, userId, supplierId, goodsId) ? "下单成功!" : "下单失败!";
    }
}
