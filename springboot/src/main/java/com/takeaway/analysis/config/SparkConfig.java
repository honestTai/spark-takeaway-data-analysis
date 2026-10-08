package com.takeaway.analysis.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spark配置类
 */
@Slf4j
@Configuration
public class SparkConfig {

    @Value("${spark.app-name}")
    private String appName;

    @Value("${spark.master}")
    private String master;

    @Bean
    public SparkSession sparkSession() {
        log.info("正在初始化 SparkSession，appName={}, master={}", appName, master);
        
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        
        SparkConf conf = new SparkConf()
                .setAppName(appName)
                .setMaster(master)
                .set("spark.sql.warehouse.dir", "file:///tmp/spark-warehouse")
                .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                .set("spark.driver.host", "localhost")
                .set("spark.driver.userClassPathFirst", "true");
        
        SparkSession session = SparkSession.builder()
                .config(conf)
                .getOrCreate();
        
        log.info("SparkSession 初始化完成");
        return session;
    }
}

