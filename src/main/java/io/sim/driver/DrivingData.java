package io.sim.driver;

import java.io.Serializable;

/**Armazena dados do veiculo
 * Funcao organizacional, para ser usada no relatorio via Excel.
 */
public class DrivingData implements Serializable
{

	/* SUMO's data */

	private String carState;
	private String driverLogin;
	private long timestamp; 			// System.currentTimeMillis() // TODO mudar para Timestamp
	private String carID;
	private String routeIDSUMO; 		// this.sumo.do_job_get(Vehicle.getRouteID(this.idAuto))
	private double speed; 				// in m/s for the last time step
	private double distance;
	private double fuelConsumption; 	// in mg/s for the last time step
	private int fuelType; 				// 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
	private double co2Emission; 		// in mg/s for the last time step
	private double longitude;
	private double latitude;

	public DrivingData(String _carState, String _driverLogin, long _timestamp, String _carID, String _routeIDSUMO, double _speed,
	double _distance, double _fuelConsumption, int _fuelType, double _co2Emission, double _longitude, double _latitude)
	{
		this.carState = _carState;
		this.driverLogin = _driverLogin;
		this.timestamp = _timestamp;
		this.carID = _carID;
		this.routeIDSUMO = _routeIDSUMO;
		this.speed = _speed;
		this.distance = _distance;
		this.fuelConsumption = _fuelConsumption;
		this.fuelType = _fuelType;
		this.co2Emission = _co2Emission;
		this.longitude = _longitude;
		this.latitude = _latitude;
	}

	public String getCarState() {
		return carState;
	}

	public void setCarState(String _carState) {
		this.carState = _carState;
	}

	public String getDriverLogin() {
		return driverLogin;
	}

	public long getTimeStamp() {
		return timestamp;
	}

	public void setTimestamp(long _timestamp) {
		this.timestamp = _timestamp;
	}

	public String getCarID() {
		return carID;
	}

	public String getRouteIDSUMO() {
		return routeIDSUMO;
	}

	public void setRouteIDSUMO(String routeIDSUMO) {
		this.routeIDSUMO = routeIDSUMO;
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

	public double getLongitude() {
		return longitude;
	}

	public double getLatitude() {
		return latitude;
	}
}