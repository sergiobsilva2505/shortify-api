package br.com.forjacode.shortifyapi.support;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.mysql.MySQLContainer;

/**
 * Base das classes de teste que precisam de MySQL: um único container é compartilhado por toda a JVM de testes
 * (e reaproveitado entre execuções com testcontainers.reuse.enable=true em ~/.testcontainers.properties).
 */
public abstract class IntegrationTestSupport {

    @ServiceConnection
    protected static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4")
            .withReuse(true);

    static {
        MYSQL.start();
    }
}
