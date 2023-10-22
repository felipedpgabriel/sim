package io.sim;

import it.polito.appeal.traci.SumoTraciConnection;

public class TankUpdate extends Thread
{
    private Car car;
    private SumoTraciConnection sumo;

    public TankUpdate(Car car, SumoTraciConnection sumo) {
        this.car = car;
        this.sumo = sumo;
    }

    @Override
    public void run()
    {
        while(!this.car.isFinished())
        {
            while(this.car.iscarOn())
            {
                // TODO verifica se esta abastecendo
                // TODO verifica se precisa abastecer
                // TODO decrementar o tanque temp de 1000 ms
                // TODO mudar car.abastecendo conforme o caso
                // TODO setar speed = 0
                // TODO voltar speed normal (usar atributo de Car)
            //     try
            // {
            //     if(this.car.getFuelTank() <= (EnvSimulator.MIN_FUEL_TANK * 1000))
            //     {   
            //         this.car.setAbastecendo(true);
            //         this.car.setSpeed(0);
            //     }
            // } catch (Exception e) {
            //     // TODO Auto-generated catch block
            //     e.printStackTrace();
            // }
            }
        }   
    }
}
