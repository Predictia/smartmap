package es.predictia.smartmap.data;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SantanderOpenDataClient {

	private final String envMonitoringUrl, mobileUrl;
	
	private final Integer ageLimitInSeconds;
	
	public Map<DataType, List<SimpleObservation>> getEnvMonitoring() throws IOException, InterruptedException {
		var observations = new HashMap<DataType, List<SimpleObservation>>();
		for(var type: DataType.values()) {
			observations.put(type, new ArrayList<>());
		}
		getEnvMonitoring(false).entrySet()
			.forEach(e -> observations.get(e.getKey()).addAll(e.getValue()));
		getEnvMonitoring(true).entrySet()
			.forEach(e -> observations.get(e.getKey()).addAll(e.getValue()));
		if(log.isDebugEnabled()){
			for(var type: DataType.values()) {
				log.debug("Found {} observations for type {}", observations.get(type).size(), type);
			}
		}
		return observations;
	}
	
	Map<DataType, List<SimpleObservation>> getEnvMonitoring(boolean mobile) throws IOException, InterruptedException {
		var observations = new HashMap<DataType, List<SimpleObservation>>();
		for(var type: DataType.values()) {
			observations.put(type, new ArrayList<>());
		}
		var request = HttpRequest.newBuilder()
			.uri(URI.create(mobile ? mobileUrl : envMonitoringUrl))
			.header("Content-type", "application/json")
            .build();
		log.debug("Performing request {}", request);
		var r = CLIENT.send(request, BodyHandlers.ofString());
		var ageLimit = ZonedDateTime.now(ZoneId.of("Europe/Madrid")).minusSeconds(ageLimitInSeconds);
		log.debug("Filtering data older than {}", ageLimit);
		getJsonMapper().readValue(r.body(), Response.class).getResources().stream()
			.filter(o -> o.getFixedModified() != null && ageLimit.isBefore(o.getFixedModified()))
			.flatMap(resource -> observations(resource, mobile).entrySet().stream())
			.filter(e -> e.getValue().getValue() != null)
			.forEach(e -> observations.get(e.getKey()).add(e.getValue()));
		return observations;
	}
	
	static Map<DataType, SimpleObservation> observations(Response.Resource resource, boolean mobile) {
		return EnumSet.allOf(DataType.class).stream()
			.collect(Collectors.toMap(t -> t, t -> observation(resource, t, mobile)));
	}
	
	static SimpleObservation observation(Response.Resource resource, DataType type, boolean mobile) {
		Function<Response.Resource, Float> fn;
		Float nan;
		switch (type) {
			case LIGHT: fn = Response.Resource::getLight; nan = 0.0f; break;
			case NOISE: fn = Response.Resource::getNoise; nan = 0.0f; break;
			case TEMPERATURE: fn = Response.Resource::getTemperature; nan = 0.0f; break;
			case CO: fn = Response.Resource::getCo; nan = 99.9f; break;
			case NO2: fn = Response.Resource::getNo2; nan = 999.0f; break;
			case OZONE: fn = Response.Resource::getOzone; nan = 999.0f; break;
			default: fn = eresource -> null; nan = Float.NaN;
		}
		return SimpleObservation.builder()
			.latitude(resource.getLatitude())
			.longitude(resource.getLongitude())
			.mobile(mobile)
			.value(Optional.ofNullable(fn.apply(resource)).filter(v -> !nan.equals(v)).orElse(null))
			.date(resource.getFixedModifiedString())
			.node(resource.getIdentifier())
			.build();
	}
	
	@Data 
	static class Response {
		
		Summary summary;
		
		@Data 
		static class Summary {
			Integer items, items_per_page, pages, current_page;
		}
		
		List<Resource> resources;
		
		@Data 
		static class Resource {
			
			@JsonProperty("uri") String uri;
			@JsonProperty("ayto:type") String type;
			@JsonProperty("ayto:noise") Float noise;
			@JsonProperty("ayto:battery") Float battery;
			@JsonProperty("ayto:latitude") Float latitude;
			@JsonProperty("ayto:altitude") Float altitude;
			@JsonProperty("ayto:speed") Float speed;
			@JsonProperty("ayto:odometer") Float odometer;
			@JsonProperty("ayto:course") Float course;
			@JsonProperty("ayto:temperature") Float temperature;
			@JsonProperty("dc:modified") LocalDateTime modified;
			@JsonProperty("dc:identifier") Integer identifier;
			@JsonProperty("ayto:longitude") Float longitude;
			@JsonProperty("ayto:light") Float light;
			@JsonProperty("ayto:CO") Float co;
			@JsonProperty("ayto:NO2") Float no2;
			@JsonProperty("ayto:ozone") Float ozone;
			@JsonProperty("ayto:particles") Float particles;
			
			ZonedDateTime getFixedModified() {
				if(getModified() == null) {
					return null;
				}
				var madrid = ZoneId.of("Europe/Madrid");
				var utc = ZoneId.of("UTC");
				var srcTime = LOCAL_FMT.format(getModified());
				var utcTime = LOCAL_FMT.format(ZonedDateTime.parse(srcTime, LOCAL_FMT.withZone(utc)).withZoneSameInstant(madrid));
				return ZonedDateTime.parse(utcTime, LOCAL_FMT.withZone(utc)).withZoneSameInstant(madrid);
			}
			
			public String getFixedModifiedString() {
				return LOCAL_FMT.format(getFixedModified());
			}

			static final DateTimeFormatter LOCAL_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

		}
	}
	
	@Getter(lazy = true, value = AccessLevel.PRIVATE)
	private final ObjectMapper jsonMapper = jsonMapper();

	private static ObjectMapper jsonMapper() {
		return new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	static final HttpClient CLIENT = HttpClient.newBuilder()
		.connectTimeout(Duration.ofSeconds(5))
		.build();
	
}