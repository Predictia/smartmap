/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package es.predictia.smartmap.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import es.predictia.smartmap.service.DataService;
import es.predictia.smartmap.service.DataWebSocketHandler;
import es.predictia.smartmap.service.DefaultDataService;

@Configuration
@EnableAutoConfiguration
@EnableWebSocket
public class SampleWebSocketsApplication extends SpringBootServletInitializer implements
		WebSocketConfigurer {

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(dataWebSocketHandler(), "/lastdata").withSockJS();
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(SampleWebSocketsApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(SampleWebSocketsApplication.class, args);
	}

	@Bean
	public DataService dataService() {
		return new DefaultDataService();
	}

	@Bean
	public WebSocketHandler dataWebSocketHandler() {
		return new DataWebSocketHandler(dataService());
	}


}
