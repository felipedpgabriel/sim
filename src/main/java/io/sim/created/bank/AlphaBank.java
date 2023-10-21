package io.sim.created.bank;

import java.net.ServerSocket;
import java.util.ArrayList;

import io.sim.EnvSimulator;
import io.sim.created.Transaction;
import io.sim.created.Account;

public class AlphaBank extends Thread
{
    // Atributos de servidor
    private ServerSocket serverSocket;
    private int numAccounts;
    // Atributos de sincronizacao
    private static Thread oWatch;
    // Atributos da conta
    private static ArrayList<Account> accounts;
    private static ArrayList<Transaction> transactions;
    private static boolean conectionsInit;

    public AlphaBank(ServerSocket serverSocket, int numAccounts)
    {
        this.serverSocket = serverSocket;
        this.numAccounts = numAccounts;
        oWatch = new Thread();
        accounts = new ArrayList<Account>();
        transactions = new ArrayList<Transaction>();
        conectionsInit = false;
    }

    @Override
    public void run()
    {
        try
        {
            System.out.println("AlphaBank iniciado...");

            BankChannelCreator bc = new BankChannelCreator(serverSocket, numAccounts);
            bc.start();

            while(!accounts.isEmpty() || !conectionsInit)
            {
                sleep(EnvSimulator.ACQUISITION_RATE);
            }
            System.out.println("Contas encerradas");
        }
        catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }

        System.out.println("AlphaBank encerrado...");
    }

    public static void addAccount(Account _account)
    {
        synchronized(oWatch)
        {
            accounts.add(_account);
        }
    }

    private static Account searchAccount(String _login)
    {
        for(int i=0;i<accounts.size();i++)
        {
            Account account = accounts.get(i);
            if(account.getLogin().equals(_login))
            {
                return account;
            }
        }
        return null;
    }

    public static void addTransaction(Transaction _transaction)
    {
        synchronized(oWatch)
        {
            transactions.add(_transaction);
        }
    }

    public static void transfer(Transaction _transaction)
    {
        Account origem = searchAccount(_transaction.getOrigem());
        if(origem.getSenha().equals(_transaction.getSenha()))
        {
            Account destino = searchAccount(_transaction.getDestino());
            origem.pay(_transaction.getValor());
            destino.recieve(_transaction.getValor());
            _transaction.setTimeStamp(System.nanoTime());
            System.out.println("R$ " + _transaction.getValor() + " de " + origem.getLogin() + " para " + destino.getLogin());
            System.out.println(origem.getLogin() + ": R$" + origem.getSaldo() + "\n" + destino.getLogin() + ": R$" + destino.getSaldo());
        }
        else
        {
            System.out.println("Senha de acesso incorreta!");
        }
    }

    public static void encerrarConta(String _login)
    {
        synchronized(oWatch)
        {
            Account account = searchAccount(_login);
            accounts.remove(account);
            System.out.println("Encerrando conta de " + _login);
        }
    }

    public static double consultarSaldo(String _login, String _senha)
    {
        Account account = searchAccount(_login);
        if(account.getSenha().equals(_senha))
        {
            double saldo = account.getSaldo();
            System.out.println("Saldo de " + _login + ": R$" + saldo);
            return saldo;
        }
        else
        {
            System.out.println("Senha de acesso incorreta!");
            return 0.0;
        }
    }

    public static void setConectionsInit(boolean _conectionsInit)
    {
        AlphaBank.conectionsInit = _conectionsInit;
    }

}
