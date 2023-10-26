package io.sim.driver;

import io.sim.simulation.EnvSimulator;

/**
 * Classe que executa o gasto de combustivel da classe Car. 
 */
public class CarFuelManager extends Thread
{
    private Car car;
    private double fuelConsumption; // ml/s

    /**
     * Construtor da classe CarFuelManager.
     * @param car
     */
    public CarFuelManager(Car car) {
        this.car = car;
        this.fuelConsumption = 0;
    }

    @Override
    public void run()
    {
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
                        // this.sumo.do_job_set(Vehicle.setSpeedMode(this.car.getcarID(), 31));
                        // this.car.setSpeed(this.car.getSpeedDefault());
                        this.car.setFuelTank(this.car.getFuelTank() - this.fuelConsumption);
                        // System.out.println("Consumo: " + this.fuelConsumption);
                        // System.out.println("Nivel de combustivel " + this.car.getcarID() +": " + this.car.getFuelTank());
                        if(this.car.getFuelTank() <= (EnvSimulator.MIN_FUEL_TANK * 1000))
                        {   
                            this.car.setAbastecendo(true);
                            this.car.setSpeed(0);
                            System.out.println(this.car.getCarID() + " parou para abastecer: " + this.car.getFuelTank());
                        }
                        sleep(1000); // consumo por segundo
                    }
                }
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
