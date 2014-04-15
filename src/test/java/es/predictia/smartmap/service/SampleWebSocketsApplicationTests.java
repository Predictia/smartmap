package es.predictia.smartmap.service;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import es.predictia.smartmap.config.SampleWebSocketsApplication;
import es.predictia.smartmap.service.DataService;
import es.predictia.smartmap.service.DataWebSocketHandler;
import es.predictia.smartmap.service.DefaultDataService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SampleWebSocketsApplication.class)
@WebAppConfiguration
@IntegrationTest
@DirtiesContext
public class SampleWebSocketsApplicationTests {

	private static Log logger = LogFactory.getLog(SampleWebSocketsApplicationTests.class);

	private static final String WS_URI = "ws://localhost:8080/echo/websocket";

	@Test
	public void runAndWait() throws Exception {
		ConfigurableApplicationContext context = SpringApplication.run(
				ClientConfiguration.class, "--spring.main.web_environment=false");
		long count = context.getBean(ClientConfiguration.class).latch.getCount();
		context.close();
		assertEquals(0, count);
	}

	@Configuration
	static class ClientConfiguration implements CommandLineRunner {

		private final CountDownLatch latch = new CountDownLatch(1);

		@Override
		public void run(String... args) throws Exception {
			logger.info("Waiting for response: latch=" + this.latch.getCount());
			this.latch.await(10, TimeUnit.SECONDS);
			logger.info("Got response: latch=" + this.latch.getCount());
		}

		@Bean
		public WebSocketConnectionManager wsConnectionManager() {

			WebSocketConnectionManager manager = new WebSocketConnectionManager(client(),
					handler(), WS_URI);
			manager.setAutoStartup(true);

			return manager;
		}

		@Bean
		public StandardWebSocketClient client() {
			return new StandardWebSocketClient();
		}

		@Bean
		public DataWebSocketHandler handler() {
			return new DataWebSocketHandler(dataService());
		}

		@Bean
		public DataService dataService() {
			return new DefaultDataService();
		}
	}

}
