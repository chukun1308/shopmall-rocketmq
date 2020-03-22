package com.chukun.rocket.pay;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = "com.chukun.rocket.pay.mapper")
@ComponentScan(basePackages = {"com.chukun.rocket.pay.*", "com.chukun.rocket.pay.config.*"})
public class MainRocketShopmallPayBakConfig {

}
