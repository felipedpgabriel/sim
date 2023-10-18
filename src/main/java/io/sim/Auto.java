package io.sim;

import de.tudresden.sumo.cmd.Vehicle;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;
import de.tudresden.sumo.objects.SumoStringList;
import io.sim.created.MobilityCompany;
import io.sim.created.RouteN;

/**Define os atributos que coracterizam um Carro.
 * Por meio de metodos get da classe Vehicle, 
 */
public class Auto extends Thread // IMP# Car extends Vehicle implements Runnable
{
	// atributos de cliente
	private String carHost;
	private int servPort;
    private Socket socket;
	private DataInputStream entrada;
    private DataOutputStream saida;
    // atributos de sincronizacao
    // private boolean ocupado = false;
    // private Object monitor = new Object(); // sincronizacao
	// atributos da classe
	private boolean on_off;
	private String idAuto; // id do carro
	private SumoColor colorAuto;
	private String driverID; // id do motorista
	private SumoTraciConnection sumo;
	private long acquisitionRate; // taxa de aquisicao de dados dos sensores
	private int fuelType; 			// 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
	private int fuelPreferential; 	// 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
	private double fuelPrice; 		// price in liters
	private int personCapacity;		// the total number of persons that can ride in this vehicle
	private int personNumber;		// the total number of persons which are riding in this vehicle

	private DrivingData carRepport;
	private RouteN route;
	private TransportService ts;

	private double speed = 40;
	private boolean finished = false;

	public Auto(boolean _on_off, String _carHost,int _servPort, String _idAuto, SumoColor _colorAuto, String _driverID, 
	SumoTraciConnection _sumo, long _acquisitionRate, int _fuelType, int _fuelPreferential, double _fuelPrice, int _personCapacity,
	int _personNumber) throws Exception
	{
		this.carHost = _carHost;
		this.servPort = _servPort;
		this.on_off = _on_off;
		this.idAuto = _idAuto;
		this.colorAuto = _colorAuto;
		this.driverID = _driverID;
		this.sumo = _sumo;
		this.acquisitionRate = _acquisitionRate;
		this.setFuelType(_fuelType);
		this.setFuelPreferential(_fuelPreferential);
		this.fuelPrice = _fuelPrice;
		this.personCapacity = _personCapacity;
		this.personNumber = _personNumber;
		this.carRepport = this.updateDrivingData("aguardando", "");
	}

	@Override
	public void run()
	{
		System.out.println(this.idAuto + " iniciado.");
		try
		{
			// System.out.println(this.idAuto + " no try.");
            socket = new Socket(this.carHost, this.servPort);
			// System.out.println(this.idAuto + " passou do socket.");
            entrada = new DataInputStream(socket.getInputStream());
			// System.out.println(this.idAuto + " passou da entrada.");
            saida = new DataOutputStream(socket.getOutputStream());

			while(!finished)
			{
				saida.writeUTF(drivingDataToString(this.carRepport));
				System.out.println(this.idAuto + " aguardando rota.");
				route = (RouteN) stringToRouteN(entrada.readUTF());
				if(route.getRouteID().equals("-1"))
				{
					System.out.println(this.idAuto +" - Sem rotas a receber.");
					finished = true;
					break;
				}
				System.out.println(this.idAuto + " iniciando rota " + route.getRouteID());
				ts = new TransportService(this.idAuto, route,this, this.sumo);
				ts.start();
				String edgeFinal = this.getEdgeFinal(); 
				this.on_off = true;
				while(!MobilityCompany.estaNoSUMO(this.idAuto, this.sumo))
				{
					sleep(this.acquisitionRate);
				}
				String edgeAtual = (String) this.sumo.do_job_get(Vehicle.getRoadID(this.idAuto));

				while (this.on_off) // && MobilityCompany.estaNoSUMO(this.idAuto, sumo) mudar nome para on
				{
					if(isRouteFineshed(edgeAtual, edgeFinal))
					{
						System.out.println(this.idAuto + " acabou a rota " + this.route.getRouteID());
						this.carRepport = this.updateDrivingData("finalizado");
						saida.writeUTF(drivingDataToString(this.carRepport));
						this.on_off = false;
						break;
					}
					sleep(this.acquisitionRate);
					if(!isRouteFineshed(edgeAtual, edgeFinal))
					{
						// System.out.println(this.idAuto + " -> edge atual: " + edgeAtual);
						this.carRepport = this.atualizaSensores(); // IMP# tentar trocar para TransportService
						saida.writeUTF(drivingDataToString(this.carRepport));
						if(this.carRepport.getCarState().equals("finalizado"))
						{
							this.on_off = false;
							break;
						}
						else
						{
							edgeAtual = (String) this.sumo.do_job_get(Vehicle.getRoadID(this.idAuto));
						}
					}
				}
				System.out.println(this.idAuto + " off.");

				if(!finished)
				{
					this.carRepport = this.updateDrivingData("aguardando");
				}
				if(finished)
				{
					this.carRepport = this.updateDrivingData("encerrado");
				}
			}
			System.out.println("Encerrando: " + idAuto);
			entrada.close();
			saida.close();
			socket.close();
        }
		catch (Exception e)
		{
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

		System.out.println(this.idAuto + " encerrado.");
	}

	private DrivingData atualizaSensores() {
		DrivingData repport = updateDrivingData("aguardando", "");
		try {
			if (!this.getSumo().isClosed()) {
				SumoPosition2D sumoPosition2D;
				sumoPosition2D = (SumoPosition2D) sumo.do_job_get(Vehicle.getPosition(this.idAuto));
				
				// Criacao dos dados de conducao do veiculo
				repport = updateDrivingData(

						"rodando", this.idAuto, this.driverID, System.currentTimeMillis(), sumoPosition2D.x, sumoPosition2D.y,
						(String) this.sumo.do_job_get(Vehicle.getRoadID(this.idAuto)),
						(String) this.sumo.do_job_get(Vehicle.getRouteID(this.idAuto)),
						(double) this.sumo.do_job_get(Vehicle.getSpeed(this.idAuto)),
						(double) this.sumo.do_job_get(Vehicle.getDistance(this.idAuto)),

						(double) this.sumo.do_job_get(Vehicle.getFuelConsumption(this.idAuto)),
						// Vehicle's fuel consumption in ml/s during this time step,
						// to get the value for one step multiply with the step length; error value:
						// -2^30
						
						1/*averageFuelConsumption (calcular)*/,

						this.fuelType, this.fuelPrice,

						(double) this.sumo.do_job_get(Vehicle.getCO2Emission(this.idAuto)),
						// Vehicle's CO2 emissions in mg/s during this time step,
						// to get the value for one step multiply with the step length; error value:
						// -2^30

						(double) this.sumo.do_job_get(Vehicle.getHCEmission(this.idAuto)),
						// Vehicle's HC emissions in mg/s during this time step,
						// to get the value for one step multiply with the step length; error value:
						// -2^30
						
						this.personCapacity,
						// the total number of persons that can ride in this vehicle
						
						this.personNumber
						// the total number of persons which are riding in this vehicle

				);

				this.sumo.do_job_set(Vehicle.setSpeedMode(this.idAuto, 0));
				this.setSpeed(speed);

			} else {
				this.on_off = false;
				System.out.println("SUMO is closed...");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(this.idAuto + " erro no sumo.");
			return this.updateDrivingData("encerrado");			
		}

		return repport;
	}

	private DrivingData updateDrivingData(String _carState, String _autoID, String _driverID, long _timeStamp, double _x_Position,
	double _y_Position, String _roadIDSUMO, String _routeIDSUMO, double _speed, double _odometer, double _fuelConsumption,
	double _averageFuelConsumption, int _fuelType, double _fuelPrice, double _co2Emission, double _HCEmission, int _personCapacity,
	int _personNumber)
	{
		DrivingData _repport = new DrivingData(_carState, _autoID, _driverID, _timeStamp, _x_Position, _y_Position, _roadIDSUMO, 
		_routeIDSUMO, _speed, _odometer, _fuelConsumption, _averageFuelConsumption, _fuelType, _fuelPrice, _co2Emission, _HCEmission, 
		_personCapacity, _personNumber);
		return _repport;
	}

	private DrivingData updateDrivingData(String _carState, String _routeIDSUMO)
	{
		DrivingData _repport = updateDrivingData(_carState, idAuto, driverID, 0, 0, 0, 
		"", _routeIDSUMO, 0, 0, 0, 1, this.fuelType,
		this.fuelPrice,0, 0, this.personCapacity, this.personNumber);
		return _repport;	
	}

	private DrivingData updateDrivingData(String _carState)
	{
		DrivingData _repport = updateDrivingData(_carState, this.route.getRouteID());
		return _repport;	
	}

	public RouteN getRoute() {
		return route;
	}

	public boolean isOn_off() {
		return this.on_off;
	}

	public void setOn_off(boolean _on_off) {
		this.on_off = _on_off;
	}

	public void setfinished(boolean finished) {
		this.finished = finished;
	}

	public long getAcquisitionRate() {
		return this.acquisitionRate;
	}

	public void setAcquisitionRate(long _acquisitionRate) {
		this.acquisitionRate = _acquisitionRate;
	}

	public String getIdAuto() {
		return this.idAuto;
	}

	public SumoTraciConnection getSumo() {
		return this.sumo;
	}

	public int getFuelType() {
		return this.fuelType;
	}

	public void setFuelType(int _fuelType) {
		if((_fuelType < 0) || (_fuelType > 4)) {
			this.fuelType = 4;
		} else {
			this.fuelType = _fuelType;
		}
	}

	public double getFuelPrice() {
		return this.fuelPrice;
	}

	public void setFuelPrice(double _fuelPrice) {
		this.fuelPrice = _fuelPrice;
	}

	public SumoColor getColorAuto() {
		return this.colorAuto;
	}

	public int getFuelPreferential() {
		return this.fuelPreferential;
	}

	public void setFuelPreferential(int _fuelPreferential) {
		if((_fuelPreferential < 0) || (_fuelPreferential > 4)) {
			this.fuelPreferential = 4;
		} else {
			this.fuelPreferential = _fuelPreferential;
		}
	}

	public int getPersonCapacity() {
		return this.personCapacity;
	}

	public int getPersonNumber() {
		return this.personNumber;
	}

	public void setSpeed(double speed) throws Exception
	{
		this.sumo.do_job_set(Vehicle.setSpeed(this.idAuto, speed));
	}

	public DrivingData getCarRepport() {
		return carRepport;
	}

	private boolean isRouteFineshed(String _edgeAtual, String _edgeFinal)
	{
		boolean taNoSUMO = MobilityCompany.estaNoSUMO(this.idAuto, this.sumo);
		if(!taNoSUMO && (_edgeFinal.equals(_edgeAtual)))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private String getEdgeFinal()
	{
		SumoStringList edge = new SumoStringList();
		edge.clear();
		String aux = this.route.getEdges();
		for(String e : aux.split(" "))
		{
			edge.add(e);
		}
		return edge.get(edge.size()-1);
	}

	private RouteN stringToRouteN(String _string)
	{
		JSONObject jsonOut = new JSONObject(_string);
		String jsRouteID = jsonOut.getString("RouteID");
		String jsEdges = jsonOut.getString("Edges");
		RouteN route = new RouteN(jsRouteID, jsEdges);
		return route;
	}

	/**Converte um objeto do tipo DrivingData para uma string no formato JSON
	 * @param _carRepport DrivingData - 
	 * @return
	 */
	private String drivingDataToString(DrivingData _carRepport)
	{
        JSONObject jsonOut = new JSONObject();
        jsonOut.put("CarState",_carRepport.getCarState());
        jsonOut.put("AutoID",_carRepport.getAutoID());
		jsonOut.put("DriverID",_carRepport.getDriverID());
		jsonOut.put("TimeStamp",_carRepport.getTimeStamp());
		jsonOut.put("X_Position",_carRepport.getX_Position());
		jsonOut.put("Y_Position",_carRepport.getY_Position());
		jsonOut.put("RoadIDSUMO",_carRepport.getRoadIDSUMO());
		jsonOut.put("RouteIDSUMO",_carRepport.getRouteIDSUMO());
		jsonOut.put("Speed",_carRepport.getSpeed());
		jsonOut.put("Odometer",_carRepport.getOdometer());
		jsonOut.put("FuelConsumption",_carRepport.getFuelConsumption());
		jsonOut.put("AverageFuelConsumption",_carRepport.getAverageFuelConsumption());
		jsonOut.put("FuelType",_carRepport.getFuelType());
		jsonOut.put("FuelPrice",_carRepport.getFuelPrice());
		jsonOut.put("Co2Emission",_carRepport.getCo2Emission());
		jsonOut.put("HCEmission",_carRepport.getHCEmission());
		jsonOut.put("PersonCapacity",_carRepport.getPersonCapacity());
		jsonOut.put("PersonNumber",_carRepport.getPersonNumber());

        return jsonOut.toString();
	}
}