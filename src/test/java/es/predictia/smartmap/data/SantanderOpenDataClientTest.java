package es.predictia.smartmap.data;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class SantanderOpenDataClientTest {

	@Disabled("external service")
	@Test
	public void testEnvMonitoringService() throws Exception {
		var client = new SantanderOpenDataClient(ENV_MONITORING_URL, MOBILE_URL, 600);
		var obs = client.getEnvMonitoring();
		Assertions.assertFalse(obs.isEmpty());
	}
	
	static final String ENV_MONITORING_URL = "http://datos.santander.es/api/rest/datasets/sensores_smart_env_monitoring.json";
	static final String MOBILE_URL = "http://datos.santander.es/api/rest/datasets/sensores_smart_mobile.json";
	
	@Test
	public void testFixedDate() throws Exception {
		var r = new SantanderOpenDataClient.Response.Resource();
		r.setModified(LocalDateTime.parse("2020-09-24T13:00:00"));
		var realDate = LocalDateTime.parse("2020-09-24T17:00:00").atZone(ZoneId.of("Europe/Madrid"));
		Assertions.assertEquals(realDate, r.getFixedModified());
	}
	
}
