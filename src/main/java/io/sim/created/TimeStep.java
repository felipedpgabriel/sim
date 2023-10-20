package io.sim.created;

import io.sim.EnvSimulator;
import it.polito.appeal.traci.SumoTraciConnection;

public class TimeStep extends Thread
{
    private SumoTraciConnection sumo;
    
    public TimeStep(SumoTraciConnection _sumo)
    {
        this.sumo = _sumo;
    }

    @Override
    public void run()
    {
        while(true)
        {
            try {
                this.sumo.do_timestep();
                sleep(EnvSimulator.ACQUISITION_RATE);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                break;
            }
        }
    }
}
