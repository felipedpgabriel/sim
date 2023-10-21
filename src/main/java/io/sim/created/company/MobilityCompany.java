package io.sim.created.company;

import java.net.ServerSocket;
// import java.net.Socket;
import java.util.ArrayList;

import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoStringList;
import io.sim.created.Account;
import io.sim.created.RouteN;
import io.sim.created.bank.AlphaBank;
import it.polito.appeal.traci.SumoTraciConnection;

public class MobilityCompany extends Thread
{
    // Atributos de cliente
    private static String companyHost;
	private static int bankPort;
    // Atributos de servidor
    private static ServerSocket serverSocket;
    // private Socket socket;
    private Account account;
    // Atributos de sincronizacao
    private static Thread oWatch;
    // Atributos da classe
    private static ArrayList<RouteN> routesToExe;
    private static ArrayList<RouteN> routesInExe;
    private static ArrayList<RouteN> routesExecuted;
    private static boolean routesAvailable = true;
    private long acquisitionRate; 

    public MobilityCompany(String _companyHost, int _bankPort, ServerSocket _serverSocket, ArrayList<RouteN> _routes, SumoTraciConnection _sumo, long _acquisitionRate)
    {
        oWatch = new Thread();
        routesToExe = new ArrayList<RouteN>();
        routesInExe = new ArrayList<RouteN>();
        routesExecuted = new ArrayList<RouteN>();
        account = new Account(10000.0, "MobilityCompany", "mc123");
        companyHost = _companyHost;
        bankPort = _bankPort;
        serverSocket = _serverSocket;
        routesToExe = _routes;
        this.acquisitionRate = _acquisitionRate;
    }

    @Override
    public void run()
    {
        try
        {
            System.out.println("MobilityCompany iniciada...");
            
            // AlphaBank.setConectionsInit(true);

            CompanyChannelCreator ccc = new CompanyChannelCreator(companyHost, bankPort, serverSocket, account);
            ccc.start();

            boolean fimRotasNotificado = false; // evita que a mensagem "Rotas terminadas" seja enviado continuamente

            while (routesAvailable || !routesInExe.isEmpty()) // || !routesInExe.isEmpty() IMP trocar para pagamentos
            {
                sleep(this.acquisitionRate);
                if(routesToExe.isEmpty() && !fimRotasNotificado) // && routesInExe.isEmpty()
                {
                    System.out.println("Rotas terminadas");
                    routesAvailable = false;
                    fimRotasNotificado = true;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        System.out.println("MobilityCompany encerrada...");
        System.out.println("Saldo Company: " + account.getSaldo());
        // AlphaBank.encerrarConta(account.getLogin());
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
        synchronized(oWatch)
        {
            try {
                SumoStringList lista;
                lista = (SumoStringList) _sumo.do_job_get(Vehicle.getIDList()); // TODO IllegalStateException
                return lista.contains(_idCar);
            } catch (Exception e) {
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

    public Account getAccount() {
        return this.account;
    }

}