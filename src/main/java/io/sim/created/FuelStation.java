package io.sim.created;

import java.io.DataOutputStream;
import java.util.concurrent.Semaphore;

import io.sim.Car;
import io.sim.EnvSimulator;
import io.sim.created.bank.AlphaBank;
import io.sim.created.messages.Cryptography;
import io.sim.created.messages.JSONConverter;

import java.net.Socket;

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

    public FuelStation(String _stationHost, int _bankPort, int _numBombas, long _fuelTime)
    {
        super("FuelStation");
        this.stationHost = _stationHost;
        this.bankPort = _bankPort;
        this.account = new Account(0.00, "FuelStation", "fs123");
        this.numBombas = _numBombas;
        fuelTime = _fuelTime;
        semaphore = new Semaphore(this.numBombas);
        stationOn = true;
    }
    
    @Override
    public void run()
    {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getAccountLogin() {
        return account.getLogin();
    }

    public static boolean fuel(Car _car, double _fuelQtd) throws InterruptedException
    {
        semaphore.acquire();
        System.out.println(_car.getIdAuto() + " abastecendo.");
        sleep(fuelTime * 1000);
        _car.setFuelTank( _car.getFuelTank() + _fuelQtd);
        System.out.println(_car.getIdAuto() + " abastecido: " + _car.getFuelTank());
        semaphore.release();
        return true;
    }

    public void setStationOn(boolean _on)
    {
        this.stationOn = _on;
    }

    private void write(BankService _bankService) throws Exception
	{
		String jsMsg = JSONConverter.bankServiceToString(_bankService);
		byte[] msgEncrypt = Cryptography.encrypt(jsMsg);
		saida.writeInt(msgEncrypt.length);
		saida.write(msgEncrypt);
	}
}
