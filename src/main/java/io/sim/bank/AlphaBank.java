package io.sim.bank;

import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;

import io.sim.repport.ExcelBank;
import io.sim.simulation.EnvSimulator;

/**
 * Classe do Banco que contem as contas e gerencia as transacoes financeiras.
 */
public class AlphaBank extends Thread
{
    // Atributos de servidor
    private ServerSocket serverSocket;
    private int numAccounts;
    // Atributos da conta
    private static ArrayList<Account> accounts;
    private static ArrayList<BankService> bankServices;
    private static boolean conectionsInit;
    private static boolean accountsEnded;

    /**
     * Contrutor da classe AlphaBank.
     * @param serverSocket ServerSocket - Socket para conexao do servidor.
     * @param numAccounts int - Numero de contas para criar.
     */
    public AlphaBank(ServerSocket serverSocket, int numAccounts)
    {
        this.serverSocket = serverSocket;
        this.numAccounts = numAccounts;
        accounts = new ArrayList<Account>();
        bankServices = new ArrayList<BankService>();
        conectionsInit = false;
        accountsEnded = false;
    }

    @Override
    public void run()
    {
        try
        {
            System.out.println("AlphaBank iniciado...");

            // Cria os canais de comunicação Thread com cada cliente de AlphaBank
            BankChannelCreator bcc = new BankChannelCreator(serverSocket, numAccounts);
            bcc.start();
            bcc.join();

            // Cria a Thread de atualizacao dos relatorios do Excel
            ExcelBank eb = new ExcelBank(this);
            eb.start();

            // Aguarda a desconexoes das contas
            while(!accounts.isEmpty() || !conectionsInit)
            {
                sleep(EnvSimulator.ACQUISITION_RATE);
                if(accounts.isEmpty())
                {
                    System.out.println("Contas encerradas");
                    accountsEnded = true;
                }
            }

            // Aguarda a Thread do Excel finalizar
            eb.join();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        System.out.println("AlphaBank encerrado...");
    }

    /**
     * Verifica se a lista bankServices esta vazia.
     * @return boolean
     */
    public boolean isBankServicesEmpty()
    {
        return bankServices.isEmpty();
    }
    
    /**
     * Remove o primeiro item de bankServices.
     * @return BankService - Servico removido.
     */
    public BankService removeServices()
    {
        return bankServices.remove(0);
    }

    /**
     * Adiciona uma conta na lista (synchronized).
     * @param _account Account - Conta para adicionar.
     */
    public static synchronized void addAccount(Account _account)
    {
        accounts.add(_account);
    }

    /**
     * Busca uma conta na lista (synchronized).
     * @param _login String - Login para fazer a busca.
     * @return Account - A conta buscada.
     */
    private static synchronized Account searchAccount(String _login)
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

    /**
     * Adiciona um BankService na lista para relatorio (synchronized).
     * @param _bankService BankService - Servico com transacao a ser registrada.
     */
    public static synchronized void addBankService(BankService _bankService)
    {
        bankServices.add(_bankService);
    }

    /**
     * Realiza uma operacao de transferencia: tira um valor de uma conta e adiciona em outra (synchronized).
     * @param _bankService BankService - Servico com as informacoes para transferencia.
     */
    public static synchronized void transfer(BankService _bankService)
    {
        Account origem = searchAccount(_bankService.getOrigem());
        if(origem.getSenha().equals(_bankService.getSenha()))
        {
            Account destino = searchAccount(_bankService.getDestino());
            origem.pay(_bankService.getValor());
            destino.recieve(_bankService.getValor());
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            _bankService.setTimeStamp(timestamp);
            System.out.println(origem.getLogin() + ": R$" + origem.getSaldo() + " | " + destino.getLogin() + ": R$" + destino.getSaldo());
        }
        else
        {
            System.out.println("Senha de acesso incorreta!");
        }
    }

    /**
     * Remove a conta da lista, para indicar que uma instancia Cliente foi encerrada (synchronized).
     * @param _login String - Login da conta a ser encerrada.
     */
    public static synchronized void encerrarConta(String _login)
    {
        Account account = searchAccount(_login);
        accounts.remove(account);
        System.out.println("Encerrando conta de " + _login);
    }

    /**
     * Cria um bot de pagamento para as Threads da MobilityCompany (synchronized).
     * @param socket Socket - Canal de comunicacao de cliente.
     * @param _loginOrigem String - Login do pagador.
     * @param _senhaOrigem String - Senha da conta do pagador.
     * @param _loginDestino String - Login do recebedor.
     * @param _valor double - Valor para transferencia.
     */
    public static synchronized void payment(Socket socket, String _loginOrigem, String _senhaOrigem, String _loginDestino,
    double _valor)
    {
        BotPayment bot = new BotPayment(socket, _loginOrigem, _senhaOrigem, _loginDestino, _valor);
        bot.start();
    }

    /**
     * Set padrao para o atributo conectionsInit
     * @param _conectionsInit boolean 
     */
    public static void setConectionsInit(boolean _conectionsInit){
        AlphaBank.conectionsInit = _conectionsInit;
    }

    /**
     * Get padrao para o atributo accountsEnded.
     * @return boolean
     */
    public static boolean isAccountsEnded() {
        return accountsEnded;
    }

}
