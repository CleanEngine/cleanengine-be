package com.cleanengine.coin.tool.extension;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * mariadb testcontainer 관련 설정은 다 이 클래스에서 할 수 있도록 하기
 */
public class MariaDBTestContainerExtension implements Extension, BeforeAllCallback, BeforeEachCallback {
    public static JdbcDatabaseContainer<?> container = (JdbcDatabaseContainer<?>) new MariaDBContainer(DockerImageName.parse("mariadb:latest"))
            .withDatabaseName("if")
            .withUsername("user")
            .withPassword("pass")
            .withInitScripts("./db/mariadb/schema/init.sql")
            .withLogConsumer(new Slf4jLogConsumer(getLogger()))
            .withEnv("TZ", "Asia/Seoul");

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        ExtensionContext.Store store = context.getStore(ExtensionContext.Namespace.GLOBAL);

        if(store.get("mariaDBStarted", Boolean.class) == null) {
            container.start();
            store.put("mariaDBStarted", true);
            store.put("mariaDBInstance", container);
            Runtime.getRuntime().addShutdownHook(new Thread(()->{
                if(container!=null && container.isRunning()) {
                    container.stop();
                }
            }));
        }
        else{
            container = (JdbcDatabaseContainer<?>) store.get("mariaDBInstance", JdbcDatabaseContainer.class);
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        assertTrue(container.isRunning());
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger("MariaDBIntegrationTest");
    }
}
