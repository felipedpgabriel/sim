package io.sim.created.bank;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import io.sim.EnvSimulator;
import io.sim.created.BankService;
import io.sim.created.Account;

public class AlphaBank extends Thread
{
    // Atributos de servidor
    private ServerSocket serverSocket;
    private int port;
    private int numAccounts;
    // Atributos da conta
    private static ArrayList<Account> accounts;
    private static ArrayList<BankService> bankServices;
    private static boolean conectionsInit;
    private HashMap<String,String> cadastro;
    private ArrayList<BankChannel> channels;

    public AlphaBank(int _port, int numAccounts) throws IOException
    {
        this.port = _port;
        accounts = new ArrayList<Account>();
        serverSocket = new ServerSocket(this.port);
        cadastro = new HashMap<String,String>();
        channels = new ArrayList<BankChannel>();
        this.numAccounts = numAccounts;
        bankServices = new ArrayList<BankService>();
        conectionsInit = false;
    }

    @Override
    public void run()
    {
        try
        {
            System.out.println("AlphaBank iniciado...");

            for(int i=0; i<EnvSimulator.NUM_DRIVERS+1;i++)
            {
                System.out.println("BC - Aguardando conexao " + (i+1));
                Socket socket = serverSocket.accept();
                System.out.println("Account conectada");

                BankChannel channel = new BankChannel(socket, this);
                channels.add(channel);
                channel.start();
            }

            for(BankChannel bc: channels)
            {
                bc.join();
            }

            serverSocket.close();
            System.out.println("AlphaBank encerrado...");

        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Account searchAccount(String _login)
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

    public void addAccount(Account _account)
    {
        accounts.add(_account);
        cadastro.put(_account.getLogin(), _account.getSenha());
    }

    public boolean confereConta(String _login, String _senha){
		return cadastro.get(_login).equals(_senha);
	}

    public static void addBankService(BankService _bankService)
    {
        bankServices.add(_bankService);
    }

    // public static void transfer(BankService _bankService)
    // {
    //     Account origem = searchAccount(_bankService.getOrigem());
    //     if(origem.getSenha().equals(_bankService.getSenha()))
    //     {
    //         Account destino = searchAccount(_bankService.getDestino());
    //         origem.pay(_bankService.getValor());
    //         destino.recieve(_bankService.getValor());
    //         Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    //         _bankService.setTimeStamp(timestamp);
    //         System.out.println("R$ " + _bankService.getValor() + " de " + origem.getLogin() + " para " + destino.getLogin());
    //         System.out.println(origem.getLogin() + ": R$" + origem.getSaldo() + "\n" + destino.getLogin() + ": R$" + destino.getSaldo());
    //     }
    //     else
    //     {
    //         System.out.println("Senha de acesso incorreta!");
    //     }
    // }

    // public static void encerrarConta(String _login)
    // {
    //     synchronized(oWatch)
    //     {
    //         Account account = searchAccount(_login);
    //         accounts.remove(account);
    //         System.out.println("Encerrando conta de " + _login);
    //     }
    // }

    // public static double consultarSaldo(String _login, String _senha)
    // {
    //     Account account = searchAccount(_login);
    //     if(account.getSenha().equals(_senha))
    //     {
    //         double saldo = account.getSaldo();
    //         System.out.println("Saldo de " + _login + ": R$" + saldo);
    //         return saldo;
    //     }
    //     else
    //     {
    //         System.out.println("Senha de acesso incorreta!");
    //         return 0.0;
    //     }
    // }

    // public static void setConectionsInit(boolean _conectionsInit)
    // {
    //     AlphaBank.conectionsInit = _conectionsInit;
    // }

    // public static void payment(Socket socket, String _loginOrigem, String _senhaOrigem, String _loginDestino,
    // double _valor)
    // {
    //     synchronized(oWatch)
    //     {
    //         BotPayment bot = new BotPayment(socket, _loginOrigem, _senhaOrigem, _loginDestino, _valor);
    //         bot.start();
    //     }
    // }

}
