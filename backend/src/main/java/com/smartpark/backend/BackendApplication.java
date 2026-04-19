package com.smartpark.backend;

import com.smartpark.backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableAsync
@EnableScheduling
public class BackendApplication implements CommandLineRunner {

    @Autowired
    private AuthService authService;

    @Value("${app.startup.ignore-db-init-errors:true}")
    private boolean ignoreDbInitErrors;

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Override
    public void run(String... args) {
        // Crée l'admin au démarrage, sans stopper toute l'app si Mongo n'est pas disponible en dev.
        try {
            authService.createDefaultAdmin();
        } catch (DataAccessException ex) {
            if (ignoreDbInitErrors) {
                System.err.println("⚠️ Mongo indisponible au démarrage: création admin ignorée. " + ex.getMessage());
            } else {
                throw ex;
            }
        }
    }
}