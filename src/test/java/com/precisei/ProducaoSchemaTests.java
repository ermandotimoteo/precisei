package com.precisei;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.flyway.locations=classpath:db/production,classpath:db/common",
    "spring.datasource.url=${TEST_PROD_DB_URL:jdbc:tc:mysql:8.0.46:///precisei_production_test?TC_DAEMON=true}"
})
@ActiveProfiles("test")
class ProducaoSchemaTests {
    @Autowired JdbcTemplate jdbc;
    @Test void baseOperacionalNaoCriaContasOuPedidosDemonstrativos() {
        for (String tabela : new String[]{"clientes","profissionais","solicitacoes_servico","avaliacoes"}) {
            assertEquals(0, jdbc.queryForObject("select count(*) from " + tabela,Integer.class));
        }
        assertEquals(6,jdbc.queryForObject("select count(*) from categorias",Integer.class));
        assertEquals(3,jdbc.queryForObject("select count(*) from flyway_schema_history where success = 1",Integer.class));
    }
}
