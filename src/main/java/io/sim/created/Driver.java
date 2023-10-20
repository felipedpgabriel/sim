package io.sim.created;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import io.sim.Auto;

public class Driver extends Thread
{
	// atributos de cliente
	private String driverHost;
	private int servPort;
    // atributos da classe
    private String driverID;
    private Account account;
    private Auto car; // private Car car;
    // private static final double FUEL_PRICE = 5.87;
    private long acquisitionRate;
    // private ArrayList<RouteN> routeToExe = new ArrayList<RouteN>();
    private ArrayList<RouteN> routesExecuted = new ArrayList<RouteN>();
    private ArrayList<RouteN> routesInExe = new ArrayList<RouteN>();
    private boolean initRoute = false;

    public Driver(String driverHost, int servPort, String driverID, Auto car, long acquisitionRate)
    {
        account = new Account(0, driverID, (driverID + 123));
        this.driverHost = driverHost;
        this.servPort = servPort;
        this.driverID = driverID;
        this.car = car;
        this.acquisitionRate = acquisitionRate;
    }

    @Override
    public void run()
    {
        try {
            // System.out.println(this.idDriver + " no try.");
            Socket socket = new Socket(this.driverHost, this.servPort);
			// System.out.println(this.idDriver + " passou do socket.");
            DataInputStream entrada = new DataInputStream(socket.getInputStream());
			// System.out.println(this.idDriver + " passou da entrada.");
            DataOutputStream saida = new DataOutputStream(socket.getOutputStream());
            
            System.out.println("Iniciando " + this.driverID);
            this.car.start();
            while(this.car.isAlive())
            // while(MobilityCompany.areRoutesAvailable() || !this.routesInExe.isEmpty()) // this.car.getCarRepport().getCarState().equals("rodando")
            {
                Thread.sleep(acquisitionRate);
                String carState = this.car.getCarRepport().getCarState();
                if(carState.equals("finalizado"))
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
            }
            System.out.println("Encerrando " + this.driverID);
            entrada.close();
			saida.close();
			socket.close();
        } catch (InterruptedException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getDriverID() {
        return driverID;
    }
}
