package br.edu.utfpr.apicore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
	info = @Info(
		title = "API Core TAC",
		version = "1.0.1",
		contact = @Contact(name = "Juca Silvestre", email = "ss"),
		description = "API da disciplina TAC...",
		summary = "API bagual!",
		license = @License(name = "Qualuer", url = "http://....")
	),
	servers = {
		@Server(url = "http://localhost:8080", description = "Testes locais"),
		@Server(url = "http://minhaapp.com", description = "Produção"),
	}
)
@SpringBootApplication
public class ApiCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiCoreApplication.class, args);
	}

}
