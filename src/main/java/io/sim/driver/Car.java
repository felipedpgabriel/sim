package io.sim.driver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;
import de.tudresden.sumo.objects.SumoStringList;
import io.sim.company.MobilityCompany;
import io.sim.company.RouteN;
import io.sim.messages.Cryptography;
import io.sim.messages.JSONconverter;
import io.sim.simulation.EnvSimulator;
import it.polito.appeal.traci.SumoTraciConnection;
import it.polito.appeal.traci.TraCIException;

/**Define os atributos que coracterizam um Carro.
 *
 */
public class Car extends Vehicle implements Runnable
{
	// atributos de cliente
	private String carHost;
	private int servPort;
	private DataInputStream entrada;
	private DataOutputStream saida;
	// atributos da classe
	private String carState;
	private boolean carOn;
	private String carID;
	private String driverLoginID;
	private SumoColor carColor;
	private SumoTraciConnection sumo;
	private int fuelType; 			// 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
	private int personCapacity;		// the total number of persons that can ride in this vehicle
	private int personNumber;		// the total number of persons which are riding in this vehicle

	private DrivingData carRepport;
	private TransportService ts;
	private RouteN route;
	private double distanceCovered;
	private double fuelTank;
	private double speedDefault;
	private boolean finished;
	private boolean abastecendo;
	private int av;

	/**
	 * Construtor da classe Car.
	 * @param _carOn boolean - Define se o Car esta ativo para rodar o loop principal.
	 * @param _carHost String - Host para a conexao como cliente.
	 * @param _servPort int - Porta para conexao como cliente.
	 * @param _carID String - ID do carro.
	 * @param _driverLoginID String - ID/Login do Driver.
	 * @param _carColor SumoColor - Cor do carro.
	 * @param _sumo SumoTraciConnection - Objeto sumo.
	 * @param _fuelType int - Tipo de combustivel do Car.
	 * @param _personCapacity int - Capacidade de pessoas para transporte.
	 * @param _personNumber int - Numero de pessoas no Car.
	 * @param _av int - Indica qual avaliacao a simulacao se refere.
	 * @throws Exception
	 */
	public Car(boolean _carOn, String _carHost,int _servPort, String _carID, String _driverLoginID, SumoColor _carColor, 
	SumoTraciConnection _sumo, int _fuelType, int _personCapacity, int _personNumber, int _av) throws Exception
	{
		this.carHost = _carHost;
		this.servPort = _servPort;
		this.carState = "aguardando";
		this.carOn = _carOn;
		this.carID = _carID;
		this.driverLoginID = _driverLoginID;
		this.carColor = _carColor;
		this.sumo = _sumo;
		this.setFuelType(_fuelType);
		this.personCapacity = _personCapacity;
		this.personNumber = _personNumber;
		this.carRepport = this.updateDrivingData(this.carState, "");
		this.fuelTank = EnvSimulator.MAX_FUEL_TANK * 1000; // passa para ml
		this.speedDefault = EnvSimulator.SPEED_DEFAULT/3.6;
		this.finished = false;
		this.abastecendo = false;
		this.av = _av;
	}

	@Override
	public void run()
	{
		System.out.println(this.carID + " iniciado.");
		try
		{
			// Conexoes cliente
            Socket socket = new Socket(this.carHost, this.servPort);
            entrada = new DataInputStream(socket.getInputStream());
            saida = new DataOutputStream(socket.getOutputStream());

			CarFuelManager cfm = new CarFuelManager(this); 
			cfm.start();

			while(!finished)
			{
				write(this.carRepport);
				System.out.println(this.carID + " aguardando rota.");
				route = (RouteN) read();
				if(route.getRouteID().equals("-1"))
				{
					System.out.println(this.carID +" - Sem rotas a receber.");
					finished = true;
					break;
				}
				System.out.println(this.carID + " iniciando rota " + route.getRouteID());
				ts = new TransportService(this.carID, route,this, this.sumo);
				ts.start();
				String edgeFinal = this.getEdgeFinal(); 
				this.carOn = true;
				while(!MobilityCompany.estaNoSUMO(this.carID, this.sumo)) //esperar estar no SUMO
				{
					Thread.sleep(EnvSimulator.ACQUISITION_RATE/2);
				}
				// atualiza informacoes iniciais
				if(av == 2)
				{
					this.carState = "fluxo atingido";
					this.carRepport.setTimestamp(System.nanoTime());
					this.carRepport.setCarState(this.carState);
					this.carRepport.setRouteIDSUMO(this.route.getRouteID());
					write(this.carRepport);
				}
				
				String edgeAtual = (String) this.sumo.do_job_get(Vehicle.getRoadID(this.carID));
				double[] coordGeo = this.convertGeo();
				double previousLat = coordGeo[0];
				double previousLon = coordGeo[1];
				this.distanceCovered = 0;

				while (this.carOn) // && MobilityCompany.estaNoSUMO(this.carID, sumo)
				{
					if(isRouteFineshed(edgeAtual, edgeFinal)) // TODO IllegalStateException
					{
						System.out.println(this.carID + " acabou a rota " + this.route.getRouteID());
						this.carState = "finalizado";
						this.carRepport.setCarState(this.carState); //= this.updateDrivingData(this.carState);
						write(this.carRepport);
						this.carOn = false;
						break;
					}
					Thread.sleep(EnvSimulator.ACQUISITION_RATE);
					if(!isRouteFineshed(edgeAtual, edgeFinal))
					{
						this.carRepport = this.atualizaSensores(previousLat, previousLon); // TODO tentar trocar para TransportService
						cfm.setFuelConsumption(this.carRepport.getFuelConsumption()/800);
						if(this.carRepport.getCarState().equals("finalizado"))
						{
							write(this.carRepport);
							this.carOn = false;
							break;
						}
						else
						{
							previousLat = carRepport.getLatitude();
							previousLon = carRepport.getLongitude();
							while(!MobilityCompany.estaNoSUMO(this.carID, this.sumo))
							{
								Thread.sleep(EnvSimulator.ACQUISITION_RATE/10);
							}
							edgeAtual = (String) this.sumo.do_job_get(Vehicle.getRoadID(this.carID));
							if(av == 2 && parseFlowReached(edgeAtual))
							{
								this.carState = "fluxo atingido";
								this.carRepport.setCarState(this.carState);
							}
							write(this.carRepport);
						}
					}
				}

				if(!finished)
				{
					this.carRepport = this.updateDrivingData("aguardando");
				}
				if(finished)
				{
					this.carRepport = this.updateDrivingData("encerrado");
				}
			}
			System.out.println("Encerrando: " + carID);
			entrada.close();
			saida.close();
			socket.close();
        }
		catch (TraCIException e)
		{
			System.out.println(this.carID + " erro na rota: " + this.carRepport.getRouteIDSUMO());
			this.carState = "encerrado";
			this.carRepport = this.updateDrivingData(this.carState);
			try {
				write(this.carRepport);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
            e.printStackTrace();
        }
		catch (Exception e)
		{
            e.printStackTrace();
        }

		System.out.println(this.carID + " encerrado.");
	}

	/**
     * Atualiza os dados de direcao do carro e retorna um objeto DrivingData atualizado.
     *
     * @param _initiLat double - Latitude inicial.
     * @param _initLon double - Longitude inicial.
     * @return DrivingData - Objeto com os dados de direcao atualizados.
     */
	private DrivingData atualizaSensores(double _initiLat, double _initLon) {
		DrivingData repport = updateDrivingData("aguardando", "");
		try {
			if (!this.sumo.isClosed()) {
				double[] coordGeo = this.convertGeo();
				double currentLat = coordGeo[0];
				double currentLon = coordGeo[1];
				double carSpeed;

				if(this.abastecendo)
				{
					this.carState = "abastecendo";
					carSpeed = 0;
				}
				else
				{
					this.carState = "rodando";
					carSpeed = this.speedDefault;
				}

				double sumoSensorSpeed = 0;
				double sumoSensorFuelConsumption = 0;
				double sumoSensorCO2Emission = 0;
				while(!MobilityCompany.estaNoSUMO(this.carID, this.sumo))
				{
					Thread.sleep(EnvSimulator.ACQUISITION_RATE/10);
				}
				sumoSensorSpeed = (double) this.sumo.do_job_get(Vehicle.getSpeed(this.carID));
				sumoSensorFuelConsumption = (double) this.sumo.do_job_get(Vehicle.getFuelConsumption(this.carID));
				sumoSensorCO2Emission = (double) this.sumo.do_job_get(Vehicle.getCO2Emission(this.carID));
				// Criacao dos dados de conducao do veiculo
				repport = updateDrivingData(this.carState, this.driverLoginID, System.nanoTime(), this.carID, this.route.getRouteID(),
				sumoSensorSpeed, this.updateDistance(currentLat,currentLon,_initiLat, _initLon), sumoSensorFuelConsumption, this.fuelType,
				sumoSensorCO2Emission, currentLon, currentLat);
						
				// Vehicle's fuel consumption in ml/s during this time step,
				// to get the value for one step multiply with the step length; error value:
				// -2^30

				// Vehicle's CO2 emissions in mg/s during this time step,
				// to get the value for one step multiply with the step length; error value:
				// -2^30
				
				// 1/*averageFuelConsumption (calcular)*/,
				this.setSpeed(carSpeed);

			} else {
				this.carOn = false;
				this.carState = "finalizado";
				this.carRepport = this.updateDrivingData(this.carState);
				System.out.println("SUMO is closed...");
			}
		} catch (Exception e) {
			System.out.println(this.carID + " erro no sumo.");
			return this.updateDrivingData("encerrado");			
		}

		return repport;
	}

	/**
     * Converte as coordenadas x e y do Sumo para latitude e longitude.
     *
     * @return double[] - Array contendo a latitude e longitude convertidas.
     * @throws Exception
     */
	private double[] convertGeo() throws Exception {
		SumoPosition2D sumoPosition2D = (SumoPosition2D) sumo.do_job_get(Vehicle.getPosition(this.carID));

		double x = sumoPosition2D.x; // Coordenada X em metros
		double y = sumoPosition2D.y; // Coordenada Y em metros

		double raioTerra = 6378100; // Raio medio da Terra em metros
		
		// Retirados do sumo
		double latRef = -22.986731;
		double lonRef = -43.217054;

		// Conversao de metros para graus
		double lat = latRef + (y / raioTerra) * (180 / Math.PI);
		double lon = lonRef + (x / raioTerra) * (180 / Math.PI) / Math.cos(latRef * Math.PI / 180);

		double[] coordGeo = new double[] { lat, lon };
		return coordGeo;
	}

	/**
     * Calcula o deslocamento entre duas coordenadas geograficas.
     *
     * @param lat1 double - Latitude da posicao inicial.
     * @param lon1 double - Longitude da posicao inicial.
     * @param lat2 double - Latitude da posicao final.
     * @param lon2 double - Longitude da posicao final.
     * @return double - Distancia em metros entre as duas posicoes.
     */
	public double calcDesloc(double lat1, double lon1, double lat2, double lon2) {
		double raioTerra = 6378100;
	
		// Diferencas das latitudes e longitudes
		double latDiff = Math.toRadians(lat2 - lat1);
		double lonDiff = Math.toRadians(lon2 - lon1);
	
		// Formula de Haversine
		double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
		Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(lonDiff / 2) * Math.sin(lonDiff / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distancia = raioTerra * c;
	
		return distancia;
	}

	/**
     * Atualiza a distancia percorrida pelo carro com base em coordenadas geograficas
     * anteriores e atuais.
     *
     * @param _previousLat  double - Latitude anterior.
     * @param _previousLon  double - Longitude anterior.
     * @param _currentLat   double - Latitude atual.
     * @param _currentLon   double - Longitude atual.
     * @return double - Distancia atualizada em metros.
     * @throws Exception
     */
	private double updateDistance(double _previousLat, double _previousLon,double _currentLat, double _currentLon) throws Exception
	{

		double deslocamento = calcDesloc(_previousLat, _previousLon, _currentLat, _currentLon);
		this.distanceCovered += deslocamento;
		if(this.av == 2)
		{
			return this.distanceCovered;
		}
		else
		{
			if (this.distanceCovered > (this.carRepport.getDistance() + EnvSimulator.PAYABLE_DISTANCE))
			{
				return this.carRepport.getDistance() + (EnvSimulator.PAYABLE_DISTANCE);
			}
			return this.carRepport.getDistance();
		}
	} 

	/**
     * Atualiza o objeto DrivingData com os parametros fornecidos.
     *
     * @param _carState String - Estado do carro.
     * @param _driverLoginID String - ID/Login do motorista.
     * @param _timeStamp long - Carimbo de data/hora.
     * @param _autoID String - ID do carro.
     * @param _routeIDSUMO String - ID da rota no SUMO.
     * @param _speed double - Velocidade do carro.
     * @param _distance double - Distancia percorrida pelo carro.
     * @param _fuelConsumption double - Consumo de combustivel do carro.
     * @param _fuelType int - Tipo de combustivel.
     * @param _co2Emission double - Emissao de CO2 do carro.
     * @param _longitude double - Longitude atual do carro.
     * @param _latitude double - Latitude atual do carro.
     * @return DrivingData - Objeto com dados de direcao atualizados.
     */
	private DrivingData updateDrivingData(String _carState, String _driverLoginID,long _timeStamp, String _autoID, String _routeIDSUMO,
	double _speed, double _distance, double _fuelConsumption, int _fuelType, double _co2Emission, double _longitude, double _latitude)
	{
		DrivingData repport = new DrivingData(_carState, _driverLoginID, _timeStamp, _autoID, _routeIDSUMO, _speed, _distance, _fuelConsumption,
		_fuelType, _co2Emission,_longitude, _latitude);
		return repport;
	}

	/**
	 * Sobrecarga do metodo updateDrivingData. 
	 * @param _carState String - Estado do carro.
	 * @param _routeIDSUMO String - ID da rota no SUMO.
	 * @return DrivingData - Objeto com dados de direcao atualizados.
	 */
	private DrivingData updateDrivingData(String _carState, String _routeIDSUMO)
	{
		DrivingData repport = updateDrivingData(_carState, this.driverLoginID, 0 , this.carID, _routeIDSUMO, 0, 0,
		0, this.fuelType, 0,0,0);
		return repport;	
	}

	private DrivingData updateDrivingData(String _carState)
	{
		DrivingData repport = updateDrivingData(_carState, this.route.getRouteID());
		return repport;	
	}

	public void setSpeed(double _speed) throws Exception
	{
		this.sumo.do_job_set(Vehicle.setSpeedMode(this.carID, 27)); // [0 1 1 0 1 1]
		this.sumo.do_job_set(Vehicle.setSpeed(this.carID, _speed));
	}

	private void write(DrivingData _carRepport) throws Exception
	{
		String jsMsg = JSONconverter.drivingDataToString(_carRepport);
		byte[] msgEncrypt = Cryptography.encrypt(jsMsg);
		saida.writeInt(msgEncrypt.length);
		saida.write(msgEncrypt);

		// chegou em um fluxo
		if(av == 2 && this.carState.equals("fluxo atingido"))
		{
			this.updateFlowList();
		}
	}

	private boolean parseFlowReached(String _currentEdge)
	{
		if(this.route.getEdgesList().isEmpty())
		{
			return false;
		}
		else
		{
			return this.route.getEdgesList().get(0).equals(_currentEdge);
		}
	}

	private void updateFlowList()
	{
		for(int i=0; i<EnvSimulator.FLOW_SIZE;i++)
		{
			this.route.getEdgesList().remove(0);
		}
	}

	private RouteN read() throws Exception
	{
		int numBytes = entrada.readInt();
		byte[] msgEncrypt = entrada.readNBytes(numBytes);
		String msgDecrypt = Cryptography.decrypt(msgEncrypt);
		return JSONconverter.stringToRouteN(msgDecrypt);
	}

	private boolean isRouteFineshed(String _edgeAtual, String _edgeFinal)
	{
		boolean taNoSUMO = MobilityCompany.estaNoSUMO(this.carID, this.sumo); //TODO IllegalStateException
		return !taNoSUMO && (_edgeFinal.equals(_edgeAtual));
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

	/**
	 * Set padrao para o atributo carState.
	 * @param carState String - Estado do Car.
	 */
	public void setcarState(String _carState) {
		this.carState = _carState;
	}

	/**
	 * Get booleano para o atributo carOn.
	 * @return boolean.
	 */
	public boolean iscarOn() {
		return this.carOn;
	}

	/**
	 * Set booleano para o atributo carOn.
	 * @param _carOn boolean,
	 */
	public void setcarOn(boolean _carOn) {
		this.carOn = _carOn;
	}

	/**
	 * Get padrao para o atributo getCarID.
	 * @return String - ID do carro.
	 */
	public String getCarID() {
		return this.carID;
	}

	/**
	 * Get padrao para o atributo carColor.
	 * @return SumoColor - Cor do car.
	 */
	public SumoColor getCarColor() {
		return this.carColor;
	} 

	/**
	 * Set para o atributo fuelType, com tratamento de limites validos.
	 * @param _fuelType int - Tipo de combustivel.
	 */
	public void setFuelType(int _fuelType) {
		if((_fuelType < 0) || (_fuelType > 4)) {
			this.fuelType = 4;
		} else {
			this.fuelType = _fuelType;
		}
	}

	/**
	 * Get padrao para o atributo personCapacity.
	 * @return int - Capacidade de pessoas para transporte.
	 */
	public int getPersonCapacity() {
		return this.personCapacity;
	}

	/**
	 * Get padrao para o atributo personNumber.
	 * @return int - Numero de pessoas no Car.
	 */
	public int getPersonNumber() {
		return this.personNumber;
	}

	/**
	 * Get padrao para o atributo carRepport.
	 * @return Drivingdata - Estado atual do Car.
	 */
	public DrivingData getCarRepport() {
		return this.carRepport;
	}

	/**
	 * Get padrao para o atributo route.
	 * @return RouteN - Rota para execucao.
	 */
	public RouteN getRoute() {
		return this.route;
	}

	public double getDistanceCovered() {
		return distanceCovered;
	}

	public synchronized double getFuelTank() {
		return fuelTank;
	}

	public synchronized void setFuelTank(double _fuelTank) {
		this.fuelTank = _fuelTank;
	}

	public double getSpeedDefault() {
		return speedDefault;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setfinished(boolean finished) {
		this.finished = finished;
	}

	public boolean isAbastecendo() {
		return abastecendo;
	}

	public void setAbastecendo(boolean abastecendo) {
		this.abastecendo = abastecendo;
	}
}