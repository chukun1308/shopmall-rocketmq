package com.chukun.rocket.order.service.consumer;

import com.alibaba.dubbo.config.annotation.Reference;
import com.chukun.rocket.spi.store.HelloWorldSpiService;
import org.springframework.stereotype.Service;

@Service
public class HelloServiceConsumer {

    @Reference(version = "1.0.0",
            application = "${dubbo.application.id}",
            interfaceClass = HelloWorldSpiService.class,
            check = false,
            timeout = 1000,
            retries = 0
    )
    private HelloWorldSpiService helloWorldSpiService;


    public String sayHello(String name){
        System.out.println("the order consumer be called ");
        return helloWorldSpiService.sayHello(name);
    }
}
