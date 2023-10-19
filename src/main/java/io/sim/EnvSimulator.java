package io.sim;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

import de.tudresden.sumo.objects.SumoColor;
import io.sim.created.Driver;
import io.sim.created.MobilityCompany;
import io.sim.created.RouteN;
import io.sim.created.TimeStep;
import it.polito.appeal.traci.SumoTraciConnection;

/**Classe que faz a conexao com o SUMO e cria os objetos da simulacao. 
 * Acaba funcionando como uma classe principal
 */
public class EnvSimulator extends Thread
{
    private SumoTraciConnection sumo;
	private static final int PORT_SUMO = 12345; // NEWF
	private static final int PORT_COMPANY = 11111;
	private static final int PORT_BANK = 22222;
	private static final String ROTAS_XML = "data/dados.xml"; // NEWF
	private static final int ACQUISITION_RATE = 500;
	private static final int NUM_DRIVERS = 100; // ideal 100
	private static final int FUEL_TYPE = 2;
	private static final int FUEL_PREFERENTIAL = 2;
	private static final double FUEL_PRICE = 3.40;
	private static final int PERSON_CAPACITY = 1;
	private static final int PERSON_NUMBER = 1;

    public EnvSimulator(){}

    public void run()
	{
		/* SUMO */
		String sumo_bin = "sumo-gui";		
		String config_file = "map/map.sumo.cfg";
		
		// Sumo connection
		this.sumo = new SumoTraciConnection(sumo_bin, config_file);
		sumo.addOption("start", "1"); // auto-run on GUI show
		sumo.addOption("quit-on-end", "1"); // auto-close on end IMP# para testes remover

		try
		{
			sumo.runServer(PORT_SUMO); // porta servidor SUMO
			System.out.println("SUMO conectado.");
			Thread.sleep(5000);
			TimeStep tStep = new TimeStep(this.sumo, ACQUISITION_RATE);
			tStep.start();

			String lHost = "localhost";
			ArrayList<RouteN> routes = RouteN.extractRoutes(ROTAS_XML);
			System.out.println("ES - " + routes.size() + " rotas disponiveis.");
			ServerSocket companyServer = new ServerSocket(PORT_COMPANY);
			MobilityCompany company = new MobilityCompany(lHost, PORT_BANK,companyServer, routes, NUM_DRIVERS, sumo, ACQUISITION_RATE);
			company.start();

			ArrayList<Driver> drivers = new ArrayList<Driver>();
			for(int i=0;i<NUM_DRIVERS;i++)
			{
				SumoColor cor = new SumoColor(0, 255, 0, 126);// IMP# funcao para cria cor
				String driverID = "D" + (i+1);
				//String lHost = "localhost"; // IMP# host unico para cada e outros para drivers;
				Auto car = new Auto(true,lHost,PORT_COMPANY, ("CAR" + (i+1)), cor, driverID, sumo, ACQUISITION_RATE, FUEL_TYPE, FUEL_PREFERENTIAL, FUEL_PRICE,
				PERSON_CAPACITY, PERSON_NUMBER);
				Driver driver = new Driver(lHost, PORT_BANK, driverID, car, ACQUISITION_RATE);
				drivers.add(driver);
			}
			iniciaDrivers(drivers);
			aguardaDrivers(drivers);
			companyServer.close();
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
			System.out.println("aguardar " + d.getDriverID());
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
			sleep(ACQUISITION_RATE);
        }
    }

}
