package io.sim.created;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import io.sim.Car;
import io.sim.EnvSimulator;
import io.sim.created.bank.AlphaBank;

public class Driver extends Thread
{
	// atributos de cliente
	private String driverHost;
	private int servPort;
    // atributos da classe
    private String driverID;
    private Account account;
    private Car car; // private Car car;
    // private static final double FUEL_PRICE = 5.87;
    private long acquisitionRate;
    // private ArrayList<RouteN> routeToExe = new ArrayList<RouteN>();
    private ArrayList<RouteN> routesExecuted;
    private ArrayList<RouteN> routesInExe;
    private boolean initRoute;

    public Driver(String driverHost, int servPort, String driverID, Car car, long acquisitionRate)
    {
        account = new Account(1000, driverID, (driverID + "123")); // 0
        this.driverHost = driverHost;
        this.servPort = servPort;
        this.driverID = driverID;
        this.car = car;
        this.acquisitionRate = acquisitionRate;
        routesExecuted = new ArrayList<RouteN>();
        routesInExe = new ArrayList<RouteN>();
        initRoute = false;
    }

    @Override
    public void run()
    {
        try {
            Socket socket = new Socket(this.driverHost, this.servPort);
            DataInputStream entrada = new DataInputStream(socket.getInputStream());
            DataOutputStream saida = new DataOutputStream(socket.getOutputStream());
            
            System.out.println("Iniciando " + this.driverID);
            Thread c = new Thread(car);
            c.start();
            while(c.isAlive())
            {
                Thread.sleep(acquisitionRate);
                String carState = this.car.getCarRepport().getCarState();
                if(carState.equals("finalizado") || (carState.equals("aguardando") && initRoute)) // esta acontecendo muito rapido
                {
                    System.out.println(this.driverID + " rota "+ this.routesInExe.get(0).getRouteID() +" finalizada");
                    this.routesExecuted.add((this.routesInExe.remove(0)));
                    initRoute = false;
                }
                else if(carState.equals("rodando") && !initRoute)
                {
                    System.out.println(this.driverID + " rota "+ this.car.getRoute().getRouteID() +" iniciada");
                    this.routesInExe.add(this.car.getRoute());
                    initRoute = true; 
                }
                else if(carState.equals("abastecendo"))
                {
                    System.out.println(this.driverID + " parou para abastecer.");
                    double fuelQtd = calcFuelQtd();
                    boolean terminouDeAbastecer = FuelStation.fuel(this.car, fuelQtd);
                    if(terminouDeAbastecer)
                    {
                        this.car.setAbastecendo(false);
                        this.car.setCarSate("rodando");
                    }
                    BotPayment bot = new BotPayment(socket, this.account.getLogin(), this.account.getSenha(), "FuelStation",
                    fuelQtd * EnvSimulator.FUEL_PRICE/1000);
                    bot.start();
                }
                else if(carState.equals("encerrado"))
                {
                    break;
                }
            }
            BankService bs = BankService.createService("Encerrar");
            saida.writeUTF(JSONConverter.bankServiceToString(bs));
            System.out.println("Encerrando " + this.driverID);
            entrada.close(); 
			saida.close();
			socket.close();
            System.out.println("Saldo "+ this.driverID+": "+this.account.getSaldo());
            AlphaBank.encerrarConta(this.account.getLogin());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getDriverID() {
        return driverID;
    }

    private double calcFuelQtd()
    {
        double maxFuel = (this.account.getSaldo()/EnvSimulator.FUEL_PRICE) * 1000;
        System.out.println("Qtd max: " + maxFuel);
        if(maxFuel + this.car.getFuelTank() > EnvSimulator.MAX_FUEL_TANK*1000)
        {
            return ((EnvSimulator.MAX_FUEL_TANK*1000) - this.car.getFuelTank());
        }
        else
        {
            return maxFuel;
        }
    }
}
