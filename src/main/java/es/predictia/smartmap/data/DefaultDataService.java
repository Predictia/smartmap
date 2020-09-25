package es.predictia.smartmap.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultDataService implements DataService {

	private final SantanderOpenDataClient client;
	private final Map<DataType, List<SimpleObservation>> lastData;
	
	public DefaultDataService(SantanderOpenDataClient client, Integer refreshPeriodInSeconds){
		super();
		this.client = client;
		this.lastData = new ConcurrentHashMap<DataType, List<SimpleObservation>>();
		EnumSet.allOf(DataType.class)
			.forEach(t -> lastData.put(t, new ArrayList<SimpleObservation>()));
		Executors.newSingleThreadScheduledExecutor()
			.scheduleAtFixedRate(() -> update(), 0, refreshPeriodInSeconds, TimeUnit.SECONDS);		
	}

	@Override
	public Collection<SimpleObservation> getLastData(DataType type) {
		return lastData.get(type);
	}
	
	private synchronized void update(){
		try {
			EnumSet.allOf(DataType.class)
				.forEach(t -> lastData.get(t).clear());
			var data = client.getEnvMonitoring();
			EnumSet.allOf(DataType.class)
				.forEach(t -> lastData.get(t).addAll(data.get(t)));
		} catch (Throwable e) {
			log.warn("Problem updating data", e);
		}
	}

}
