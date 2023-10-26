package io.sim.company;

import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoStringList;
import io.sim.bank.Account;
import io.sim.bank.AlphaBank;
import io.sim.bank.BankService;
import io.sim.driver.DrivingData;
import io.sim.messages.Cryptography;
import io.sim.messages.JSONconverter;
import io.sim.repport.ExcelCompany;
import io.sim.simulation.EnvSimulator;
import it.polito.appeal.traci.SumoTraciConnection;

public class MobilityCompany extends Thread
{
    // Atributos de cliente
    private String companyHost;
	private int bankPort;
    private DataOutputStream saidaCli;
    // Atributos de servidor
    private static ServerSocket serverSocket;
    private static Account account;
    // Atributos da classe
    private static ArrayList<RouteN> routesToExe;
    private static ArrayList<RouteN> routesInExe;
    private static ArrayList<RouteN> routesExecuted;
    private static boolean routesAvailable;
    private static ArrayList<DrivingData> carsRepport;

    /**
     * Construtor da classe MobilityCompany
     * @param _companyHost String - 
     * @param _bankPort 
     * @param _serverSocket 
     * @param _routes 
     * @param _sumo 
     */
    public MobilityCompany(String _companyHost, int _bankPort, ServerSocket _serverSocket, ArrayList<RouteN> _routes, SumoTraciConnection _sumo)
    {
        this.companyHost = _companyHost;
        this.bankPort = _bankPort;
        serverSocket = _serverSocket;
        account = new Account(100000.0, "MobilityCompany", "mc123");
        routesToExe = new ArrayList<RouteN>();
        routesToExe = _routes;
        routesInExe = new ArrayList<RouteN>();
        routesExecuted = new ArrayList<RouteN>();
        routesAvailable = true;
        carsRepport = new ArrayList<DrivingData>();
    }

    @Override
    public void run()
    {
        try
        {
            System.out.println("MobilityCompany iniciada...");
            
            AlphaBank.setConectionsInit(true);
            Socket socketCli = new Socket(this.companyHost, this.bankPort);
            saidaCli = new DataOutputStream(socketCli.getOutputStream());

            CompanyChannelCreator ccc = new CompanyChannelCreator(socketCli, serverSocket, account);
            ccc.start();
            ccc.join();
            ExcelCompany ec = new ExcelCompany(this);
            ec.start();

            boolean fimRotasNotificado = false; // evita que a mensagem "Rotas terminadas" seja enviado continuamente

            while (routesAvailable || !routesInExe.isEmpty()) // || !routesInExe.isEmpty() IMP trocar para pagamentos
            {
                sleep(EnvSimulator.ACQUISITION_RATE);
                if(routesToExe.isEmpty() && !fimRotasNotificado) // && routesInExe.isEmpty()
                {
                    System.out.println("Distribuicao de rotas terminadas");
                    routesAvailable = false;
                    fimRotasNotificado = true;
                }
            }
            BankService bs = BankService.createService("Encerrar");
            write(bs);
            socketCli.close();
            System.out.println("Saldo Company: " + account.getSaldo());
            AlphaBank.encerrarConta(account.getLogin());
            ec.join();
            System.out.println("MobilityCompany encerrada...");
            }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public String getAccountLogin() {
        return account.getLogin();
    }

    public boolean isCarsRepportEmpty() {
        return carsRepport.isEmpty();
    }

    public static synchronized void addRepport(DrivingData _repport)
    {
        carsRepport.add(_repport);
    }

    public DrivingData removeRepport() {
        return carsRepport.remove(0);
    }

    public static boolean isRoutesInExeEmpty() {
        return routesInExe.isEmpty();
    }

    
    /**
     * Libera uma rota para o cliente que a solicitou. Para isso, remove de routesToExe e adiciona em routesInExe
     * @return route RouteN - Rota do topo da ArrayList de rotas
     */
    public static synchronized RouteN liberarRota()
    {
        if(!routesAvailable) // entrou no liberarRota(), mas acabou durante a espera
        {
            System.out.println("SMC - Sem mais rotas para liberar.");
            RouteN route = new RouteN("-1", "00000");
            return route;
        }
        RouteN route = routesToExe.remove(0);
        routesInExe.add(route);
        System.out.println("SMC - Liberando rota:\n" + route.getRouteID());
        return route;
    }

    public static synchronized void arquivarRota(String _routeID)
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

    public static boolean areRoutesAvailable() {
        return routesAvailable;
    }

    public static synchronized boolean estaNoSUMO(String _idCar, SumoTraciConnection _sumo)
	{
        try
        {
            SumoStringList lista;
            lista = (SumoStringList) _sumo.do_job_get(Vehicle.getIDList()); // TODO IllegalStateException
            return lista.contains(_idCar);
        } catch (Exception e) {
            // e.printStackTrace();
            return false;
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

    private void write(BankService _bankService) throws Exception
	{
		String jsMsg = JSONconverter.bankServiceToString(_bankService);
		byte[] msgEncrypt = Cryptography.encrypt(jsMsg);
		saidaCli.writeInt(msgEncrypt.length);
		saidaCli.write(msgEncrypt);
	}
}