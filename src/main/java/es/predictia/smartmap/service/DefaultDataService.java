package es.predictia.smartmap.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import es.predictia.smartmap.model.DataType;
import es.predictia.smartmap.model.SimpleObservation;
import es.predictia.smartsantander.model.EnviromentValue;
import es.predictia.smartsantander.model.MobileValue;
import es.predictia.smartsantander.service.SENS2SOCService;
import es.predictia.smartsantander.service.SENS2SOCServiceImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultDataService implements DataService, Runnable {

	private final SENS2SOCService service = new SENS2SOCServiceImpl();
	private final Map<DataType,List<SimpleObservation>> lastData = new HashMap<>();
	private final static Integer REFRESH_PERIOD = 10;
	private final static Integer DATA_AGE = 600;
	
	public DefaultDataService(){
		Executors.newSingleThreadScheduledExecutor()
			.scheduleAtFixedRate(this, 0, REFRESH_PERIOD, TimeUnit.SECONDS);
		EnumSet.allOf(DataType.class)
			.forEach(t -> lastData.put(t, new ArrayList<SimpleObservation>()));
	}

	@Override
	public Collection<SimpleObservation> getLastData(DataType type) {
		return lastData.get(type);
	}
	
	@Override
	public void run() {
		List<EnviromentValue> dataFixed = Collections.emptyList();
		List<MobileValue> dataMobile = Collections.emptyList();
		try {
			dataFixed = service.getLastEnviromentValues();
			dataMobile = service.getLastMobileValues();
			log.debug("Found {} enviroment values and {} mobile values", dataFixed.size(), dataMobile.size());
		} catch (IOException e) {
			log.warn("error getting data", e);
		}
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
