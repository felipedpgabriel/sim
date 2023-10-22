package io.sim.created;

// import de.tudresden.sumo.cmd.Vehicle;
import io.sim.Car;
import io.sim.EnvSimulator;
import it.polito.appeal.traci.SumoTraciConnection;

public class CarFuelManager extends Thread
{
    private Car car;
    private SumoTraciConnection sumo;
    private double fuelConsumption; // ml/s

    public CarFuelManager(Car car, SumoTraciConnection sumo) {
        this.car = car;
        this.sumo = sumo;
        this.fuelConsumption = EnvSimulator.FUEL_CONSUMPTION; // ml/s
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
                    if(!this.car.isAbastecendo())
                    {
                        System.out.println("Voltou a consumir.");
                        // this.sumo.do_job_set(Vehicle.setSpeedMode(this.car.getIdAuto(), 31));
                        // this.car.setSpeed(0);
                    }
                    while(this.car.iscarOn() && !this.car.isAbastecendo())
                    {
                        // this.sumo.do_job_set(Vehicle.setSpeedMode(this.car.getIdAuto(), 31));
                        // this.car.setSpeed(this.car.getSpeedDefault());
                        this.car.setFuelTank(this.car.getFuelTank() - this.fuelConsumption);
                        // System.out.println("Consumo: " + this.fuelConsumption);
                        // System.out.println("Nivel de combustivel " + this.car.getIdAuto() +": " + this.car.getFuelTank());
                        if(this.car.getFuelTank() <= (EnvSimulator.MIN_FUEL_TANK * 1000))
                        {   
                            this.car.setAbastecendo(true);
                            this.car.setSpeed(0);
                            System.out.println(this.car.getIdAuto() + " parou para abastecer: " + this.car.getFuelTank());
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
