package com.chukun.rocket.store.service.provider;

import com.alibaba.dubbo.config.annotation.Service;
import com.chukun.rocket.spi.store.HelloWorldSpiService;

@Service(
        version = "1.0.0",
        application = "${dubbo.application.id}",
        protocol = "${dubbo.protocol.id}",
        registry = "${dubbo.registry.id}"
)
public class HelloServiceProvider implements HelloWorldSpiService {

    @Override
    public String sayHello(String name) {
        System.out.println("the store spi be called");
        return "hello ** "+name;
    }
}
