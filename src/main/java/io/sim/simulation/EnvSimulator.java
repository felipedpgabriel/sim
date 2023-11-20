package io.sim.simulation;

import java.net.ServerSocket;
import java.util.ArrayList;

import de.tudresden.sumo.objects.SumoColor;
import io.sim.FuelStation;
import io.sim.bank.AlphaBank;
import io.sim.company.MobilityCompany;
import io.sim.company.RouteN;
import io.sim.driver.Car;
import io.sim.driver.Driver;
import io.sim.repport.ExcelRepport;
import it.polito.appeal.traci.SumoTraciConnection;

/**Classe que faz a conexao com o SUMO e cria os objetos da simulacao. 
 * Acaba funcionando como uma classe principal
 */
public class EnvSimulator extends Thread
{
    private SumoTraciConnection sumo;
	// Servidores
	private static final int PORT_SUMO = 12345;
	private static final int PORT_COMPANY = 11111;
	private static final int PORT_BANK = 22222;
	// Quantidades 
	private static final String ROTAS_XML = "data/dadosAV2.xml"; // "data/dadosav2.xml"
	private static final int NUM_BOMBAS = 2;
	public static final int NUM_DRIVERS = 1; // 100 AV1 e 1 AV2
	// Atributos Carros
	private static final int FUEL_TYPE = 2;
	// private static final int FUEL_PREFERENTIAL = 2; NAO USADO
	private static final int PERSON_CAPACITY = 1;
	private static final int PERSON_NUMBER = 1;
	public static final double MAX_FUEL_TANK = 10; // 10 [L]
	public static final double MIN_FUEL_TANK = 3; // 3 [L]
	// SPEED_DEFAULT apenas estimula a velocidade maxima, mas o SpeedMode(31) ja ajusta para o limite da via
	public static final double SPEED_DEFAULT = 120; // 120 Km/h (em Car passa para [m/s])
	// public static final double FUEL_CONSUMPTION = 3; // 3 [mL/s]
	// Atributos de pagamento
	public static final double RUN_PRICE = 3.25; // [R$]
	public static final double FUEL_PRICE = 5.87; // [R$]
	public static final double PAYABLE_DISTANCE = 1000; // ideal 1000 [m]
	// Tempos
	private static final long FUEL_TIME = 120; // ideal 120 [s]
	public static final int ACQUISITION_RATE = 300; // Tempo padrao de sleeps [ms]
	// Configura extracao de rotas
	private static final int AV = 2; // 1 ou 2
	private static final int AV2_cicle = 100; // ideal 100

    /**Construtor vazio
	 * 
	 */
	public EnvSimulator(){}

    public void run()
	{
		/* SUMO */
		String sumo_bin = "sumo-gui";		
		String config_file = "map/map.sumo.cfg";
		
		// Sumo connection
		this.sumo = new SumoTraciConnection(sumo_bin, config_file);
		sumo.addOption("start", "1"); // auto-run on GUI show
		sumo.addOption("quit-on-end", "1"); // auto-close on end 

		try
		{
			// Abrindo SUMO
			sumo.runServer(PORT_SUMO);
			System.out.println("SUMO conectado.");
			Thread.sleep(5000);
			// Tempo da simulacao
			TimeStep tStep = new TimeStep(this.sumo);
			tStep.start();

			String lHost = "127.0.0.1"; // Host padrao para todos os clientes
			
			// Extraindo rotas
			ArrayList<RouteN> routes = new ArrayList<RouteN>();
			int av = AV;
			if(av == 1)
			{
				routes = RouteN.extractRoutes(ROTAS_XML);
				System.out.println("ES - " + routes.size() + " rotas disponiveis.");
			}
			else if(av == 2)
			{
				ArrayList<RouteN> aux_routes = new ArrayList<RouteN>();
				for(int i = 0; i<AV2_cicle; i++)
				{
					aux_routes = RouteN.extractRoutes(ROTAS_XML);
					aux_routes.get(0).setRouteID(Integer.toString(i));
					routes.add(i, aux_routes.get(0));
				}
			}
			else
			{
				System.err.println("Avaliacao invalida! Selecione 1 ou 2 para AV.");
				System.exit(0);
			}

			// Iniciando Servidores
			ServerSocket bankServer = new ServerSocket(PORT_BANK);
			AlphaBank bank = new AlphaBank(bankServer, NUM_DRIVERS + 2);
			bank.start();

			ServerSocket companyServer = new ServerSocket(PORT_COMPANY);
			MobilityCompany company = new MobilityCompany(lHost, PORT_BANK,companyServer, routes, sumo);
			company.start();

			// Inicializando FuelStation
			FuelStation fuelStation = new FuelStation(lHost, PORT_BANK, NUM_BOMBAS, FUEL_TIME);
			fuelStation.start();

			ArrayList<Driver> drivers = new ArrayList<Driver>(); // Lista de Drivers para join e start

			for(int i=0;i<NUM_DRIVERS;i++) // Cria Cars e Drivers
			{
				SumoColor color = new SumoColor(0, 82, 159, 126);// TODO funcao para cria cor
				String driverID = "D" + (i+1);
				Car car = new Car(true,lHost,PORT_COMPANY, ("CAR" + (i+1)), driverID, color, sumo, FUEL_TYPE,
				PERSON_CAPACITY, PERSON_NUMBER);
				Driver driver = new Driver(lHost, PORT_BANK, driverID, car, ACQUISITION_RATE);
				drivers.add(driver);
			}

			// Cria as planilhas de Excel
			ExcelRepport.ssDrivingDataCreator();
			ExcelRepport.ssBankServiceCreator(company.getAccountLogin(), drivers, fuelStation.getAccountLogin());
			
			iniciaDrivers(drivers);
			aguardaDrivers(drivers);
			fuelStation.setStationOn(false);
			company.join();
			bank.join();
			companyServer.close();
			bankServer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Encerrando EnvSimulator");
    }

	/**Roda o metodo join() em todos os Driver's.
     * @param lista ArrayList<Driver> - Lista de Driver's
     * @throws InterruptedException 
     */
    private static void aguardaDrivers(ArrayList<Driver> _lista) throws InterruptedException
    {
        for(int i=0;i<_lista.size();i++)
        {
			Driver d =_lista.get(i);
            d.join();
        }
    }

	/**Roda o metodo start() em todos os Driver's.
	 * @param _lista ArrayList<Driver> - Lista de Driver's
	 * @throws InterruptedException
	 */
	private static void iniciaDrivers(ArrayList<Driver> _lista) throws InterruptedException
    {
        for(int i=0;i<_lista.size();i++)
        {
			Driver d =_lista.get(i);
			System.out.println("aguardar " + d.getDriverID());
            d.start();
			sleep(5/3*ACQUISITION_RATE); // intervalo de 0.5 [s] entre cricao de Driver's
        }
    }

}
