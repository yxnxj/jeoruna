package com.prography1.eruna.batch.scheduler;

import jakarta.persistence.EntityManagerFactory;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import javax.xml.crypto.Data;
import java.util.Properties;

@Configuration
@Import(TestRedisConfig.class)
//@ActiveProfiles("local")
class CustomConfig {


//    @Bean
//    public DataSource dataSource() {
//        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
//        dataSourceBuilder.driverClassName("com.mysql.cj.jdbc.Driver");
//        dataSourceBuilder.url("jdbc:mysql://localhost:3306/eruna?serverTimezone=Asia/Seoul&characterEncoding=UTF-8&&createDatabaseIfNotExist=true");
//        dataSourceBuilder.username("root");
//        dataSourceBuilder.password("jo1219");
//        return dataSourceBuilder.build();
//    }

//    @Autowired
//    DataSource dataSource;
//    @Bean
//    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
//        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
////        em.setDataSource(dataSource());
//        em.setDataSource(dataSource);
//        em.setPackagesToScan(new String[]{"\\com.prography1.eruna.domain"});
//
//        Properties properties = new Properties();
//        properties.setProperty("show-sql", "true");
//        properties.setProperty("hibernate.format_sql", "true");
//        properties.setProperty("hibernate.physical_naming_strategy",
//                "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
//        properties.setProperty("spring.data.redis.host", "127.0.0.1");
//        properties.setProperty("spring.data.redis.port", "6379");
//
//        em.setJpaProperties(properties);
//
//        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
//        em.setJpaVendorAdapter(vendorAdapter);
//        em.afterPropertiesSet();
//
//        return em;
//    }
//
//    @Bean
//    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
//        JpaTransactionManager transactionManager = new JpaTransactionManager();
//        transactionManager.setEntityManagerFactory(emf);
//
//        return transactionManager;
//    }



//    @Bean
//    public JobRepositoryFactoryBean jobRepository(DataSource dataSource, PlatformTransactionManager transactionManager) {
//        JobRepositoryFactoryBean jobRepositoryFactoryBean = new JobRepositoryFactoryBean();
//        jobRepositoryFactoryBean.setDataSource(dataSource);
//        jobRepositoryFactoryBean.setTransactionManager(transactionManager);
//        return jobRepositoryFactoryBean;
//    }

}
