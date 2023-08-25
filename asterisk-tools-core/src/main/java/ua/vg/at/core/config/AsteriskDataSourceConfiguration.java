package ua.vg.at.core.config;

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

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "asteriskEntityManagerFactory",
        transactionManagerRef = "asteriskTransactionManager",
        basePackages = {"ua.vg.at.core.data.repository.ami"})
public class AsteriskDataSourceConfiguration {


    @Bean(name = "asteriskDataSourceProperties")
    @ConfigurationProperties("spring.datasource-asterisk")
    public DataSourceProperties asteriskDataSourceProperties() {
        return new DataSourceProperties();
    }


    @Bean(name = "asteriskDataSource")
    @ConfigurationProperties("spring.datasource-asterisk.configuration")
    public DataSource asteriskDataSource(@Qualifier("asteriskDataSourceProperties") DataSourceProperties asteriskDataSourceProperties) {
        return asteriskDataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }


    @Bean(name = "asteriskEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean asteriskEntityManagerFactory(
            EntityManagerFactoryBuilder asteriskEntityManagerFactoryBuilder, @Qualifier("asteriskDataSource") DataSource asteriskDataSource) {

        Map<String, String> asteriskJpaProperties = new HashMap<>();
        asteriskJpaProperties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        asteriskJpaProperties.put("hibernate.hbm2ddl.auto", "create-drop");

        return asteriskEntityManagerFactoryBuilder
                .dataSource(asteriskDataSource)
                .packages("ua.vg.at.core.data.entity.ami")
                .persistenceUnit("asteriskDataSource")
                .properties(asteriskJpaProperties)
                .build();
    }


    @Bean(name = "asteriskTransactionManager")
    public PlatformTransactionManager primaryTransactionManager(
            @Qualifier("asteriskEntityManagerFactory") EntityManagerFactory asteriskEntityManagerFactory) {

        return new JpaTransactionManager(asteriskEntityManagerFactory);
    }
}