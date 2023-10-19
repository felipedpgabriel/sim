package io.sim.created;

import java.net.ServerSocket;
// import java.net.Socket;
import java.util.ArrayList;

import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;

public class MobilityCompany extends Thread
{
    // Atributos de servidor
    private ServerSocket serverSocket;
    // Atributos de cliente
    private String companyHost;
	private int bankPort;
    // private Socket socket;
    // private static Account account;
    // Atributos de sincronizacao
    private static Thread oWatch;
    // Atributos da classe
    private static ArrayList<RouteN> routesToExe;
    private static ArrayList<RouteN> routesInExe;
    private static ArrayList<RouteN> routesExecuted;
    // private static final double RUN_PRICE = 3.25;
    private static int numDrivers;
    private static boolean routesAvailable = true;
    private long acquisitionRate; 

    public MobilityCompany(String _companyHost, int _bankPort, ServerSocket _serverSocket, ArrayList<RouteN> _routes, int _numDrivers, SumoTraciConnection _sumo, long _acquisitionRate)
    {
        oWatch = new Thread();
        routesToExe = new ArrayList<RouteN>();
        routesInExe = new ArrayList<RouteN>();
        routesExecuted = new ArrayList<RouteN>();
        // BotPayment payment = new BotPayment(RUN_PRICE);
        this.companyHost = _companyHost;
        this.bankPort = _bankPort;
        this.serverSocket = _serverSocket;
        numDrivers = _numDrivers;
        routesToExe = _routes;
        this.acquisitionRate = _acquisitionRate;
    }

    @Override
    public void run()
    {
        try
        {
            System.out.println("MobilityCompany iniciada...");

            CompanyChannelCreator ccc = new CompanyChannelCreator(serverSocket, numDrivers);
            ccc.start();

            while (routesAvailable) // || !routesInExe.isEmpty() IMP trocar para pagamentos
            {
                sleep(this.acquisitionRate);
                if(routesToExe.isEmpty()) // && routesInExe.isEmpty()
                {
                    System.out.println("Rotas terminadas");
                    routesAvailable = false;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        System.out.println("MobilityCompany encerrada...");
    }
    
    /**Libera uma rota para o cliente que a solicitou. Para isso, remove de routesToExe e adiciona em routesInExe
     * @return route RouteN - Rota do topo da ArrayList de rotas
     */
    public static RouteN liberarRota()
    {
        synchronized (oWatch)
        {
            if(!routesAvailable) // entrou no liberarRota(), mas acabou durante a espera
            {
                System.out.println("SMC - Sem mais rotas para liberar.");
                RouteN route = new RouteN("-1", "00000");
                // saida.writeUTF(routeNtoString(route));
                return route;
            }
            RouteN route = routesToExe.remove(0);
            routesInExe.add(route); // mudar para routesInExe.add(car.getID(),route) ou route.getID()
            System.out.println("SMC - Liberando rota:\n" + route.getRouteID());
            return route;
        }
    }

    public static void arquivarRota(String _routeID)
    {
        synchronized (oWatch)
        {
            System.out.println("Arquivando rota: " + _routeID);
            for(int i=0;i<routesInExe.size();i++)
            {
                if(routesInExe.get(i).getRouteID().equals(_routeID))
                {
                    routesExecuted.add(routesInExe.remove(i));
                    break;
                }
            }
        }
    }

    public static boolean areRoutesAvailable() {
        return routesAvailable;
    }

    public static boolean estaNoSUMO(String _idCar, SumoTraciConnection _sumo)
	{
        synchronized(oWatch){
            try {
                SumoStringList lista;
                lista = (SumoStringList) _sumo.do_job_get(Vehicle.getIDList()); // IMP# IllegalStateException
                return lista.contains(_idCar);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
        }
	}

    public static int getRoutesToExeSize()
    {
        return routesToExe.size();
    }

    public static int getRoutesInExeSize()
    {
        return routesInExe.size();
    }

    public static int getRoutesExecutedSize()
    {
        return routesExecuted.size();
    }

}