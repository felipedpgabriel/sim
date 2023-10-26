package io.sim.company;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import io.sim.bank.Account;
import io.sim.simulation.EnvSimulator;

/**
 * Classe para criar os canais de comunicacao Thread com cada cliente de MobilityCompany.
 */
public class CompanyChannelCreator extends Thread
{
    private Socket socketCli;
    private ServerSocket serverSocket;
    private Account account;

    /**
     * Construtor da Classe CompanyChannelCreator.
     * @param _socketCli Socket - Socket do cliente para comunicacao.
     * @param _serverSocket ServerSocket - Socket do servidor para conexao.
     * @param _account Account - Conta associada a empresa.
     */
    public CompanyChannelCreator(Socket _socketCli, ServerSocket _serverSocket, Account _account)
    {
        this.serverSocket = _serverSocket;
        this.socketCli = _socketCli;
        this.account = _account;
    }

    @Override
    public void run()
    {
        for(int i = 0; i < EnvSimulator.NUM_DRIVERS; i++)
        {
            try
            {
                // Aguarda a conexao de uma nova conta
                System.out.println("CC - Aguardando conexao " + (i + 1)); // TODO retirar futuramente
                Socket socket = serverSocket.accept();
                System.out.println("Carro conectado"); // TODO retirar futuramente

                // Cria um novo canal de comunicacao com o cliente e inicia a Thread
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
