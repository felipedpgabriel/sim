package io.sim;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

import de.tudresden.sumo.objects.SumoColor;
import io.sim.created.Driver;
import io.sim.created.MobilityCompany;
import io.sim.created.RouteN;
import it.polito.appeal.traci.SumoTraciConnection;

/**Classe que faz a conexao com o SUMO e cria os objetos da simulacao. 
 * Acaba funcionando como uma classe principal
 */
public class EnvSimulator extends Thread
{
    private SumoTraciConnection sumo;
	private static final int PORT_SUMO = 12345; // NEWF
	private static final int PORT_COMPANY = 11111;
	private static final String ROTAS_XML = "data/dados2.xml"; // NEWF
	private static final int AQUISITION_RATE = 500;
	private static final int NUM_DRIVERS = 1; // ideal 100
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
		sumo.addOption("quit-on-end", "1"); // auto-close on end

		try
		{
			sumo.runServer(PORT_SUMO); // porta servidor SUMO
			System.out.println("SUMO conectado.");
			Thread.sleep(5000);

			// Itinerary i1 = new Itinerary(ROTAS_XML, "0");
			ArrayList<RouteN> routes = RouteN.extractRoutes(ROTAS_XML);
			ServerSocket companyServer = new ServerSocket(PORT_COMPANY);
			MobilityCompany company = new MobilityCompany(companyServer, routes, NUM_DRIVERS);
			company.start();;

			// if (!routes.isEmpty()) //(routes.isEmpty())
			// { //cria um carro e sua instancia no sumo
			// 	RouteN route = routes.get(0); // nao necessario inicialmente
			// 	// fuelType: 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
			// 	SumoColor green = new SumoColor(0, 255, 0, 126); // RGBA -> A eh opacidade // definir cores sortidas.
			// 	Auto car = new Auto(true, "CAR1", green,"D1", sumo, AQUISITION_RATE, FUEL_TYPE, FUEL_PREFERENTIAL,
			// 	FUEL_PRICE, PERSON_CAPACITY, PERSON_NUMBER); // OK
			// 	// mudar de lugar. Colocar no Car
			// 	TransportService tS1 = new TransportService(true, "CAR1", route, car, sumo); // route no lugar de i1
			// 	tS1.start();
            //     Thread.sleep(5000); // SLA
			// 	// a1.start();
			// }

			ArrayList<Driver> drivers = new ArrayList<Driver>();
			for(int i=0;i<NUM_DRIVERS;i++)
			{
				SumoColor cor = new SumoColor(0, 255, 0, 126);// funcao para cria cor
				String driverID = "D" + (i+1);
				String carHost = "localhost";// + i+1;
				Auto car = new Auto(carHost,PORT_COMPANY,true, "CAR" + (i+1), cor, driverID, sumo, AQUISITION_RATE, FUEL_TYPE, FUEL_PREFERENTIAL, FUEL_PRICE,
				PERSON_CAPACITY, PERSON_NUMBER);
				Driver driver = new Driver(driverID, car, AQUISITION_RATE);
				drivers.add(driver);
			}
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
            _lista.get(i).join();
        }
    }

}
