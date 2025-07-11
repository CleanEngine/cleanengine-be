package com.cleanengine.coin.configuration;

import com.querydsl.sql.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import javax.sql.DataSource;

@Configuration
public class QueryDslConfig {

    @Bean
    @Profile("(!mariadb-local & !prod) || (h2-mem)")
    public SQLQueryFactory h2QueryFactory(DataSource dataSource) {
        com.querydsl.sql.Configuration configuration = new com.querydsl.sql.Configuration(
                new H2Templates());
//         SQLListener listener = new SQLBaseListener() {
//             @Override
//             public void preExecute(SQLListenerContext context) {
//                 System.out.println("### QueryDSL-SQL Connection: " + context.getConnection());
//                 super.preExecute(context);
//             }
//         };
//         configuration.addListener(listener);

        return new SQLQueryFactory(configuration, new TransactionAwareDataSourceProxy(dataSource));
    }

    @Bean
    @Profile("!h2-mem & (mariadb-local || prod)")
    public SQLQueryFactory mariadbQueryFactory(DataSource dataSource) {
        com.querydsl.sql.Configuration configuration = new com.querydsl.sql.Configuration(
                new MySQLTemplates());

        return new SQLQueryFactory(configuration, new TransactionAwareDataSourceProxy(dataSource));
    }
}
