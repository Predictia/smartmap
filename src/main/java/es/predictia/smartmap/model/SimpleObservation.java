package es.predictia.smartmap.model;

import es.predictia.smartsantander.model.NodeLocationValue;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SimpleObservation {

	private final Integer node;
	private final String date;
	private final Float longitude, latitude, value;
	private final Boolean mobile;
	
	public static class SimpleObservationBuilder {
		
		public SimpleObservationBuilder nodeLocationValue(NodeLocationValue datum) {
			return node(datum.getNodeId())
				.date(datum.getDate() != null ? datum.getDate().toString() : null)
				.longitude(datum.getLongitude())
				.latitude(datum.getLatitude());
		}
		
	}
	
	public static SimpleObservation fixedNodeLocationValue(NodeLocationValue datum, Float value) {
		return SimpleObservation.builder()
			.nodeLocationValue(datum)
			.mobile(false)
			.value(value)
			.build();
	}
	
	public static SimpleObservation mobileNodeLocationValue(NodeLocationValue datum, Float value) {
		return SimpleObservation.builder()
			.nodeLocationValue(datum)
			.mobile(true)
			.value(value)
			.build();
	}
	
}
