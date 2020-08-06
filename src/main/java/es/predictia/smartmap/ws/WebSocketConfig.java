package es.predictia.smartmap.ws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import es.predictia.smartmap.service.DataService;

@EnableWebSocket
@Configuration	
class WebSocketConfig implements WebSocketConfigurer {

	private final DataWsHandler wsHandler;
	
	public WebSocketConfig(@Autowired DataService dataService) {
		this.wsHandler = new DataWsHandler(dataService);
	}
	
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(wsHandler, "/smartmap").setAllowedOrigins("*").withSockJS();
	}

}