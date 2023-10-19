package io.sim.created;

import java.net.ServerSocket;
import java.util.ArrayList;

public class AlphaBank extends Thread
{
    // Atributos de servidor
    private ServerSocket serverSocket;
    private int numAccounts;
    // Atributos de sincronizacao
    private static Thread oWatch;
    private static ArrayList<Transaction> transactions;

    @Override
    public void run()
    {
        System.out.println("AlphaBank iniciado...");

        BankChannelCreator bc = new BankChannelCreator(serverSocket, numAccounts);
        bc.start();

        // IMP# while() para manter classe ativa.

        System.out.println("MobilityCompany encerrada...");
    }
}
