package com.chukun.rocket.order.web;

import com.chukun.rocket.order.service.consumer.HelloServiceConsumer;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

    @Autowired
    private HelloServiceConsumer helloServiceConsumer;

    @RequestMapping("/index")
    public String index(@RequestParam("name")String name){
        return helloServiceConsumer.sayHello(name);
    }

    /**
     * 使用hystrix做服务降级操作
     * 超时降级
     * @return
     */
    @HystrixCommand(commandKey = "IndexController.sayHello",
                      commandProperties = {
                        @HystrixProperty(name="execution.timeout.enabled", value="true"),
						@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds", value="3000")
                      },
              fallbackMethod = "sayHelloFallbackMethod4Timeout"
             )
    @RequestMapping("/sayHello")
    public String sayHello() throws InterruptedException {
        Thread.sleep(4000);
        return "hello";
    }

    public String sayHelloFallbackMethod4Timeout()throws InterruptedException{
        System.err.println("-------超时降级策略执行------------");
        return "hystrix sayHello timeout";
    }
}
