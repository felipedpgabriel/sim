package io.sim.driver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import io.sim.simulation.EnvSimulator;
import io.sim.FuelStation;
import io.sim.bank.Account;
import io.sim.bank.AlphaBank;
import io.sim.bank.BankService;
import io.sim.bank.BotPayment;
import io.sim.company.RouteN;
import io.sim.messages.Cryptography;
import io.sim.messages.JSONconverter;
import io.sim.repport.ExcelRepport;

public class Driver extends Thread
{
	// atributos de cliente
	private String driverHost;
	private int servPort;
    private DataOutputStream saida;
    // atributos da classe
    private String driverID;
    private Account account;
    private Car car; // private Car car;
    private long acquisitionRate;
    // private ArrayList<RouteN> routeToExe = new ArrayList<RouteN>();
    private ArrayList<RouteN> routesExecuted;
    private ArrayList<RouteN> routesInExe;
    private boolean initRoute;
    // Escalonamento
    private long initRunTime;
    private long endRunTime;
    private long birthTime;

    public Driver(String driverHost, int servPort, String driverID, Car car, long acquisitionRate)
    {
        this.birthTime = System.nanoTime();
        account = new Account(41.10/2, driverID, (driverID + "123"));
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
        this.initRunTime = System.nanoTime();
        try {
            Socket socket = new Socket(this.driverHost, this.servPort);
            DataInputStream entrada = new DataInputStream(socket.getInputStream());
            saida = new DataOutputStream(socket.getOutputStream());
            
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
                    System.out.println(this.driverID + " iniciando: rota "+ this.car.getRoute().getRouteID() + " | " + 
                    (this.car.getRoute().getEdgesList().size() + EnvSimulator.FLOW_SIZE) + " edges");
                    this.routesInExe.add(this.car.getRoute());
                    initRoute = true; 
                }
                else if(carState.equals("abastecendo"))
                {
                    System.out.println(this.driverID + " parou para abastecer.");
                    double fuelQtd = calcFuelQtd(); // TODO adicionar condicional para caso saldo seja 0, encerrar 
                    boolean terminouDeAbastecer = FuelStation.fuel(this.car, fuelQtd);
                    if(terminouDeAbastecer)
                    {
                        this.car.setAbastecendo(false);
                        this.car.setcarState("rodando");
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
            write(bs);
            System.out.println("Encerrando " + this.driverID);
            entrada.close(); 
			saida.close();
			socket.close();
            System.out.println("Saldo "+ this.driverID+": "+this.account.getSaldo());
            AlphaBank.encerrarConta(this.account.getLogin());
            this.endRunTime = System.nanoTime();
			ExcelRepport.updateSSScheduling("Driver", this.initRunTime, this.endRunTime, this.birthTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getDriverID() {
        return driverID;
    }

    public String getAccountLogin()
    {
        return this.account.getLogin();
    }

    public String getCarID()
    {
        return this.car.getCarID();
    }

    public double calcFuelQtd() // publico para usar no teste unitario
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

    private void write(BankService _bankService) throws Exception
	{
		String jsMsg = JSONconverter.bankServiceToString(_bankService);
		byte[] msgEncrypt = Cryptography.encrypt(jsMsg);
		saida.writeInt(msgEncrypt.length);
		saida.write(msgEncrypt);
	}
}
