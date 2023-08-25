package ua.vg.at.core.config;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


/**
 *
 * @author vgvadmin
 */
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;


import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "secureEntityManagerFactory",
        transactionManagerRef = "secureTransactionManager",
        basePackages = {"ua.vg.at.data.repository.secure"})
public class SecureDataSourceConfiguration {

    @Primary
    @Bean(name = "secureDataSourceProperties")
    @ConfigurationProperties("spring.datasource-secure")
    public DataSourceProperties secureDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean(name = "secureDataSource")
    @ConfigurationProperties("spring.datasource-secure.configuration")
    public DataSource asteriskDataSource(@Qualifier("secureDataSourceProperties") DataSourceProperties secureDataSourceProperties) {
        return secureDataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Primary
    @Bean(name = "secureEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean secureEntityManagerFactory(
            EntityManagerFactoryBuilder secureEntityManagerFactoryBuilder, @Qualifier("secureDataSource") DataSource secureDataSource) {

        Map<String, String> secureJpaProperties = new HashMap<>();
        secureJpaProperties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        secureJpaProperties.put("hibernate.hbm2ddl.auto", "update");

        return secureEntityManagerFactoryBuilder
                .dataSource(secureDataSource)
                .packages("ua.vg.at.data.entity.secure")
                .persistenceUnit("secureDataSource")
                .properties(secureJpaProperties)
                .build();
    }

    @Primary
    @Bean(name = "secureTransactionManager")
    public PlatformTransactionManager secureTransactionManager(
            @Qualifier("secureEntityManagerFactory") EntityManagerFactory secureEntityManagerFactory) {

        return new JpaTransactionManager(secureEntityManagerFactory);
    }
}