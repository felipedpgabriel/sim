package io.sim.driver;

import io.sim.repport.ExcelRepport;
import io.sim.simulation.EnvSimulator;

/**
 * Classe que executa o gasto de combustivel da classe Car. 
 */
public class CarFuelManager extends Thread
{
    private Car car;
    private double fuelConsumption; // ml/s
    // Escalonamento
    private long initRunTime;
    private long endRunTime;
    private long birthTime;

    /**
     * Construtor da classe CarFuelManager.
     * @param car
     */
    public CarFuelManager(Car car) {
        this.birthTime = System.nanoTime();
        this.car = car;
        this.fuelConsumption = 0;
    }

    @Override
    public void run()
    {
        this.initRunTime = System.nanoTime();
        System.out.println("Iniciando FuelManager");
        while(!this.car.isFinished())
        {
            try
            {
                sleep(EnvSimulator.ACQUISITION_RATE);
                while(this.car.iscarOn())
                {
                    while(this.car.iscarOn() && !this.car.isAbastecendo())
                    {
                        this.car.setFuelTank(this.car.getFuelTank() - this.fuelConsumption);
                        if(this.car.getFuelTank() <= (EnvSimulator.MIN_FUEL_TANK * 1000))
                        {   
                            this.car.setAbastecendo(true);
                            this.car.setSpeed(0);
                            System.out.println(this.car.getCarID() + " parou para abastecer: " + this.car.getFuelTank());
                        }
                        sleep(1000); // consumo por segundo
                    }
                }
                this.endRunTime = System.nanoTime();
                ExcelRepport.updateSSScheduling("CarFuelManager", this.initRunTime, this.endRunTime, this.birthTime);
            }
            catch (Exception e) {
                    e.printStackTrace();
            }
        }   
        System.out.println("Finalizando FuelManager");
    }

    public void setFuelConsumption(double fuelConsumption) {
        this.fuelConsumption = fuelConsumption;
    }

    public double getFuelConsumption() {
        return this.fuelConsumption;
    }
}
