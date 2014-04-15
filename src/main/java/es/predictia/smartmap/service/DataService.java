package es.predictia.smartmap.service;

import java.util.Collection;

import es.predictia.smartmap.model.DataType;
import es.predictia.smartmap.model.SimpleObservation;

public interface DataService {

	Collection<SimpleObservation> getLastData(DataType type);

}
