package io.sim.created.company;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import io.sim.DrivingData;
import io.sim.EnvSimulator;
import io.sim.created.Account;
import io.sim.created.RouteN;
import io.sim.created.bank.AlphaBank;
import io.sim.created.messages.Cryptography;
import io.sim.created.messages.JSONConverter;

public class CompanyChannel extends Thread
{
    private Socket socketServ;
    private DataInputStream entradaServ;
    private DataOutputStream saidaServ;
    private Socket socketCli;
    private boolean rotaFinalizada;

    // atributos da classe
    private Account account;
    
    public CompanyChannel(Socket _socketCli, Socket _socketServ, Account _account)
    {
        this.socketCli = _socketCli;
        this.socketServ = _socketServ;
        this.account = _account;
        rotaFinalizada = false;
    }

    public void run()
    {
        try
        {
            entradaServ = new DataInputStream(socketServ.getInputStream());
            saidaServ = new DataOutputStream(socketServ.getOutputStream());
            // DataInputStream entradaCli = new DataInputStream(socketCli.getInputStream());
            // DataOutputStream saidaCli = new DataOutputStream(socketCli.getOutputStream());

            String mensagem = "";
            double previusDistance = 0;
            while(!mensagem.equals("encerrado")) // loop do sistema
            {
                DrivingData ddIn = (DrivingData) read();
                // verifica distancia para pagamento
                if(payableDistanceReached(previusDistance, ddIn.getDistance()))
                {
                    // TODO verificar se o carro existe antes de prosseguir, senão fechar
                    previusDistance = ddIn.getDistance();
                    AlphaBank.payment(socketCli, account.getLogin(), account.getSenha(), ddIn.getDriverLogin(),
                    EnvSimulator.RUN_PRICE);
                }
                mensagem = ddIn.getCarState(); // lê solicitacao do cliente
                // System.out.println("CC ouviu " + mensagem);
                if (mensagem.equals("aguardando"))
                {
                    previusDistance = 0;
                    if(!MobilityCompany.areRoutesAvailable()) // routesToExe.isEmpty()
                    {
                        System.out.println("CC - Sem mais rotas para liberar.");
                        RouteN route = new RouteN("-1", "00000");
                        write(route);
                        break;
                    }
                    if(MobilityCompany.areRoutesAvailable())
                    {
                        RouteN resposta = MobilityCompany.liberarRota();
                        write(resposta);
                        rotaFinalizada = false;
                    }
                }
                else if(mensagem.equals("finalizado"))
                {
                    String routeID = ddIn.getRouteIDSUMO();
                    MobilityCompany.arquivarRota(routeID);
                    System.out.println("Rotas para executar: " + MobilityCompany.getRoutesToExeSize() +"\nRotas em execucao: " 
                    + MobilityCompany.getRoutesInExeSize() + "\nRotas executadas: "+ MobilityCompany.getRoutesExecutedSize());
                    // System.out.println("Aguardando mensagem...");
                    rotaFinalizada = true;
                }
                else if(mensagem.equals("rodando") || mensagem.equals("abastecendo"))
                {
                    MobilityCompany.addRepport(ddIn);
                }
                else if (mensagem.equals("encerrado"))
                {
                    if(!rotaFinalizada)
                    {
                        String routeID = ddIn.getRouteIDSUMO();
                        MobilityCompany.arquivarRota(routeID);
                        System.out.println("Rotas para executar: " + MobilityCompany.getRoutesToExeSize() +"\nRotas em execucao: " 
                        + MobilityCompany.getRoutesInExeSize() + "\nRotas executadas: "+ MobilityCompany.getRoutesExecutedSize());
                        rotaFinalizada = true;
                    }
                    break;
                }
            }

            System.out.println("Encerrando canal CC.");
            // entradaCli.close();
            // saidaCli.close();
            // socketCli.close();
            entradaServ.close();
            saidaServ.close();
            socketServ.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private boolean payableDistanceReached(double _previusDistance, double _currentDistance)
    {
        // System.out.println(_idAuto + " distancia: " + _currentDistance);
        return (_currentDistance >= (_previusDistance + EnvSimulator.PAYABLE_DISTANCE));
    }

    private void write(RouteN _route) throws Exception
	{
		String jsMsg = JSONConverter.routeNtoString(_route);
		byte[] msgEncrypt = Cryptography.encrypt(jsMsg);
		saidaServ.writeInt(msgEncrypt.length);
		saidaServ.write(msgEncrypt);
	}

	private DrivingData read() throws Exception
	{
		int numBytes = entradaServ.readInt();
		byte[] msgEncrypt = entradaServ.readNBytes(numBytes);
		String msgDecrypt = Cryptography.decrypt(msgEncrypt);
		return JSONConverter.stringToDrivingData(msgDecrypt);
	}
}
