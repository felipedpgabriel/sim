package io.sim.bank;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Classe para criar os canais de comunicacao Thread com cada cliente de AlphaBank.
 */
public class BankChannelCreator extends Thread
{
    private ServerSocket serverSocket;
    private int numAccounts;

    /**
     * Construtor da Classe BankChannelCreator.
     * @param _serverSocket ServerSocket - Socket para conexao.
     * @param _numAccounts int - Numero de contas para criar BankChannel's.
     */
    public BankChannelCreator(ServerSocket _serverSocket, int _numAccounts)
    {
        this.serverSocket = _serverSocket;
        this.numAccounts = _numAccounts;
    }

    @Override
    public void run()
    {
        for(int i = 0; i < numAccounts; i++)
        {
            try
            {
                // Aguarda a conexao de uma nova conta
                Socket socket = serverSocket.accept(); 

                // Cria um novo canal de comunicacao com o cliente e inicia a thread
                BankChannel channel = new BankChannel("BC" + (i + 1), socket);
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
