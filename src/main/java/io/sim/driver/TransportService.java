package io.sim.driver;

import de.tudresden.sumo.cmd.Route;
import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoStringList;
import io.sim.simulation.EnvSimulator;
import io.sim.company.RouteN;
import it.polito.appeal.traci.SumoTraciConnection;

/**Cria o objeto veiculo no SUMO
 * Define cor e a rota.
 */
public class TransportService extends Thread {

	private String idTransportService;
	private SumoTraciConnection sumo;
	private Car car; // Veiculo correspondente 
	private RouteN route; // representa a rota a ser cumprida
	private SumoStringList edge;

	public TransportService(String _idTransportService, RouteN _route,Car _car, SumoTraciConnection _sumo)
	{
		this.idTransportService = _idTransportService;
		this.route = _route;
		this.car = _car;
		this.sumo = _sumo;
	}

	@Override
	public void run()
	{
		// System.out.println("Iniciando TransportService - " + this.car.getCarID());
		this.initializeRoutes();
		// System.out.println(this.car.getCarID() + " - TS - on");
		try {
			sleep(EnvSimulator.ACQUISITION_RATE);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// System.out.println("Encerrando TransportService " + this.car.getCarID());
	}

	private void initializeRoutes() { // TODO investigar se existe um retorno para saber se foi bem suscedido (ou se a rota eh valida)

		// Adiciona todas as edges em uma lista de Strings
		edge = new SumoStringList();
		edge.clear();
		String aux = this.route.getEdges();
		for(String e : aux.split(" "))
		{
			edge.add(e);
		}

		try {// Inicializa a rota, veiculo e a cor do veiculo
			sumo.do_job_set(Route.add(this.route.getRouteID(), edge));
			
			sumo.do_job_set(Vehicle.addFull(this.car.getCarID(), 				//vehID
											this.route.getRouteID(), 			//routeID 
											"DEFAULT_VEHTYPE", 					//typeID 
											"now", 								//depart  
											"0", 								//departLane 
											"0", 								//departPos 
											"0",								//departSpeed
											"current",							//arrivalLane 
											"max",								//arrivalPos 
											"current",							//arrivalSpeed 
											"",									//fromTaz 
											"",									//toTaz 
											"", 								//line 
											this.car.getPersonCapacity(),		//personCapacity 
											this.car.getPersonNumber())		//personNumber
					);
			
			sumo.do_job_set(Vehicle.setColor(this.car.getCarID(), this.car.getCarColor()));
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public String getIdTransportService() {
		return this.idTransportService;
	}

	public RouteN getRoute() {
		return this.route;
	}

	public Car getcar() {
		return this.car;
	}

	public SumoTraciConnection getSumo() {
		return this.sumo;
	}

	public void setRoute(RouteN route) {
		this.route = route;
	}
}