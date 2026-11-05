package com.AcolheSP.demo.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.core.env.StandardEnvironment;

/** Carrega as propriedades reais de teste e reproduz os overrides do Actions. */
class TestDatabaseConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withInitializer(context -> context.getEnvironment().getPropertySources()
                    .remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME))
            .withUserConfiguration(TestProperties.class);

    @TestConfiguration(proxyBeanMethods = false)
    @PropertySource("classpath:application.properties")
    @EnableConfigurationProperties(DataSourceProperties.class)
    static class TestProperties {
    }

    @Test
    void configuracaoLocalEscolheH2PelaUrl() {
        runner.run(context -> {
            var properties = context.getBean(DataSourceProperties.class);
            assertTrue(properties.getUrl().startsWith("jdbc:h2:"));
            assertEquals("org.h2.Driver", properties.determineDriverClassName());
        });
    }

    @Test
    void variaveisDoWorkflowEscolhemDriverPostgresqlSemH2Fixo() {
        runner.withInitializer(context -> context.getEnvironment().getPropertySources().addFirst(
                new SystemEnvironmentPropertySource("github-actions", Map.of(
                        "SPRING_DATASOURCE_URL", "jdbc:postgresql://localhost:5432/acolhesp_test",
                        "SPRING_DATASOURCE_USERNAME", "test",
                        "SPRING_DATASOURCE_PASSWORD", "test",
                        "SPRING_JPA_HIBERNATE_DDL_AUTO", "none"))))
                .run(context -> {
                    var properties = context.getBean(DataSourceProperties.class);
                    assertEquals("jdbc:postgresql://localhost:5432/acolhesp_test", properties.getUrl());
                    assertEquals("org.postgresql.Driver", properties.determineDriverClassName());
                    assertEquals("test", properties.getUsername());
                    assertEquals("none", context.getEnvironment().getProperty("spring.jpa.hibernate.ddl-auto"));
                });
    }
}
