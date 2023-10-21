package io.sim.created.company;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import io.sim.EnvSimulator;
import io.sim.created.Account;

public class CompanyChannelCreator extends Thread
{
    private String companyHost;
	private int bankPort;
    private ServerSocket serverSocket;
    private Account account;

    public CompanyChannelCreator(String _companyHost, int _bankPort,ServerSocket _serverSocket, Account _account)
    {
        this.serverSocket = _serverSocket;
        this.companyHost = _companyHost;
        this.bankPort = _bankPort;
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

                CompanyChannel channel = new CompanyChannel(this.companyHost, this.bankPort, socket, this.account);
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
