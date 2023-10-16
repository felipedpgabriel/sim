package io.sim.created;

import java.util.ArrayList;

import io.sim.Auto;

public class Driver extends Thread
{
    private String driverID;

    // // Cliente de AlphaBank
    // private Account account;
    // private TransportService ts;
    private Auto car; // private Car car;
    // private static final double FUEL_PRICE = 5.87;
    // private Route route;
    private long acquisitionRate;
    private ArrayList<RouteN> routeToExe = new ArrayList<RouteN>();
    private ArrayList<RouteN> routesExecuted = new ArrayList<RouteN>();
    private ArrayList<RouteN> routesInExe = new ArrayList<RouteN>();
    private boolean initRoute = false;

    public Driver(String _driverID, Auto _car, long _acquisitionRate)
    {
        this.driverID = _driverID;
        this.car = _car;
        this.acquisitionRate = _acquisitionRate;
        // this.start();
        // pensar na logica de inicializacao do TransporteService e do Car
        // this.car.start();
        // BotPayment payment = new BotPayment(fuelPrice);
    }

    @Override
    public void run()
    {
        try {
            System.out.println("Iniciando " + this.driverID);
            this.car.start();
            while(this.car.isAlive())
            // while(MobilityCompany.areRoutesAvailable() || !this.routesInExe.isEmpty()) // this.car.getCarRepport().getCarState().equals("rodando")
            {
                String carState = this.car.getCarRepport().getCarState();
                Thread.sleep(acquisitionRate);
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
            // this.car.setfinished(true);
            System.out.println("Encerrando " + this.driverID);
            // this.car.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // System.out.println("Iniciando " + this.driverID);
        // this.car.start();
        // while(MobilityCompany.areRoutesAvailable()) // retirar segundo termo
        // {
        //     // Thread.sleep(this.car.getAcquisitionRate());
        //     if(this.car.getCarRepport().getCarState() == "finalizado")
        //     {
        //         // retirar de routesInExe e colocar em routesExecuted
        //         System.out.println(this.driverID + " rota "+ this.routesInExe.get(0).getRouteID() +" finalizada");
        //         this.routesExecuted.add((this.routesInExe.remove(0)));
        //         initRoute = false;
        //     }
        //     else if((this.car.getCarRepport().getCarState() == "rodando") && !initRoute)
        //     {
        //         System.out.println(this.driverID + " rota "+ this.car.getRoute().getRouteID() +" iniciada");
        //         this.routesInExe.add(this.car.getRoute());
        //         initRoute = true; 
        //     }
        // }
        // System.out.println("Encerrando " + this.driverID);
        // this.car.setfinished(true);

    }

    public String getDriverID() {
        return driverID;
    }
}
