package io.sim;

import java.io.Serializable;

/**Armazena dados do veiculo IMP# arrumar os dados necessarios para o relatorio
 * Funcao organizacional, para ser usada no relatorio via Excel.
 */
public class DrivingData implements Serializable{

	/* SUMO's data */

	private String carState;
	private long timeStamp; 			// System.currentTimeMillis() IMP# precisa ser em nanosegundos
	private String autoID;
	private String routeIDSUMO; 		// this.sumo.do_job_get(Vehicle.getRouteID(this.idAuto))
	private double speed; 				// in m/s for the last time step
	private double distance;
	private double fuelConsumption; 	// in mg/s for the last time step
	private int fuelType; 				// 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
	private double co2Emission; 		// in mg/s for the last time step
	private long longitude;
	private long latitude;

	public DrivingData(String carState, long timeStamp, String autoID, String routeIDSUMO, double speed, double distance,
	double fuelConsumption, int fuelType, double co2Emission, long longitude, long latitude)
	{
		this.carState = carState;
		this.timeStamp = timeStamp;
		this.autoID = autoID;
		this.routeIDSUMO = routeIDSUMO;
		this.speed = speed;
		this.distance = distance;
		this.fuelConsumption = fuelConsumption;
		this.fuelType = fuelType;
		this.co2Emission = co2Emission;
		this.longitude = longitude;
		this.latitude = latitude;
	}

	// public void setAverageFuelConsumption(double _averageFuelConsumption) {
	// 	this.averageFuelConsumption = _averageFuelConsumption;
	// }

	public String getCarState() {
		return carState;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public String getAutoID() {
		return autoID;
	}

	public String getRouteIDSUMO() {
		return routeIDSUMO;
	}

	public double getSpeed() {
		return speed;
	}

	public double getDistance() {
		return distance;
	}

	public double getFuelConsumption() {
		return fuelConsumption;
	}

	public int getFuelType() {
		return fuelType;
	}

	public double getCo2Emission() {
		return co2Emission;
	}

	public long getLongitude() {
		return longitude;
	}

	public long getLatitude() {
		return latitude;
	}
}