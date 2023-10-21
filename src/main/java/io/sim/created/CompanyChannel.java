package io.sim.created;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import io.sim.DrivingData;
import io.sim.EnvSimulator;

public class CompanyChannel extends Thread
{
    private Socket socketServ;
    private String companyHost;
	private int bankPort;

    // atributos da classe
    private Account account;
    
    public CompanyChannel(String _companyHost, int _bankPort, Socket _socketServ, Account _account)
    {
        this.companyHost = _companyHost;
        this.bankPort = _bankPort;
        this.socketServ = _socketServ;
        this.account = _account;
    }

    public void run()
    {
        try
        {
            // variaveis de entrada e saida do servidor
            // System.out.println("CC - entrou no try.");
            DataInputStream entradaServ = new DataInputStream(socketServ.getInputStream());
            // System.out.println("CC - passou da entradaServ.");
            DataOutputStream saidaServ = new DataOutputStream(socketServ.getOutputStream());
            // System.out.println("CC - passou da saidaServ.");

            Socket socketCli = new Socket(this.companyHost, this.bankPort);
			// System.out.println("CC - passou do socketCli.");
            DataInputStream entradaCli = new DataInputStream(socketCli.getInputStream());
			// System.out.println("CC - passou da entradaCli.");
            DataOutputStream saidaCli = new DataOutputStream(socketCli.getOutputStream());
            // System.out.println("CC - passou da saidaCli.");

            String mensagem = "";
            double previusDistance = 0;
            while(!mensagem.equals("encerrado")) // loop do sistema
            {
                DrivingData ddIn = (DrivingData) JSONConverter.stringToDrivingData(entradaServ.readUTF());
                // verifica distancia para pagamento
                if(payableDistanceReached(previusDistance, ddIn.getDistance()))
                {
                    previusDistance = ddIn.getDistance();
                    BotPayment bot = new BotPayment(entradaCli,saidaCli, account.getLogin(), account.getSenha(), ddIn.getDriverLogin(),
                    EnvSimulator.RUN_PRICE);
                    bot.start();
                }
                mensagem = ddIn.getCarState(); // lê solicitacao do cliente
                // System.out.println("CC ouviu " + mensagem);
                if (mensagem.equals("aguardando"))
                {
                    if(!MobilityCompany.areRoutesAvailable()) // routesToExe.isEmpty()
                    {
                        saidaCli.writeUTF(JSONConverter.setJSONservice("Encerrar"));
                        System.out.println("CC - Sem mais rotas para liberar.");
                        RouteN route = new RouteN("-1", "00000");
                        saidaServ.writeUTF(JSONConverter.routeNtoString(route));
                        break;
                    }
                    if(MobilityCompany.areRoutesAvailable())
                    {
                        RouteN resposta = MobilityCompany.liberarRota();
                        saidaServ.writeUTF(JSONConverter.routeNtoString(resposta));
                    }
                }
                else if(mensagem.equals("finalizado"))
                {
                    String routeID = ddIn.getRouteIDSUMO();
                    System.out.println("CC - Rota " + routeID + " finalizada.");
                    MobilityCompany.arquivarRota(routeID);
                    System.out.println("Rotas para executar: " + MobilityCompany.getRoutesToExeSize() +"\nRotas em execucao: " 
                    + MobilityCompany.getRoutesInExeSize() + "\nRotas executadas: "+ MobilityCompany.getRoutesExecutedSize());
                    System.out.println("Aguardando mensagem...");
                }
                else if(mensagem.equals("rodando"))
                {
                    // TODO adicionar rotas em uma lista para o relatorio
                }
                else if (mensagem.equals("encerrado"))
                {
                    break;
                }
            }

            System.out.println("Encerrando canal CC.");
            entradaCli.close();
            saidaCli.close();
            socketCli.close();
            entradaServ.close();
            saidaServ.close();
            socketServ.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private boolean payableDistanceReached(double _previusDistance, double _currentDistance)
    {
        System.out.println("Distancia: " + _currentDistance); // TODO comentar caso der certo
        return (_currentDistance >= (_previusDistance + EnvSimulator.PAYABLE_DISTANCE));
    }
}
