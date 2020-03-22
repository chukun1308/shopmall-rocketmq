package com.chukun.rocket.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"com.chukun.rocket.order.*"})
@MapperScan(basePackages = {"com.chukun.rocket.order.mapper.*"})
public class MainRocketShopmallOrderConfiguration {
}
