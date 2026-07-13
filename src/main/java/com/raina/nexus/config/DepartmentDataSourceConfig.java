package com.raina.nexus.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.raina.nexus.department.repository",
        entityManagerFactoryRef = "departmentEntityManagerFactory",
        transactionManagerRef = "departmentTransactionManager"
)
public class DepartmentDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.department")
    public DataSourceProperties departmentDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource departmentDataSource() {
        return departmentDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean departmentEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("departmentDataSource") DataSource dataSource) {

        return builder
                .dataSource(dataSource)
                .packages("com.raina.nexus.entity.department")
                .persistenceUnit("department")
                .build();
    }

    @Bean
    public PlatformTransactionManager departmentTransactionManager(
            @Qualifier("departmentEntityManagerFactory")
            EntityManagerFactory entityManagerFactory) {

        return new JpaTransactionManager(entityManagerFactory);
    }
}