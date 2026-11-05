package com.AcolheSP.demo.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

/** Executa a migracao real sobre as tabelas anteriores relevantes, em H2 modo PostgreSQL. */
class DatabaseMigrationTest {
    private void executar(Connection connection, String script) {
        ScriptUtils.executeSqlScript(connection,
                new EncodedResource(new ClassPathResource(script), StandardCharsets.UTF_8));
    }

    @Test
    void migracaoPreparaEsquemaOriginalEPreservaRegistrosAoReexecutar() throws Exception {
        try (var connection = DriverManager.getConnection(
                "jdbc:h2:mem:migracao_" + UUID.randomUUID() + ";MODE=PostgreSQL", "sa", "")) {
            // Esquema minimo anterior: candidaturas ainda nao possuem horario_id.
            try (var statement = connection.createStatement()) {
                statement.execute("create table instituicao (id integer primary key, nome varchar(150) not null)");
                statement.execute("""
                        create table voluntariado (id integer primary key,
                            instituicao_id integer not null references instituicao(id))
                        """);
                statement.execute("insert into instituicao values (1, 'Casa teste')");
                statement.execute("insert into voluntariado values (1, 1)");
            }
            executar(connection, "db/001_horarios_voluntariado.sql");
            try (var statement = connection.createStatement()) {
                assertEquals(1, statement.executeUpdate("""
                        insert into horario_voluntariado (instituicao_id, dia, funcao, inicio, fim)
                        select id, 'Sábado', 'Cozinha', time '08:00:00', time '12:00:00'
                        from instituicao order by id fetch first 1 row only
                        """));
            }
            executar(connection, "db/001_horarios_voluntariado.sql");
            try (var statement = connection.createStatement()) {
                try (var result = statement.executeQuery("select count(*) from horario_voluntariado")) {
                    result.next();
                    assertEquals(1, result.getInt(1));
                }
                try (var result = statement.executeQuery("select count(*) from instituicao")) {
                    result.next();
                    assertEquals(1, result.getInt(1));
                }
                try (var result = statement.executeQuery("select horario_id from voluntariado")) {
                    assertTrue(result.next());
                    assertEquals(null, result.getObject(1));
                }
            }
        }
    }
}
