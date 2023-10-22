package io.sim.created.company;

import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
// import java.net.Socket;
import java.util.ArrayList;

import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoStringList;
import io.sim.EnvSimulator;
import io.sim.created.Account;
import io.sim.created.BankService;
import io.sim.created.JSONConverter;
import io.sim.created.RouteN;
import io.sim.created.bank.AlphaBank;
import it.polito.appeal.traci.SumoTraciConnection;

public class MobilityCompany extends Thread
{
    // Atributos de cliente
    private String companyHost;
	private int bankPort;
    // Atributos de servidor
    private static ServerSocket serverSocket;
    private static Account account;
    // Atributos de sincronizacao
    private static Thread oWatch;
    // Atributos da classe
    private static ArrayList<RouteN> routesToExe;
    private static ArrayList<RouteN> routesInExe;
    private static ArrayList<RouteN> routesExecuted;
    private static boolean routesAvailable = true;

    public MobilityCompany(String _companyHost, int _bankPort, ServerSocket _serverSocket, ArrayList<RouteN> _routes, SumoTraciConnection _sumo)
    {
        oWatch = new Thread();
        routesToExe = new ArrayList<RouteN>();
        routesInExe = new ArrayList<RouteN>();
        routesExecuted = new ArrayList<RouteN>();
        account = new Account(100000.0, "MobilityCompany", "mc123");
        companyHost = _companyHost;
        bankPort = _bankPort;
        serverSocket = _serverSocket;
        routesToExe = _routes;
    }

    @Override
    public void run()
    {
        try
        {
            System.out.println("MobilityCompany iniciada...");
            
            AlphaBank.setConectionsInit(true);
            Socket socketCli = new Socket(this.companyHost, this.bankPort);
            DataOutputStream saidaCli = new DataOutputStream(socketCli.getOutputStream());

            CompanyChannelCreator ccc = new CompanyChannelCreator(socketCli, serverSocket, account);
            ccc.start();

            boolean fimRotasNotificado = false; // evita que a mensagem "Rotas terminadas" seja enviado continuamente

            while (routesAvailable || !routesInExe.isEmpty()) // || !routesInExe.isEmpty() IMP trocar para pagamentos
            {
                sleep(EnvSimulator.ACQUISITION_RATE);
                if(routesToExe.isEmpty() && !fimRotasNotificado) // && routesInExe.isEmpty()
                {
                    System.out.println("Rotas terminadas");
                    routesAvailable = false;
                    fimRotasNotificado = true;
                }
            }
            BankService bs = BankService.createService("Encerrar");
            saidaCli.writeUTF(JSONConverter.bankServiceToString(bs));
            socketCli.close();
            System.out.println("MobilityCompany encerrada...");
            System.out.println("Saldo Company: " + account.getSaldo());
            AlphaBank.encerrarConta(account.getLogin());
            }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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
}