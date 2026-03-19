package com.finanalizador;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.h2.tools.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.sql.SQLException;

@SpringBootApplication
public class  AnalizadorFinancieroApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnalizadorFinancieroApplication.class, args);
	}

	@Bean(initMethod = "start", destroyMethod = "stop")
	public Server h2Server() throws SQLException {
		return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092");
	}

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI().info(new Info().title("Analizador Financiero API")
                .version("1.0.0")
                .description("""
                                Bienvenido a mi **Analizador financiero**!  
                                Esta API permite gestionar socios, contratos y transacciones. 
                                Las pequeñas y medianas empresas pueden usarla para visualizar 
                                sus datos en un panel de control, filtrarlos y descubrir socios 
                                más beneficiosos según diferentes fechas.
                                Todos los puntos de acceso son seguros y están documentados aquí para desarrolladores.
                                
                                Lilit Khachatryan""")
        );
	}
}
