package io.sim.created.bank;

import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;

import io.sim.EnvSimulator;
import io.sim.created.BankService;
import io.sim.created.BotPayment;
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
    private static ArrayList<BankService> bankServices;
    private static boolean conectionsInit;

    public AlphaBank(ServerSocket serverSocket, int numAccounts)
    {
        this.serverSocket = serverSocket;
        this.numAccounts = numAccounts;
        oWatch = new Thread();
        accounts = new ArrayList<Account>();
        bankServices = new ArrayList<BankService>();
        conectionsInit = false;
    }

    @Override
    public void run()
    {
        try
        {
            System.out.println("AlphaBank iniciado...");

            BankChannelCreator bcc = new BankChannelCreator(serverSocket, numAccounts);
            bcc.start();

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
        synchronized(oWatch)
        {
            for(int i=0;i<accounts.size();i++)
            {
                Account account = accounts.get(i);
                if(account.getLogin().equals(_login))
                {
                    return account;
                }
            }
        }
        return null;
    }

    public static void addBankService(BankService _bankService)
    {
        synchronized(oWatch)
        {
            bankServices.add(_bankService);
        }
    }

    public static void transfer(BankService _bankService)
    {
        synchronized (oWatch)
        {
            Account origem = searchAccount(_bankService.getOrigem());
            if(origem.getSenha().equals(_bankService.getSenha()))
            {
                Account destino = searchAccount(_bankService.getDestino());
                origem.pay(_bankService.getValor());
                destino.recieve(_bankService.getValor());
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                _bankService.setTimeStamp(timestamp);
                System.out.println("R$ " + _bankService.getValor() + " de " + origem.getLogin() + " para " + destino.getLogin());
                System.out.println(origem.getLogin() + ": R$" + origem.getSaldo() + "\n" + destino.getLogin() + ": R$" + destino.getSaldo());
            }
            else
            {
                System.out.println("Senha de acesso incorreta!");
            }
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
        synchronized(oWatch)
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
    }

    public static void setConectionsInit(boolean _conectionsInit)
    {
        AlphaBank.conectionsInit = _conectionsInit;
    }

    public static void payment(Socket socket, String _loginOrigem, String _senhaOrigem, String _loginDestino,
    double _valor)
    {
        synchronized(oWatch)
        {
            BotPayment bot = new BotPayment(socket, _loginOrigem, _senhaOrigem, _loginDestino, _valor);
            bot.start();
        }
    }

}
