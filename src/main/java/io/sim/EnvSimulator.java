package io.sim;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

import de.tudresden.sumo.objects.SumoColor;
import io.sim.created.bank.AlphaBank;
import io.sim.created.Driver;
import io.sim.created.FuelStation;
import io.sim.created.RouteN;
import io.sim.created.TimeStep;
import io.sim.created.company.MobilityCompany;
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
	private static final String ROTAS_XML = "data/dados4.xml"; // "data/dados.xml"
	public static final int NUM_DRIVERS = 100; // ideal 100
	private static final int NUM_BOMBAS = 2;
	// Atributos Carros
	private static final int FUEL_TYPE = 2;
	private static final int FUEL_PREFERENTIAL = 2;
	private static final int PERSON_CAPACITY = 1;
	private static final int PERSON_NUMBER = 1;
	public static final double MAX_FUEL_TANK = 10; // 10 litros
	public static final double MIN_FUEL_TANK = 3; // 3 litros
	public static final double SPEED_DEFAULT = 80; // Km/h
	public static final double FUEL_CONSUMPTION = 3; // ml/s
	// Atributos de pagamento
	public static final double RUN_PRICE = 3.25;
	public static final double FUEL_PRICE = 5.87;
	public static final double PAYABLE_DISTANCE = 1000; // ideal 1000
	// Tempos
	public static final int ACQUISITION_RATE = 300;
	private static final long FUEL_TIME = 120; // em segundos, ideal 120

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
			TimeStep tStep = new TimeStep(this.sumo);
			tStep.start();

			String lHost = "127.0.0.1";
			// Extraindo rotas
			ArrayList<RouteN> routes = RouteN.extractRoutes(ROTAS_XML);
			System.out.println("ES - " + routes.size() + " rotas disponiveis.");

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

			ArrayList<Driver> drivers = new ArrayList<Driver>();
			for(int i=0;i<NUM_DRIVERS;i++)
			{
				SumoColor cor = new SumoColor(0, 255, 0, 126);// TODO funcao para cria cor
				String driverID = "D" + (i+1);
				// TODO host unico para cada e outros para drivers;
				Car car = new Car(true,lHost,PORT_COMPANY, ("CAR" + (i+1)), driverID, cor, driverID, sumo, ACQUISITION_RATE, FUEL_TYPE, FUEL_PREFERENTIAL, FUEL_PRICE,
				PERSON_CAPACITY, PERSON_NUMBER);
				Driver driver = new Driver(lHost, PORT_BANK, driverID, car, ACQUISITION_RATE);
				drivers.add(driver);
			}
			iniciaDrivers(drivers);
			aguardaDrivers(drivers);
			fuelStation.setStationOn(false);
			company.join();
			bank.join();
			companyServer.close();
			bankServer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Encerrando EnvSimulator");
    }

	/**Roda o metodo join em todos os Drivers.
     * @param lista
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

	private static void iniciaDrivers(ArrayList<Driver> _lista) throws InterruptedException
    {
        for(int i=0;i<_lista.size();i++)
        {
			Driver d =_lista.get(i);
			System.out.println("aguardar " + d.getDriverID());
            d.start();
			sleep(5/3*ACQUISITION_RATE);
        }
    }

}
