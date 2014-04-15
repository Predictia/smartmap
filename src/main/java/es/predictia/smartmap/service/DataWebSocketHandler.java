package es.predictia.smartmap.service;

import java.io.IOException;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.predictia.smartmap.model.DataType;
import es.predictia.smartmap.model.SimpleObservation;

/**
 * Echo messages by implementing a Spring {@link WebSocketHandler} abstraction.
 */
public class DataWebSocketHandler extends TextWebSocketHandler {

	private static Logger logger = LoggerFactory.getLogger(DataWebSocketHandler.class);

	private final DataService dataService;
	private final ObjectMapper converter = new ObjectMapper();
	private Timer timer;

	@Autowired
	public DataWebSocketHandler(DataService dataService) {
		this.dataService = dataService;
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		logger.debug("Opened new session in instance " + this);
	}

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {		
		
		DataType type = DataType.valueOf(message.getPayload());
		
		if(timer != null){
			timer.cancel();
			timer = new Timer();
		}else{
			timer = new Timer(); 
		}
		
		TimerTask future = getDataTask(session,type);
		timer.scheduleAtFixedRate(future, 0, 1000);
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		if(timer != null){
			timer.cancel();
		}
		session.close(CloseStatus.SERVER_ERROR);
	}

	private TimerTask getDataTask(final WebSocketSession session,final DataType type){
		return new TimerTask() {
			@Override
			public void run() {
				Collection<SimpleObservation> data = dataService.getLastData(type);
				if(session.isOpen()){
					try {
						session.sendMessage(new TextMessage(converter.writeValueAsString(data)));
					} catch (JsonProcessingException e) {
						logger.debug("problem generating JSON");
					} catch (IOException e) {
						logger.debug("problem sending data");
					}
				}else{
					logger.debug("session is closed");
					if(timer != null){
						timer.cancel();
					}
				}
			}
		};
	}
}
