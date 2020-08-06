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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import es.predictia.smartmap.service.DataService;
import es.predictia.smartmap.service.DataWebSocketHandler;
import es.predictia.smartmap.service.DefaultDataService;

@SpringBootApplication
public class SmartMapApp {

	public static void main(String[] args) {
		SpringApplication.run(SmartMapApp.class, args);
	}

	@Bean
	static DataService dataService() {
		return new DefaultDataService();
	}
	
	@EnableWebSocket
	@Configuration	
	@Controller
	static class WebSocketConfig implements WebSocketConfigurer {

		private final DataWebSocketHandler wsHandler;
		
		public WebSocketConfig(@Autowired DataService dataService) {
			this.wsHandler = new DataWebSocketHandler(dataService);
		}
		
		@Override
		public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
			registry.addHandler(wsHandler, "/lastdata").withSockJS();
		}

	}	

}
