package io.sim.created;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.DataInputStream;
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

            String mensagem = "";
            while(!mensagem.equals("encerrado")) // loop do sistema
            {
                DrivingData ddIn = (DrivingData) stringToDrivingData(entrada.readUTF());
                // verifica distancia para pagamento
                mensagem = ddIn.getCarState(); // lê solicitacao do cliente
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
                    String routeID = ddIn.getRouteIDSUMO();
                    System.out.println("CC - Rota " + routeID + " finalizada.");
                    MobilityCompany.arquivarRota(routeID);
                    System.out.println("Rotas para executar: " + MobilityCompany.getRoutesToExeSize() +"\nRotas em execucao: " 
                    + MobilityCompany.getRoutesInExeSize() + "\nRotas executadas: "+ MobilityCompany.getRoutesExecutedSize());
                    // saida.writeUTF("-1");
                    System.out.println("Aguardando mensagem...");
                }
                else if(mensagem.equals("rodando"))
                {
                    System.out.println(ddIn);
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

    private String routeNtoString(RouteN _route)
    {
        String convert;
        convert = _route.getRouteID() + "," + _route.getEdges();
        return convert;
    }

    private DrivingData stringToDrivingData(String _string)
	{
		String[] aux = _string.split(",");

		DrivingData carRepport = new DrivingData(aux[0], aux[1], aux[2],Long.parseLong(aux[3]), Double.parseDouble(aux[4]), 
        Double.parseDouble(aux[5]),aux[6], aux[7], Double.parseDouble(aux[8]), Double.parseDouble(aux[9]),
        Double.parseDouble(aux[10]), Double.parseDouble(aux[11]), Integer.parseInt(aux[12]), Double.parseDouble(aux[13]),
        Double.parseDouble(aux[14]), Double.parseDouble(aux[15]),Integer.parseInt(aux[16]), Integer.parseInt(aux[17])); //BOZASSO
		return carRepport;
	}
}
