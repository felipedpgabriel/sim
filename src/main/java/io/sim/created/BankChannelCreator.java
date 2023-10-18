package io.sim.created;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class BankChannelCreator extends Thread
{
    private ServerSocket serverSocket;
    private int numAccounts;

    public BankChannelCreator(ServerSocket serverSocket, int numAccounts)
    {
        this.serverSocket = serverSocket;
        this.numAccounts = numAccounts;
    }

    @Override
    public void run()
    {
        for(int i=0; i<numAccounts;i++)
        {
            try
            {
                System.out.println("BC - Aguardando conexao" + (i+1));
                Socket socket = serverSocket.accept();
                System.out.println("Account conectada");

                BankChannel channel = new BankChannel(socket);
                channel.start();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
        System.out.println("BC - Todas as contas criadas.");
    }
}
