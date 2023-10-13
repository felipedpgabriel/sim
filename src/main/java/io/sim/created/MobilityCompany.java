package io.sim.created;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import io.sim.DrivingData;

public class MobilityCompany extends Thread {
    // atributos de servidor
    private ServerSocket serverSocket;
    // atributos de sincronizacao
    private Object oWatch = new Object();
    private static boolean liberado = true;
    // cliente AlphaBank
    // atributos da classe
    private static ArrayList<RouteN> routesToExe;
    private static ArrayList<RouteN> routesInExe;
    private static ArrayList<RouteN> routesExecuted;
    // private static Account account;
    private static final double RUN_PRICE = 3.25;
    private static int numDrivers;
    private static boolean routesAvailable = true;
    private static boolean allDriversCreated = false;

    public MobilityCompany(ServerSocket serverSocket, ArrayList<RouteN> routes, int _numDrivers)
    {
        // BotPayment payment = new BotPayment(RUN_PRICE);
        // Adicionar as rotas em routesToExe a partir de um arquivo
        this.serverSocket = serverSocket;
        numDrivers = _numDrivers;
        routesToExe = routes;
    }

    @Override
    public void run()
    {
        try
        {
            System.out.println("MobilityCompany iniciada...");

            while (routesAvailable) // IMP tentar trocar para 
            {
                if(routesToExe.isEmpty() && routesInExe.isEmpty())
                {
                    routesAvailable = false;
                }
                if(!allDriversCreated) // delegar essa funcao para outra classe
                {
                    for(int i=0; i<numDrivers;i++)
                    {
                        // conecta os clientes -> IMP mudar para ser feito paralelamente (ou n)
                        Socket socket = serverSocket.accept();
                        System.out.println("Car conectado");

                        Thread mc = new Thread(() -> 
                        { // lança uma thread para comunicacao -> IMP criar uma classe separada para melhor organizacao
                            try
                            {
                                // variaveis de entrada e saida do servidor
                                ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
                                ObjectOutputStream saida = new ObjectOutputStream(socket.getOutputStream());

                                String mensagem = "";
                                while(!mensagem.equals("encerrado")) // loop do sistema
                                {
                                    DrivingData objIn = (DrivingData) entrada.readObject();
                                    // verifica distancia para pagamento
                                    mensagem = objIn.getCarState(); // lê solicitacao do cliente
                                    if (mensagem.equals("aguardando"))
                                    {
                                        synchronized (oWatch)
                                        {
                                            RouteN resposta = liberarRota();
                                            // if (isLiberado())
                                            // {
                                            //     // liberado = false;
                                            //     System.out.println("Liberando rota...");
                                            //     resposta = liberarRotas();
                                            // }
                                            // else
                                            // {
                                            //     System.out.println("Ocupado, aguarde a rota.");
                                            //     resposta = "ocupado";
                                            // }
                                            System.out.println("Liberando rota:\n" + resposta);
                                            saida.writeObject(resposta);
                                        }
                                    }
                                    else if(mensagem.equals("finalizado"))
                                    {
                                        String routeID = objIn.getRouteIDSUMO();
                                        System.out.println("Rota " + routeID + " finalizada.");
                                        this.arquivarRota(routeID);
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
                        });
                        mc.start();
                    }
                    allDriversCreated = true;
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        System.out.println("MobilityCompany encerrada...");
    }
    
    // /**Define se o trecho de acesso sincronizadao esta liberado
    //  * @return liberado - boolean
    //  */
    // private boolean isLiberado()
    // {
    //     synchronized (oWatch)
    //     {
    //         return liberado;
    //     }
    // }

    /**Libera uma rota para o cliente que a solicitou. Para isso, remove de routesToExe e adiciona em routesInExe
     * @return route RouteN - Rota do topo da ArrayList de rotas
     */
    private RouteN liberarRota()
    {
        synchronized (oWatch)
        {
            RouteN route = routesToExe.remove(0);
            routesInExe.add(route); // mudar para routesInExe.add(car.getID(),route) ou route.getID()
            return route;
        }
    }

    private void arquivarRota(String _routeID)
    {
        synchronized (oWatch)
        {
            for(int i=0;i<routesInExe.size();i++)
            {
                if(routesInExe.get(i).getRouteID().equals(_routeID))
                {
                    routesInExe.add(routesInExe.remove(i));
                    break;
                }
            }
        }
    }

    public static boolean areRoutesAvailable() {
        return routesAvailable;
    }


    // public static ArrayList<RouteN> getRoutesToExe() {
    //     return routesToExe;
    // }

    // public static ArrayList<RouteN> getRoutesInExe() {
    //     return routesInExe;
    // }

}