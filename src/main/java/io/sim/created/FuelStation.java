package io.sim.created;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import io.sim.Car;
import io.sim.EnvSimulator;
import io.sim.created.bank.AlphaBank;
import java.net.Socket;

public class FuelStation extends Thread
{
    // Atributos de cliente
    private String stationHost;
	private int bankPort;
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
            DataOutputStream saida = new DataOutputStream(socket.getOutputStream());
            while(stationOn)
            {
                sleep(EnvSimulator.ACQUISITION_RATE);
            }
            BankService bs = BankService.createService("Encerrar");
            saida.writeUTF(JSONConverter.bankServiceToString(bs));
            socket.close();
            System.out.println("FuelStation encerrada...");
            System.out.println("Saldo Station: " + account.getSaldo());
            AlphaBank.encerrarConta(account.getLogin());
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
}
