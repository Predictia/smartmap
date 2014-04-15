package es.predictia.smartmap.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import es.predictia.smartmap.model.DataType;
import es.predictia.smartmap.model.SimpleObservation;
import es.predictia.smartsantander.model.EnviromentValue;
import es.predictia.smartsantander.model.MobileValue;
import es.predictia.smartsantander.service.SENS2SOCService;
import es.predictia.smartsantander.service.SENS2SOCServiceImpl;

public class DefaultDataService implements DataService {

	private SENS2SOCService service;
	private Multimap<DataType,SimpleObservation> lastData;
	private final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy/MM/dd hh:mm a");
	private final static Integer REFRESH_PERIOD = 10;
	private final static Integer DATA_AGE = 600;
	
	public DefaultDataService(){
		this.service = new SENS2SOCServiceImpl();
		this.lastData = ArrayListMultimap.create();
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(getDataTask(), 0, REFRESH_PERIOD, TimeUnit.SECONDS);
	}

	@Override
	public Collection<SimpleObservation> getLastData(DataType type) {
		return lastData.get(type);
	}
	
	private Runnable getDataTask(){
		return new Runnable() {
			@Override
			public void run() {
				List<EnviromentValue> dataFixed = new ArrayList<EnviromentValue>();
				List<MobileValue> dataMobile = new ArrayList<MobileValue>();
				try {
					dataFixed = Lists.newArrayList(Iterables.filter(service.getLastEnviromentValues(),new Predicate<EnviromentValue>(){
						@Override
						public boolean apply(EnviromentValue arg0) {
							return (new DateTime()).minusSeconds(DATA_AGE).isBefore(arg0.getDate());
						}
					}));
					dataMobile = Lists.newArrayList(Iterables.filter(service.getLastMobileValues(),new Predicate<MobileValue>(){
						@Override
						public boolean apply(MobileValue arg0) {
							return (new DateTime()).minusSeconds(DATA_AGE).isBefore(arg0.getDate());
						}
					}));
				} catch (IOException e) {
					logger.debug("error getting data",e);
				}
				lastData.clear();
				for(EnviromentValue datum : dataFixed){
					if(datum.getLight() != null){
						lastData.put(DataType.LIGHT,new SimpleObservation(datum.getNodeId(),formatter.print(datum.getDate()),datum.getLongitude(),datum.getLatitude(),datum.getLight(), false));
					}
					if(datum.getTemperature() != null){
						lastData.put(DataType.TEMPERATURE,new SimpleObservation(datum.getNodeId(),formatter.print(datum.getDate()),datum.getLongitude(),datum.getLatitude(),datum.getTemperature(), false));
					}
					if(datum.getNoise() != null){
						lastData.put(DataType.NOISE,new SimpleObservation(datum.getNodeId(),formatter.print(datum.getDate()),datum.getLongitude(),datum.getLatitude(),datum.getNoise(), false));
					}
				}
				for(MobileValue datum : dataMobile){
					if(datum.getTemperature() != null){
						lastData.put(DataType.TEMPERATURE,new SimpleObservation(datum.getNodeId(),formatter.print(datum.getDate()),datum.getLongitude(),datum.getLatitude(),datum.getTemperature(),true));
					}
				}
			}
		};
	}

	private static Logger logger = LoggerFactory.getLogger(DefaultDataService.class);

}
