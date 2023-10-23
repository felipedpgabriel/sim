package io.sim.simulation;

import it.polito.appeal.traci.SumoTraciConnection;

/**Classe que faz o timestep do SUMO.
 *Feito separadamente em uma Thread para evitar multiplos timesteps e melhorar o desempenho. 
 */
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
            try
            {
                this.sumo.do_timestep();
                sleep(EnvSimulator.ACQUISITION_RATE); // Step da simulacao
            }
            catch (Exception e)
            {
                e.printStackTrace();
                break;
            }
        }
    }
}
