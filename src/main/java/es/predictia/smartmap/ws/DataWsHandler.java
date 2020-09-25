package es.predictia.smartmap.ws;

import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.predictia.smartmap.data.DataService;
import es.predictia.smartmap.data.DataType;
import lombok.extern.slf4j.Slf4j;

/**
 * Echo messages by implementing a Spring {@link WebSocketHandler} abstraction.
 */
@Slf4j
public class DataWsHandler extends TextWebSocketHandler {

	private final DataService dataService;
	private final ObjectMapper converter = new ObjectMapper();
	private final Map<String, Timer> timers = new ConcurrentHashMap<>();

	@Autowired
	public DataWsHandler(DataService dataService) {
		this.dataService = dataService;
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		log.debug("Opened new session {}", session.getId());
	}

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		Optional.ofNullable(timers.get(session.getId()))
			.ifPresent(Timer::cancel);
		var type = DataType.valueOf(message.getPayload());
		timers.put(session.getId(), createSendMessageTimer(session, type));
	}

	private Timer createSendMessageTimer(WebSocketSession session, DataType type) {
		var timer = new Timer(); 
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override 
			public void run() {
				var data = dataService.getLastData(type);
				if(session.isOpen()){
					try {
						session.sendMessage(new TextMessage(converter.writeValueAsString(data)));
					} catch (Throwable e) {
						log.debug("problem sending data", e);
					}
				}else{
					log.debug("session {} is closed", session.getId());
					Optional.ofNullable(timers.remove(session.getId()))
						.ifPresent(Timer::cancel);
				}
			}
		}, 0, 1000);
		return timer;
	}
	
	@Override
	public void handleTransportError(WebSocketSession session, Throwable e) throws Exception {
		log.debug("session {} had transport error: {}", session.getId(), e.getMessage());
		Optional.ofNullable(timers.remove(session.getId()))
			.ifPresent(Timer::cancel);
		session.close(CloseStatus.SERVER_ERROR);
	}

}
