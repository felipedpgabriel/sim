package io.sim.created.bank;

import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;

import io.sim.EnvSimulator;
import io.sim.created.Account;
import io.sim.created.BankService;
import io.sim.created.BotPayment;
import io.sim.created.repport.ExcelBank;

/**Classe do Banco que contem as contas e gerencia as transacoes financeiras.
 * 
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

    /**Contrutor da classe AlphaBank.
     * @param serverSocket ServerSocket - Socket para conexao 
     * @param numAccounts int - Numero de contas para criar
     */
    public AlphaBank(ServerSocket serverSocket, int numAccounts)
    {
        this.serverSocket = serverSocket;
        this.numAccounts = numAccounts;
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

            // Cria os canais de comunicação Thread com cada cliente de AlphaBank
            BankChannelCreator bcc = new BankChannelCreator(serverSocket, numAccounts);
            bcc.start();
            bcc.join();
            ExcelBank eb = new ExcelBank(this);
            eb.start();

            // Aguarda a desconexoes das contas
            while(!accounts.isEmpty() || !conectionsInit)
            {
                sleep(EnvSimulator.ACQUISITION_RATE);
            }
            System.out.println("Contas encerradas");
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        System.out.println("AlphaBank encerrado...");
    }

    public boolean isBankServicesEmpty() {
        return bankServices.isEmpty();
    }

    public BankService removeServices() {
        return bankServices.remove(0);
    }

    /**Adiciona as contas na lista.
     * @param _account Account - Conta para adicionar.
     */
    public static synchronized void addAccount(Account _account)
    {
        accounts.add(_account);
    }

    /**Busca uma conta na lista.
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

    /**Adiciona um BankService na lista (para relatorio)
     * @param _bankService BankService - Servico com transacao a ser registrada.
     */
    public static synchronized void addBankService(BankService _bankService)
    {
        bankServices.add(_bankService);
    }

    /**Realiza uma operacao de transferencia: tira um valor de uma conta e adiciona em outra.
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
            // System.out.println("R$ " + _bankService.getValor() + " de " + origem.getLogin() + " para " + destino.getLogin());
            System.out.println(origem.getLogin() + ": R$" + origem.getSaldo() + " | " + destino.getLogin() + ": R$" + destino.getSaldo());
        }
        else
        {
            System.out.println("Senha de acesso incorreta!");
        }
    }

    /**Remove a conta da lista, para indicar que uma instancia Cliente foi encerrada.
     * @param _login String - Login da conta a ser encerrada.
     */
    public static synchronized void encerrarConta(String _login)
    {
        Account account = searchAccount(_login);
        accounts.remove(account);
        System.out.println("Encerrando conta de " + _login);
    }

    /**Muda o atributo conectionsInit, para a logica de aguardar o encerramento das contas.
     * @param _conectionsInit boolean 
     */
    public static void setConectionsInit(boolean _conectionsInit)
    {
        AlphaBank.conectionsInit = _conectionsInit;
    }

    /**Cria um bote de pagamento para as Threads da MobilityCompany.
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

}
