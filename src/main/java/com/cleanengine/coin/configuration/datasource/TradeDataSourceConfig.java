package com.cleanengine.coin.configuration.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

//@Configuration
@EnableJpaRepositories(
        basePackages = "com.cleanengine.coin.trade.repository",
        entityManagerFactoryRef = "tradeEntityManagerFactory",
        transactionManagerRef = "tradeTransactionManager"
)
public class TradeDataSourceConfig {

    @Bean(name = "tradeDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.trade")
    public DataSource mainDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean(name = "tradeEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean tradeEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("tradeDataSource") DataSource dataSource
    ) {
        return builder
                .dataSource(dataSource)
                .packages("com.cleanengine.coin.trade")
                .persistenceUnit("trade")
                .build();
    }

    @Bean(name = "tradeTransactionManager")
    public PlatformTransactionManager tradeTransactionManager(
            @Qualifier("tradeEntityManagerFactory") LocalContainerEntityManagerFactoryBean tradeEntityManagerFactory
    ) {
        return new JpaTransactionManager(tradeEntityManagerFactory.getObject());
    }
}
