package io.sim;

import java.io.Serializable;

/**Armazena dados do veiculo IMP# arrumar os dados necessarios para o relatorio
 * Funcao organizacional, para ser usada no relatorio via Excel.
 */
public class DrivingData implements Serializable{

	/* SUMO's data */

	private String carState;
	private String autoID;
	private String driverID;
	private long timeStamp; 			// System.currentTimeMillis()
	private double x_Position; 			// sumoPosition2D (x)
	private double y_Position; 			// sumoPosition2D (y)
	private String roadIDSUMO; 			// this.sumo.do_job_get(Vehicle.getRoadID(this.idAuto))
	private String routeIDSUMO; 		// this.sumo.do_job_get(Vehicle.getRouteID(this.idAuto))
	private double speed; 				// in m/s for the last time step
	private double odometer; 			// the distance in (m) that was already driven by this vehicle.
	private double fuelConsumption; 	// in mg/s for the last time step
	private double fuelPrice; 			// price in liters
	private int fuelType; 				// 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
	private double averageFuelConsumption;
	private int personCapacity;			// the total number of persons that can ride in this vehicle
	private int personNumber;			// the total number of persons which are riding in this vehicle
	private double co2Emission; 		// in mg/s for the last time step
	private double HCEmission; 			// in mg/s for the last time step

	public DrivingData(

			String _carState, String _autoID, String _driverID, long _timeStamp, double _x_Position, double _y_Position,
			String _roadIDSUMO, String _routeIDSUMO, double _speed, double _odometer, double _fuelConsumption,
			double _averageFuelConsumption, int _fuelType, double _fuelPrice, double _co2Emission, double _HCEmission, int _personCapacity, int _personNumber) {

		this.carState = _carState;
		this.timeStamp = _timeStamp; // #1 nanosegundo
		this.autoID = _autoID; // #2
		this.routeIDSUMO = _routeIDSUMO; // #3
		this.speed = _speed; // #4
		// distance #5
		this.fuelConsumption = _fuelConsumption; // #6
		this.fuelType = _fuelType; // #7
		this.co2Emission = _co2Emission; // #8
		// longitude #9
		// latitude #10
		this.driverID = _driverID;
		this.x_Position = _x_Position;
		this.y_Position = _y_Position;
		this.roadIDSUMO = _roadIDSUMO;
		this.odometer = _odometer;
		this.averageFuelConsumption = _averageFuelConsumption;
		this.fuelPrice = _fuelPrice;
		this.HCEmission = _HCEmission;
		this.personCapacity = _personCapacity;
		this.personNumber = _personNumber;
	}

	public String getCarState() {
		return carState;
	}

	public String getAutoID() {
		return this.autoID;
	}

	public String getDriverID() {
		return this.driverID;
	}

	public long getTimeStamp() {
		return this.timeStamp;
	}

	public double getX_Position() {
		return this.x_Position;
	}

	public double getY_Position() {
		return this.y_Position;
	}

	public String getRoadIDSUMO() {
		return this.roadIDSUMO;
	}

	public String getRouteIDSUMO() {
		return this.routeIDSUMO;
	}

	public double getSpeed() {
		return this.speed;
	}

	public double getOdometer() {
		return this.odometer;
	}

	public double getFuelConsumption() {
		return this.fuelConsumption;
	}

	public double getAverageFuelConsumption() {
		return this.averageFuelConsumption;
	}

	public int getFuelType() {
		return this.fuelType;
	}

	public double getFuelPrice() {
		return this.fuelPrice;
	}

	public double getCo2Emission() {
		return this.co2Emission;
	}

	public double getHCEmission() {
		return this.HCEmission;
	}

	public int getPersonCapacity() {
		return this.personCapacity;
	}

	public int getPersonNumber() {
		return this.personNumber;
	}

	public void setAverageFuelConsumption(double _averageFuelConsumption) {
		this.averageFuelConsumption = _averageFuelConsumption;
	}
}