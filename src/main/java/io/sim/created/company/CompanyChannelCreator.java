package io.sim.created.company;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import io.sim.EnvSimulator;
import io.sim.created.Account;

public class CompanyChannelCreator extends Thread
{
    private Socket socketCli;
    private ServerSocket serverSocket;
    private Account account;

    public CompanyChannelCreator(Socket _socketCli,ServerSocket _serverSocket, Account _account)
    {
        this.serverSocket = _serverSocket;
        this.socketCli = _socketCli;
        this.account = _account;
    }

    @Override
    public void run()
    {
        for(int i=0; i<EnvSimulator.NUM_DRIVERS;i++)
        {
            try
            {
                System.out.println("CC - Aguardando conexao " + (i+1));
                Socket socket = serverSocket.accept();
                System.out.println("Car conectado");

                CompanyChannel channel = new CompanyChannel(this.socketCli, socket, this.account);
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
