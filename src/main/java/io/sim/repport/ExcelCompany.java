package io.sim.repport;

import java.io.IOException;

import org.apache.poi.EncryptedDocumentException;

import io.sim.simulation.EnvSimulator;
import io.sim.company.MobilityCompany;

public class ExcelCompany extends Thread
{
    private MobilityCompany company;
    
    public ExcelCompany(MobilityCompany _company)
    {
        super("ExcelCompany");
        this.company = _company;
    }

    @Override
    public void run()
    {
        try
        {
            long sleepTime = EnvSimulator.ACQUISITION_RATE/EnvSimulator.NUM_DRIVERS;
            while(!MobilityCompany.isServiceEnded() || !company.isCarsRepportEmpty()) // company.isAlive()
            {
                if(!company.isCarsRepportEmpty())
                {
                    ExcelRepport.updateSSDrivingData(this.company.removeRepport());
                }
                sleep(sleepTime);
            }
        }
        catch (EncryptedDocumentException | IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
