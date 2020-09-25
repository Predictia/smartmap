package es.predictia.smartmap.data;

import java.util.Collection;

public interface DataService {

	Collection<SimpleObservation> getLastData(DataType type);

}
