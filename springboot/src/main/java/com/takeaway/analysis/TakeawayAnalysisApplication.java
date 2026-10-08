package com.takeaway.analysis;

import com.takeaway.analysis.service.CleaningRuleService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;

/**
 * 基于Spark的外卖数据分析系统
 * 
 * @author system
 */
@SpringBootApplication
@MapperScan("com.takeaway.analysis.mapper")
@EnableScheduling
public class TakeawayAnalysisApplication implements CommandLineRunner {

    @Autowired
    private CleaningRuleService cleaningRuleService;

    public static void main(String[] args) {
        // Windows 环境下设置 Hadoop 环境变量（必须在 Spring 初始化之前）
        initHadoopHome();
        SpringApplication.run(TakeawayAnalysisApplication.class, args);
    }
    
    /**
     * 初始化 Hadoop 环境（Windows 兼容性处理）
     */
    private static void initHadoopHome() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows")) {
            String hadoopHome = System.getProperty("user.dir") + File.separator + "hadoop";
            File hadoopDir = new File(hadoopHome);
            File binDir = new File(hadoopHome + File.separator + "bin");
            
            // 创建必要的目录
            if (!hadoopDir.exists()) {
                hadoopDir.mkdirs();
            }
            if (!binDir.exists()) {
                binDir.mkdirs();
            }
            
            // 设置环境变量
            System.setProperty("hadoop.home.dir", hadoopHome);
            System.out.println("Windows环境检测，设置 hadoop.home.dir = " + hadoopHome);
        }
    }

    @Override
    public void run(String... args) {
        // 初始化默认清洗规则
        cleaningRuleService.initDefaultRules();
    }
}

