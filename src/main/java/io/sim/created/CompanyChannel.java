package io.sim.created;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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
            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
            // System.out.println("CC - passou da entrada.");
            DataOutputStream saida = new DataOutputStream(socket.getOutputStream());
            // System.out.println("CC - passou da saida.");

            String mensagem = "";
            while(!mensagem.equals("encerrado")) // loop do sistema
            {
                DrivingData objIn = (DrivingData) entrada.readObject();
                // verifica distancia para pagamento
                mensagem = objIn.getCarState(); // lê solicitacao do cliente
                // System.out.println("CC ouviu " + mensagem);
                if (mensagem.equals("aguardando"))
                {
                    if(!MobilityCompany.areRoutesAvailable()) // routesToExe.isEmpty()
                    {
                        System.out.println("CC - Sem mais rotas para liberar.");
                        RouteN route = new RouteN("-1", "00000");
                        saida.writeUTF(routeNtoString(route));
                        break;
                    }
                    if(MobilityCompany.areRoutesAvailable())
                    {
                        synchronized (oWatch)
                        {
                            RouteN resposta = MobilityCompany.liberarRota();
                            saida.writeUTF(routeNtoString(resposta));
                        }
                    }
                }
                else if(mensagem.equals("finalizado"))
                {
                    String routeID = objIn.getRouteIDSUMO();
                    System.out.println("CC - Rota " + routeID + " finalizada.");
                    MobilityCompany.arquivarRota(routeID);
                    System.out.println("Rotas para executar: " + MobilityCompany.getRoutesToExeSize() +"\nRotas em execucao: " 
                    + MobilityCompany.getRoutesInExeSize() + "\nRotas executadas: "+ MobilityCompany.getRoutesExecutedSize());
                    // saida.writeUTF("-1");
                    System.out.println("Aguardando mensagem...");
                }
                else if(mensagem.equals("rodando"))
                {
                    // a principio, nao faz nada
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
        catch (IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    private String routeNtoString(RouteN _route)
    {
        String convert;
        convert = _route.getRouteID() + "," + _route.getEdges();
        return convert;
    }
}
