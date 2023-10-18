package io.sim.created;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class CompanyChannelCreator extends Thread
{
    private ServerSocket serverSocket;
    private int numDrivers;

    public CompanyChannelCreator(ServerSocket _serverSocket, int _numDrivers)
    {
        this.serverSocket = _serverSocket;
        this.numDrivers = _numDrivers;
    }

    @Override
    public void run()
    {
        for(int i=0; i<numDrivers;i++)
        {
            try
            {
                System.out.println("CC - Aguardando conexao" + (i+1));
                Socket socket = serverSocket.accept();
                System.out.println("Car conectado");

                CompanyChannel channel = new CompanyChannel(socket);
                channel.start();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
        System.out.println("CC - Todos os carros criados.");
    }
    
}
