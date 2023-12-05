package io.sim;

import java.io.DataOutputStream;
import java.util.concurrent.Semaphore;

import io.sim.bank.Account;
import io.sim.bank.AlphaBank;
import io.sim.bank.BankService;
import io.sim.driver.Car;
import io.sim.messages.Cryptography;
import io.sim.messages.JSONconverter;
import io.sim.repport.ExcelRepport;
import io.sim.simulation.EnvSimulator;
    
import java.net.Socket;

/**
 * Esta classe representa uma estacao de combustivel que fornece servicos de abastecimento de combustivel para carros.
 * A estacao de combustivel interage com um banco para transacoes financeiras e controla o acesso as bombas de combustivel.
 */
public class FuelStation extends Thread
{
    // Atributos de cliente
    private String stationHost;
	private int bankPort;
    private DataOutputStream saida;
    // Atributos da classe
    private Account account;
    private int numBombas;
    private static long fuelTime;
    private static Semaphore semaphore;
    private boolean stationOn;
    // Escalonamento
    private long initRunTime;
    private long endRunTime;
    private long birthTime;

    /**
     * Construtor da classe FuelStation.
     *
     * @param _stationHost String - Host do banco de dados da estacao de combustivel.
     * @param _bankPort Porta de conexao com o banco de dados.
     * @param _numBombas Número de bombas de combustivel disponiveis na estacao.
     * @param _fuelTime Tempo necessário para abastecimento de combustivel em segundos.
     */
    public FuelStation(String _stationHost, int _bankPort, int _numBombas, long _fuelTime)
    {
        super("FuelStation");
        this.birthTime = System.nanoTime();
        this.stationHost = _stationHost;
        this.bankPort = _bankPort;
        this.account = new Account(0.00, "FuelStation", "fs123");
        this.numBombas = _numBombas;
        fuelTime = _fuelTime;
        semaphore = new Semaphore(this.numBombas);
        stationOn = true;
    }
    
    /**
     * Método que representa a execucao da thread da estacao de combustivel.
     * Inicializa a conexao com o banco de dados, aguarda solicitacoes de abastecimento
     * e encerra a estacao de combustivel quando necessário.
     */
    @Override
    public void run()
    {
        this.initRunTime = System.nanoTime();
        try
        {
            System.out.println("Iniciando FuelStation");
            Socket socket = new Socket(stationHost, bankPort);
            saida = new DataOutputStream(socket.getOutputStream());
            while(stationOn)
            {
                sleep(EnvSimulator.ACQUISITION_RATE);
            }
            BankService bs = BankService.createService("Encerrar");
            write(bs);
            socket.close();
            System.out.println("FuelStation encerrada...");
            System.out.println("Saldo Station: " + account.getSaldo());
            AlphaBank.encerrarConta(account.getLogin());
            this.endRunTime = System.nanoTime();
			ExcelRepport.updateSSScheduling("FuelStation", this.initRunTime, this.endRunTime, this.birthTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Obtém o login da conta associada à estacao de combustivel.
     * @return O login da conta.
     */
    public String getAccountLogin() {
        return account.getLogin();
    }

    /**
     * Método que representa o processo de abastecimento de um carro na estacao de combustivel.
     * @param _car O carro que está sendo abastecido.
     * @param _fuelQtd A quantidade de combustivel a ser abastecida.
     * @return true se o abastecimento foi bem-sucedido, false caso contrário.
     * @throws InterruptedException Se a thread for interrompida enquanto espera para adquirir o semáforo.
     */
    public static boolean fuel(Car _car, double _fuelQtd) throws InterruptedException
    {
        semaphore.acquire();
        System.out.println(_car.getCarID() + " abastecendo.");
        sleep(fuelTime * 1000);
        _car.setFuelTank( _car.getFuelTank() + _fuelQtd);
        System.out.println(_car.getCarID() + " abastecido: " + _car.getFuelTank());
        semaphore.release();
        return true;
    }

    /**
     * Define o estado da estacao de combustivel (ligada ou desligada).
     * @param _on true para ligar a estacao, false para desligá-la.
     */
    public void setStationOn(boolean _on)
    {
        this.stationOn = _on;
    }

    /**
     * Método privado para escrever uma transacao bancária no fluxo de saida.
     * @param _bankService O servico bancário a ser enviado ao banco de dados.
     * @throws Exception Se houver um erro ao escrever no fluxo de saida.
     */
    private void write(BankService _bankService) throws Exception
	{
		String jsMsg = JSONconverter.bankServiceToString(_bankService);
		byte[] msgEncrypt = Cryptography.encrypt(jsMsg);
		saida.writeInt(msgEncrypt.length);
		saida.write(msgEncrypt);
	}
}
