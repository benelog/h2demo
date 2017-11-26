package net.h2demo;

import org.apache.coyote.http2.Http2Protocol;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;

import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Http2DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(Http2DemoApplication.class, args);
	}

	@Bean
	public ConfigurableServletWebServerFactory webServerFactory() {
		TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
		factory.addConnectorCustomizers((connector) -> {
			connector.addUpgradeProtocol(new Http2Protocol());
		});
		return factory;
	}
}
