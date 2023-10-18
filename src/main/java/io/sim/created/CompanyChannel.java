package io.sim.created;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import org.json.JSONObject;

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

    private String routeNtoString(RouteN _route)
    {
        JSONObject jsonOut = new JSONObject();
        jsonOut.put("RouteID",_route.getRouteID());
        jsonOut.put("Edges",_route.getEdges());
        return jsonOut.toString();
    }

    /**Converte uma string no formato JSON para um objeto do tipo DrivingData
     * @param _string String - no formato JSON
     * @return DrivingData
     */
    private DrivingData stringToDrivingData(String _string)
	{
		JSONObject jsonOut = new JSONObject(_string);
		String jsCarState = jsonOut.getString("CarState");
		String jsAutoID = jsonOut.getString("AutoID");
        String jsDriverID = jsonOut.getString("DriverID");
        long jsTimeStamp = jsonOut.getLong("TimeStamp");
        double jsX_Position = jsonOut.getDouble("X_Position");
        double jsY_Position = jsonOut.getDouble("Y_Position");
        String jsRoadIDSUMO = jsonOut.getString("RoadIDSUMO");
        String jsRouteIDSUMO = jsonOut.getString("RouteIDSUMO");
        double jsSpeed = jsonOut.getDouble("Speed");
        double jsOdometer = jsonOut.getDouble("Odometer");
        double jsFuelConsumption = jsonOut.getDouble("FuelConsumption");
        double jsAverageFuelConsumption = jsonOut.getDouble("AverageFuelConsumption");
        int jsFuelType = jsonOut.getInt("FuelType");
        double jsFuelPrice = jsonOut.getDouble("FuelPrice");
        double jsCo2Emission = jsonOut.getDouble("Co2Emission");
        double jsHCEmission = jsonOut.getDouble("HCEmission");
        int jsPersonCapacity = jsonOut.getInt("PersonCapacity");
        int jsPersonNumber = jsonOut.getInt("PersonNumber");

        DrivingData carRepport = new DrivingData(jsCarState, jsAutoID, jsDriverID, jsTimeStamp, jsX_Position, jsY_Position, jsRoadIDSUMO,
        jsRouteIDSUMO, jsSpeed, jsOdometer, jsFuelConsumption, jsAverageFuelConsumption, jsFuelType, jsFuelPrice, jsCo2Emission, jsHCEmission,
        jsPersonCapacity, jsPersonNumber);

		return carRepport;
	}
}
