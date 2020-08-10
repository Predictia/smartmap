package es.predictia.smartmap.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import es.predictia.smartmap.model.DataType;
import es.predictia.smartmap.model.SimpleObservation;
import es.predictia.smartsantander.service.SENS2SOCService;
import es.predictia.smartsantander.service.SENS2SOCServiceImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultDataService implements DataService {

	private final SENS2SOCService service = new SENS2SOCServiceImpl();
	private final Map<DataType,List<SimpleObservation>> lastData = new HashMap<>();
	private final static Integer REFRESH_PERIOD = 10;
	private final static Integer DATA_AGE = 600;
	
	public DefaultDataService(){
		EnumSet.allOf(DataType.class)
			.forEach(t -> lastData.put(t, new ArrayList<SimpleObservation>()));
		Executors.newSingleThreadScheduledExecutor()
			.scheduleAtFixedRate(() -> {
				try {
					update();
				} catch (Throwable e) {
					log.warn("Problem updating data", e);
				}
			}, 0, REFRESH_PERIOD, TimeUnit.SECONDS);		
	}

	@Override
	public Collection<SimpleObservation> getLastData(DataType type) {
		return lastData.get(type);
	}
	
	private synchronized void update() throws Throwable {
		var dataFixed = service.getLastEnviromentValues();
		var dataMobile = service.getLastMobileValues();
		log.debug("Found {} enviroment values and {} mobile values", dataFixed.size(), dataMobile.size());
		EnumSet.allOf(DataType.class)
			.forEach(t -> lastData.get(t).clear());
		for(var datum : dataFixed) {
			if(LocalDateTime.now().minusSeconds(DATA_AGE).isBefore(datum.getDate())) {
				if(datum.getLight() != null){
					lastData.get(DataType.LIGHT).add(SimpleObservation.fixedNodeLocationValue(datum, datum.getLight()));
				}
				if(datum.getTemperature() != null){
					lastData.get(DataType.TEMPERATURE)
						.add(SimpleObservation.fixedNodeLocationValue(datum, datum.getTemperature()));
				}
				if(datum.getNoise() != null){
					lastData.get(DataType.NOISE)
						.add(SimpleObservation.fixedNodeLocationValue(datum, datum.getNoise()));
				}
			}
		}
		for(var datum : dataMobile) {
			if(LocalDateTime.now().minusSeconds(DATA_AGE).isBefore(datum.getDate())) {
				if(datum.getTemperature() != null) {
					lastData.get(DataType.TEMPERATURE)
						.add(SimpleObservation.mobileNodeLocationValue(datum, datum.getTemperature()));
				}
			}
		}
		EnumSet.allOf(DataType.class)
			.forEach(t -> log.debug("Got {} elements of type {}", lastData.get(t).size(), t));
	}

}
