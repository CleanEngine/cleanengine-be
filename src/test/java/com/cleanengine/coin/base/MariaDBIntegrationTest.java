package com.cleanengine.coin.base;

import com.cleanengine.coin.configuration.TimeConfig;
import com.cleanengine.coin.tool.extension.MariaDBTestContainerExtension;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

/**
 * Mariadb가 적용된 영속성 Adapter(Repository)를 통합 테스트하기 위한 Base 테스트 클래스입니다.
 * JPA Entity가 MariaDB에서 제대로 매핑되는지 확인을 위한 기본적인 insert/select와 직접 작성한 쿼리를 테스트바랍니다.
 * API단으로 수행하는 통합 테스트는 AcceptanceTest를 사용바랍니다.
 */
@SpringBootTest
@Tag("testcontainers")
@ActiveProfiles({"dev", "it"})
@ExtendWith(MariaDBTestContainerExtension.class)
@Import(TimeConfig.class)
@Sql(
        scripts = "classpath:db/mariadb/data/delete.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        config=@SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
)
public abstract class MariaDBIntegrationTest {
    @PersistenceContext
    protected EntityManager em;

    @DynamicPropertySource
    static void mariadbProperties(DynamicPropertyRegistry registry) {
        if(MariaDBTestContainerExtension.container != null && MariaDBTestContainerExtension.container.isRunning()) {
            registry.add("spring.datasource.url", MariaDBTestContainerExtension.container::getJdbcUrl);
            registry.add("spring.datasource.driver-class-name", MariaDBTestContainerExtension.container::getDriverClassName);
            registry.add("spring.datasource.database", MariaDBTestContainerExtension.container::getDatabaseName);
            registry.add("spring.datasource.username", MariaDBTestContainerExtension.container::getUsername);
            registry.add("spring.datasource.password", MariaDBTestContainerExtension.container::getPassword);
            registry.add("logging.level.org.hibernate.SQL", () -> "debug");
            registry.add("logging.level.org.springframework.data", () -> "debug");
            registry.add("spring.jpa.properties.hibernate.format-sql", () -> "false");
            registry.add("logging.level.org.hibernate.orm.jdbc.bind", () -> "trace");
            registry.add("logging.level.org.hibernate.orm.jdbc.extract", () -> "trace");
        }
    }
}
