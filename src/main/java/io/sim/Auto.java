package io.sim;

import de.tudresden.sumo.cmd.Vehicle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;
import de.tudresden.sumo.objects.SumoStringList;
import io.sim.created.JSONConverter;
import io.sim.created.RouteN;
import io.sim.created.company.MobilityCompany;

/**Define os atributos que coracterizam um Carro.
 * Por meio de metodos get da classe Vehicle, 
 */
public class Auto extends Thread // TODO Car extends Vehicle implements Runnable
{
	// atributos de cliente
	private String carHost;
	private int servPort;
	// atributos da classe
	private boolean carOn;
	private String idAuto; // id do carro
	private String driverLogin;
	private SumoColor colorAuto;
	private SumoTraciConnection sumo;
	private long acquisitionRate; // taxa de aquisicao de dados dos sensores
	private int fuelType; 			// 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
	private int fuelPreferential; 	// 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
	private double fuelPrice; 		// price in liters
	private int personCapacity;		// the total number of persons that can ride in this vehicle
	private int personNumber;		// the total number of persons which are riding in this vehicle

	private DrivingData carRepport;
	private TransportService ts;
	private RouteN route;
	private double distanceCovered;

	private double speed;
	private boolean finished;

	public Auto(boolean _carOn, String _carHost,int _servPort, String _idAuto, String _driverLogin,SumoColor _colorAuto, String _driverID, 
	SumoTraciConnection _sumo, long _acquisitionRate, int _fuelType, int _fuelPreferential, double _fuelPrice, int _personCapacity,
	int _personNumber) throws Exception
	{
		this.carHost = _carHost;
		this.servPort = _servPort;
		this.carOn = _carOn;
		this.idAuto = _idAuto;
		this.driverLogin = _driverLogin;
		this.colorAuto = _colorAuto;
		this.sumo = _sumo;
		this.acquisitionRate = _acquisitionRate;
		this.setFuelType(_fuelType);
		this.setFuelPreferential(_fuelPreferential);
		this.fuelPrice = _fuelPrice;
		this.personCapacity = _personCapacity;
		this.personNumber = _personNumber;
		this.speed = 40;
		this.finished = false;
		this.carRepport = this.updateDrivingData("aguardando", "");
	}

	@Override
	public void run()
	{
		System.out.println(this.idAuto + " iniciado.");
		try
		{
			// Conexoes cliente
			// System.out.println(this.idAuto + " no try.");
            Socket socket = new Socket(this.carHost, this.servPort);
			// System.out.println(this.idAuto + " passou do socket.");
            DataInputStream entrada = new DataInputStream(socket.getInputStream());
			// System.out.println(this.idAuto + " passou da entrada.");
            DataOutputStream saida = new DataOutputStream(socket.getOutputStream());

			while(!finished)
			{
				saida.writeUTF(JSONConverter.drivingDataToString(this.carRepport));
				System.out.println(this.idAuto + " aguardando rota.");
				route = (RouteN) JSONConverter.stringToRouteN(entrada.readUTF());
				if(route.getRouteID().equals("-1"))
				{
					System.out.println(this.idAuto +" - Sem rotas a receber.");
					finished = true;
					break;
				} // TODO avaliar necessidade de um else
				System.out.println(this.idAuto + " iniciando rota " + route.getRouteID());
				ts = new TransportService(this.idAuto, route,this, this.sumo);
				ts.start();
				String edgeFinal = this.getEdgeFinal(); 
				this.carOn = true;
				while(!MobilityCompany.estaNoSUMO(this.idAuto, this.sumo)) //esperar estar no SUMO
				{
					sleep(this.acquisitionRate);
				}
				// atualiza informacoes iniciais
				String edgeAtual = (String) this.sumo.do_job_get(Vehicle.getRoadID(this.idAuto));
				double[] coordGeo = this.convertGeo();
				double previousLat = coordGeo[0];
				double previousLon = coordGeo[1];
				this.distanceCovered = 0;

				while (this.carOn) // && MobilityCompany.estaNoSUMO(this.idAuto, sumo)
				{
					if(isRouteFineshed(edgeAtual, edgeFinal)) // TODO IllegalStateException
					{
						System.out.println(this.idAuto + " acabou a rota " + this.route.getRouteID());
						this.carRepport = this.updateDrivingData("finalizado");
						saida.writeUTF(JSONConverter.drivingDataToString(this.carRepport));
						this.carOn = false;
						break;
					}
					sleep(this.acquisitionRate);
					if(!isRouteFineshed(edgeAtual, edgeFinal))
					{
						// System.out.println(this.idAuto + " -> edge atual: " + edgeAtual);
						this.carRepport = this.atualizaSensores(previousLat, previousLon); // TODO tentar trocar para TransportService
						previousLat = carRepport.getLatitude();
						previousLon = carRepport.getLongitude();
						// System.out.println("Distancia percorrida: " + this.distanceCovered + "\n Distancia repport: "
						// + this.carRepport.getDistance());
						saida.writeUTF(JSONConverter.drivingDataToString(this.carRepport));
						if(this.carRepport.getCarState().equals("finalizado"))
						{
							this.carOn = false;
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

	private DrivingData atualizaSensores(double _initiLat, double _initLon) {
		DrivingData repport = updateDrivingData("aguardando", "");
		try {
			if (!this.getSumo().isClosed()) {
				double[] coordGeo = this.convertGeo();
				double currentLat = coordGeo[0];
				double currentLon = coordGeo[1];
				
				// Criacao dos dados de conducao do veiculo
				repport = updateDrivingData("rodando", this.driverLogin, System.nanoTime(), this.idAuto,
				(String) this.sumo.do_job_get(Vehicle.getRouteID(this.idAuto)), (double) this.sumo.do_job_get(Vehicle.getSpeed(this.idAuto)),
				this.updateDistance(currentLat,currentLon,_initiLat, _initLon),
				(double) this.sumo.do_job_get(Vehicle.getFuelConsumption(this.idAuto)), this.fuelType,
				(double) this.sumo.do_job_get(Vehicle.getCO2Emission(this.idAuto)), currentLon, currentLat);
						
				// Vehicle's fuel consumption in ml/s during this time step,
				// to get the value for one step multiply with the step length; error value:
				// -2^30

				// Vehicle's CO2 emissions in mg/s during this time step,
				// to get the value for one step multiply with the step length; error value:
				// -2^30
				
				// 1/*averageFuelConsumption (calcular)*/,

				this.sumo.do_job_set(Vehicle.setSpeedMode(this.idAuto, 0));
				this.setSpeed(speed);

			} else {
				this.carOn = false;
				System.out.println("SUMO is closed...");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(this.idAuto + " erro no sumo.");
			return this.updateDrivingData("encerrado");			
		}

		return repport;
	}

	private double[] convertGeo() throws Exception {
		SumoPosition2D sumoPosition2D = (SumoPosition2D) sumo.do_job_get(Vehicle.getPosition(this.idAuto));

		double x = sumoPosition2D.x; // coordenada X em metros
		double y = sumoPosition2D.y; // coordenada Y em metros

		double raioTerra = 6371000; // raio medio da Terra em metros
		
		// retirados do sumo
		double latRef = -22.986731;
		double lonRef = -43.217054;

		// Conversao de metros para graus
		double lat = latRef + (y / raioTerra) * (180 / Math.PI);
		double lon = lonRef + (x / raioTerra) * (180 / Math.PI) / Math.cos(latRef * Math.PI / 180);

		double[] coordGeo = new double[] { lat, lon };
		return coordGeo;
	}

	private double calcDesloc(double lat1, double lon1, double lat2, double lon2) {
		double raioTerra = 6371000;
	
		// Diferenças das latitudes e longitudes
		double latDiff = Math.toRadians(lat2 - lat1);
		double lonDiff = Math.toRadians(lon2 - lon1);
	
		// Fórmula de Haversine
		double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
		Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(lonDiff / 2) * Math.sin(lonDiff / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distancia = raioTerra * c;
	
		return distancia;
	}

	private double updateDistance(double _previousLat, double _previousLon,double _currentLat, double _currentLon) throws Exception {

		double deslocamento = calcDesloc(_previousLat, _previousLon, _currentLat, _currentLon);
		this.distanceCovered += deslocamento;

		if (this.distanceCovered > (this.carRepport.getDistance() + EnvSimulator.PAYABLE_DISTANCE)) {
			return this.carRepport.getDistance() + (EnvSimulator.PAYABLE_DISTANCE);
		}
		return this.carRepport.getDistance();
	} // TODO retornar double e fazer this.distance ser a distancia acumulada

	private DrivingData updateDrivingData(String _carState, String _driverLogin,long _timeStamp, String _autoID, String _routeIDSUMO,
	double _speed, double _distance, double _fuelConsumption, int _fuelType, double _co2Emission, double _longitude, double _latitude)
	{
		DrivingData repport = new DrivingData(_carState, _driverLogin, _timeStamp, _autoID, _routeIDSUMO, _speed, _distance, _fuelConsumption,
		_fuelType, _co2Emission,_longitude, _latitude);
		return repport;
	}

	private DrivingData updateDrivingData(String _carState, String _routeIDSUMO)
	{
		DrivingData repport = updateDrivingData(_carState, this.driverLogin, 0 , this.idAuto, _routeIDSUMO, 0, 0,
		0, this.fuelType, 0,0,0);
		return repport;	
	}

	private DrivingData updateDrivingData(String _carState)
	{
		DrivingData repport = updateDrivingData(_carState, this.route.getRouteID());
		return repport;	
	}

	public String getDriverLogin() {
		return driverLogin;
	}

	public double getDistanceCovered() {
		return distanceCovered;
	}

	public RouteN getRoute() {
		return route;
	}

	public boolean iscarOn() {
		return this.carOn;
	}

	public void setcarOn(boolean _carOn) {
		this.carOn = _carOn;
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
		boolean taNoSUMO = MobilityCompany.estaNoSUMO(this.idAuto, this.sumo); //TODO IllegalStateException
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
}