package com.chukun.rocket.pkg;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = {"com.chukun.rocket.pkg.mapper.*"})
@ComponentScan(basePackages = {"com.chukun.rocket.pkg.*","com.chukun.rocket.pkg.config.*"})
public class MainRocketShopmallPackageConfig {
}
