package es.predictia.smartmap.model;


public class SimpleObservation {

	private Integer node;
	private String date;
	private Float longitude, latitude, value;
	private Boolean mobile;
	
	public SimpleObservation(Integer node,String date, Float longitude, Float latitude,
			Float value, Boolean mobile) {
		super();
		this.node = node;
		this.date = date;
		this.longitude = longitude;
		this.latitude = latitude;
		this.value = value;
		this.mobile = mobile;
	}

	public Integer getNode() {
		return node;
	}

	public String getDate() {
		return date;
	}
	
	public Float getLongitude() {
		return longitude;
	}
	
	public Float getLatitude() {
		return latitude;
	}
	
	public Float getValue() {
		return value;
	}

	public Boolean getMobile() {
		return mobile;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result
				+ ((latitude == null) ? 0 : latitude.hashCode());
		result = prime * result
				+ ((longitude == null) ? 0 : longitude.hashCode());
		result = prime * result + ((mobile == null) ? 0 : mobile.hashCode());
		result = prime * result + ((node == null) ? 0 : node.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleObservation other = (SimpleObservation) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (latitude == null) {
			if (other.latitude != null)
				return false;
		} else if (!latitude.equals(other.latitude))
			return false;
		if (longitude == null) {
			if (other.longitude != null)
				return false;
		} else if (!longitude.equals(other.longitude))
			return false;
		if (mobile == null) {
			if (other.mobile != null)
				return false;
		} else if (!mobile.equals(other.mobile))
			return false;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
		
}
