package com.chukun.rocket.store;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"com.chukun.rocket.store.*"})
@MapperScan(basePackages = {"com.chukun.rocket.store.mapper.*"})
public class MainRocketShopmallStoreConfiguration {
}
