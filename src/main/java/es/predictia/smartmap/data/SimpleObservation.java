package es.predictia.smartmap.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SimpleObservation {

	private final Integer node;
	private final String date;
	private final Float longitude, latitude, value;
	private final Boolean mobile;
	
}
