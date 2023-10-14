package io.sim;

import de.tudresden.sumo.cmd.Route;
import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;

import io.sim.created.RouteN;

/**Cria o objeto veiculo no SUMO
 * Define cor e a rota.
 */
public class TransportService extends Thread {

	private String idTransportService;
	private boolean on_off;
	private SumoTraciConnection sumo;
	private Auto car; // Veiculo correspondente 
	private RouteN route; // representa a rota a ser cumprida
	private SumoStringList edge; // NEWF
	private boolean finished = false; // chamado pelo Auto no caso Car
	private boolean sumoInit = false;
	private boolean sumoReady = false;

	public TransportService(boolean _on_off, String _idTransportService, Auto _car,
			SumoTraciConnection _sumo) { // tirei RouteN _route.

		this.on_off = _on_off;
		this.idTransportService = _idTransportService;
		// this.route = _route;
		this.car = _car;
		this.sumo = _sumo;
	}

	@Override
	public void run() {
		System.out.println("Iniciando TransportService.");
		// logica para colocar num loop com while(!on_off), mas smp que sair, permanecer e 
		while(!this.finished)
		{
			try {
				// cuidar para fazer só quando receber rota
				if(this.on_off)
				{
					System.out.println("TS - on");
					while (this.on_off)
					{
						if(!this.sumoInit)
						{
							System.out.println("TS - entrou na criacao");
							this.initializeRoutes();
							System.out.println("TS - Rota: " + edge + " adcionada!");
							String edgeFinal = edge.get(edge.size()-1);
							System.out.println("TS - Edge final: "+edgeFinal);
							
						}
						if (this.getSumo().isClosed()) {
							this.on_off = false;
							this.sumoReady = false;
							System.out.println("TS - SUMO is closed...");
						}
						try {
							this.sumo.do_timestep(); // 
						} catch (Exception e) {
						}
						String edgeAtual = (String) this.sumo.do_job_get(Vehicle.getRoadID(this.car.getIdAuto()));
						System.out.println("TS - Edge atual: "+edgeAtual);
						Thread.sleep(this.car.getAcquisitionRate());
						// if(edgeAtual.equals(edgeFinal))
						// {
						// 	System.out.println("TS - "+car.getName() + " encerrou a rota.");
						// 	this.car.setSpeed(0.0);
						// }
					}
					sumoInit = false;
					sumoReady = false;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		System.out.println("Encerrando TransportService.");
	}

	private void initializeRoutes() {

		// Adiciona todas as edges em uma lista de Strings
		edge = new SumoStringList(); // NEWF
		edge.clear();
		String aux = this.route.getEdges();// String[] aux = this.itinerary.getItinerary();

		// for (String e : aux[1].split(" ")) {
		// 	edge.add(e);
		// }

		for(String e : aux.split(" "))
		{
			edge.add(e);
		}

		try {// Inicializa a rota, veiculo e a cor do veiculo
			sumo.do_job_set(Route.add(this.route.getRouteID(), edge)); // ROUTES trocar por this.route.getRouteID()
			//sumo.do_job_set(Vehicle.add(this.auto.getIdAuto(), "DEFAULT_VEHTYPE", this.itinerary.getIdItinerary(), 0,
			//		0.0, 0, (byte) 0));
			
			// MUDARF com Car herdando Vehicle, esse passo pode se tornar obsoleto
			sumo.do_job_set(Vehicle.addFull(this.car.getIdAuto(), 				//vehID
											this.route.getRouteID(), 	//routeID this.route.getRouteID()
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
			
			sumo.do_job_set(Vehicle.setColor(this.car.getIdAuto(), this.car.getColorAuto()));
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		this.sumoInit = true;
		this.sumoReady = true;
	}

	public boolean isOn_off() {
		return on_off;
	}

	public void setOn_off(boolean _on_off) {
		this.on_off = _on_off;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public String getIdTransportService() {
		return this.idTransportService;
	}

	public SumoTraciConnection getSumo() {
		return this.sumo;
	}

	public Auto getcar() {
		return this.car;
	}

	public RouteN getRoute() { // ROUTE
		return this.route;
	}

	public void setRoute(RouteN route) {
		this.route = route;
	}

	public boolean isSumoReady() {
		return sumoReady;
	}

}