package io.sim.created;

import it.polito.appeal.traci.SumoTraciConnection;

public class TimeStep extends Thread{
    private SumoTraciConnection sumo;
    private long acquisitionRate;
    public TimeStep(SumoTraciConnection _sumo, long _acquisitionRate)
    {
        this.sumo = _sumo;
        this.acquisitionRate = _acquisitionRate;
    }

    @Override
    public void run()
    {
        while(true)
        {
            try {
                this.sumo.do_timestep();
                sleep(acquisitionRate);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                break;
            }
        }
    }
}
