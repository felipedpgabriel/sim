package io.sim.created;

import java.net.ServerSocket;

public class AlphaBank extends Thread
{
    // Atributos de servidor
    private ServerSocket serverSocket;
    private int numAccounts;
    // Atributos de sincronizacao
    private static Thread oWatch = new Thread();

    @Override
    public void run()
    {
        System.out.println("AlphaBank iniciado...");

        BankChannelCreator bc = new BankChannelCreator(serverSocket, numAccounts);
        bc.start();
    }
}
