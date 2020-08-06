package es.predictia.smartmap.ws;

import java.io.IOException;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

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
import es.predictia.smartmap.service.DataService;
import lombok.extern.slf4j.Slf4j;

/**
 * Echo messages by implementing a Spring {@link WebSocketHandler} abstraction.
 */
@Slf4j
public class DataWsHandler extends TextWebSocketHandler {

	private final DataService dataService;
	private final ObjectMapper converter = new ObjectMapper();
	private Timer timer;

	@Autowired
	public DataWsHandler(DataService dataService) {
		this.dataService = dataService;
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		log.debug("Opened new session in instance " + this);
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
						log.debug("problem generating JSON");
					} catch (IOException e) {
						log.debug("problem sending data");
					}
				}else{
					log.debug("session is closed");
					if(timer != null){
						timer.cancel();
					}
				}
			}
		};
	}
}
