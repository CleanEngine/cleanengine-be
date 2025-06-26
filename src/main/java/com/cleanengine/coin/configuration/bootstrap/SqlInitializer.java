package com.cleanengine.coin.configuration.bootstrap;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.sql.init.SqlDataSourceScriptDatabaseInitializer;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Profile("loadtest")
@Configuration
public class SqlInitializer {
    @Bean
    @ConfigurationProperties(prefix = "custom.datasource.main.init")
    public SqlInitializationProperties mainSqlInitializationProperties() {
        return new SqlInitializationProperties();
    }

    @Bean
    public SqlDataSourceScriptDatabaseInitializer mainDataSourceInitializer(
            @Qualifier("mainDataSource") DataSource mainDataSource,
            @Qualifier("mainSqlInitializationProperties") SqlInitializationProperties properties) {
        return new SqlDataSourceScriptDatabaseInitializer(mainDataSource, properties);
    }

}
