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
     * @param _companyHost String - Host para a conexao como cliente.
     * @param _bankPort int - Porta para conexao como cliente.
     * @param _serverSocket ServerSocket - Socket para conexao do servidor.
     * @param _routes ArrayList<RouteN> - Rotas para distribuicao.
     * @param _sumo SumoTraciConnection - Objeto sumo.
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
            int edgesSize = routesToExe.get(0).getEdgesList().size();
            System.out.println("MobilityCompany iniciada...");
            
            AlphaBank.setConectionsInit(true);
            Socket socketCli = new Socket(this.companyHost, this.bankPort);
            saidaCli = new DataOutputStream(socketCli.getOutputStream());

            // Cria os canais de comunicação Thread com cada cliente da MobilityCompany
            CompanyChannelCreator ccc = new CompanyChannelCreator(socketCli, serverSocket, account);
            ccc.start();
            ccc.join();

            // Cria a Thread de atualizacao dos relatorios do Excel
            ExcelCompany ec = new ExcelCompany(this, edgesSize);
            ec.start();

            boolean fimRotasNotificado = false; // evita que a mensagem "Rotas terminadas" seja enviado continuamente

            while (!isServiceEnded())
            {
                sleep(EnvSimulator.ACQUISITION_RATE);
                if(routesToExe.isEmpty() && !fimRotasNotificado)
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

    public static boolean isServiceEnded()
    {
        return !routesAvailable && routesInExe.isEmpty();
    }

    /**
     * Retorna se o atributo carsRepport esta vazio.
     * @return boolean.
     */
    public boolean isCarsRepportEmpty()
    {
        return carsRepport.isEmpty();
    }

    /**
     * Adiciona um DrivingData na lista carsRepport.
     * @param _repport DrivingData - Dado dos carros para registro.
     */
    public static synchronized void addRepport(DrivingData _repport)
    {
        carsRepport.add(_repport);
    }

    /**
     * Remove o primeiro elemento da lista carsRepport.
     * @return DrivingData - Primeiro elemento da lista.
     */
    public DrivingData removeRepport() {
        return carsRepport.remove(0);
    }

    /**
     * Retorna se a lista de rotas em execucao esta vazia.
     * @return Primeiro elemento da lista de rotas em execucao.
     */
    public static boolean isRoutesInExeEmpty() {
        return routesInExe.isEmpty();
    }

    /**
     * ALTERADO PARA AV2
     * Libera uma rota para o cliente que a solicitou. Para isso, remove de routesToExe e adiciona em routesInExe (synchronized).
     * @return route RouteN - Rota do topo da ArrayList de rotas.
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

    /**
     * Transfere a rota em execucao terminada na lista de rotas executadas (synchronized).
     * @param _routeID String - ID da rota executada.
     */
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

    /**
     * Retorna se um carro esta na lista de veiculos do sumo (synchronized).
     * @param _idCar String - ID do carro para analise.
     * @param _sumo SumoTraciConnection - Objeto sumo.
     * @return boolean.
     */
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

    /**
     * Converte para JSON e criptografa a mensagem para envio.
     * @param _bankService BankService - Objeto representando a transacao bancaria.
     * @throws Exception - Excecao em caso de erro na escrita ou criptografia.
     */
    private void write(BankService _bankService) throws Exception
	{
		String jsMsg = JSONconverter.bankServiceToString(_bankService);
		byte[] msgEncrypt = Cryptography.encrypt(jsMsg);
		saidaCli.writeInt(msgEncrypt.length);
		saidaCli.write(msgEncrypt);
	}

    /**
     * Retorna o numero de rotas para executar.
     * @return int - Numero de rotas para execucao.
     */
    public static int getRoutesToExeSize()
    {
        return routesToExe.size();
    }

    /**
     * Retorna o numero de rotas em execucao.
     * @return int - Numero de rotas em execucao.
     */
    public static int getRoutesInExeSize()
    {
        return routesInExe.size();
    }

    /**
     * Retorna o numero de rotas executadas.
     * @return int - Numero de rotas executadas.
     */
    public static int getRoutesExecutedSize()
    {
        return routesExecuted.size();
    }

    /**
     * Retorna o login da conta da company.
     * @return String - Login da conta.
     */
    public String getAccountLogin()
    {
        return account.getLogin();
    }

    /**
     * Get booleano para o atributo routesAvailable.
     * @return boolean
     */
    public static boolean areRoutesAvailable() {
        return routesAvailable;
    }
}