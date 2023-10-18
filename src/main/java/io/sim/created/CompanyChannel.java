package io.sim.created;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import io.sim.DrivingData;

public class CompanyChannel extends Thread
{
    private static Object oWatch = new Object();
    private Socket socket;
    
    public CompanyChannel(Socket _socket) {
        this.socket = _socket;
    }

    public void run()
    {
        try
        {
            // variaveis de entrada e saida do servidor
            // System.out.println("CC - entrou no try.");
            DataInputStream entrada = new DataInputStream(socket.getInputStream());
            // System.out.println("CC - passou da entrada.");
            DataOutputStream saida = new DataOutputStream(socket.getOutputStream());
            // System.out.println("CC - passou da saida.");
            // JSONObject jsonOut = new JSONObject();

            String mensagem = "";
            while(!mensagem.equals("encerrado")) // loop do sistema
            {
                DrivingData ddIn = (DrivingData) JSONConverter.stringToDrivingData(entrada.readUTF());
                // verifica distancia para pagamento
                mensagem = ddIn.getCarState(); // lê solicitacao do cliente
                // System.out.println("CC ouviu " + mensagem);
                if (mensagem.equals("aguardando"))
                {
                    if(!MobilityCompany.areRoutesAvailable()) // routesToExe.isEmpty()
                    {
                        System.out.println("CC - Sem mais rotas para liberar.");
                        RouteN route = new RouteN("-1", "00000");
                        saida.writeUTF(JSONConverter.routeNtoString(route));
                        break;
                    }
                    if(MobilityCompany.areRoutesAvailable())
                    {
                        synchronized (oWatch)
                        {
                            RouteN resposta = MobilityCompany.liberarRota();
                            saida.writeUTF(JSONConverter.routeNtoString(resposta));
                        }
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
                    // adicionar rotas em uma lista para o relatorio
                }
                else if (mensagem.equals("encerrado"))
                {
                    break;
                }
            }

            System.out.println("Encerrando canal.");
            entrada.close();
            saida.close();
            socket.close();
            // serverSocket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
