package io.sim.created.repport;

import java.io.IOException;

import org.apache.poi.EncryptedDocumentException;

import io.sim.EnvSimulator;
import io.sim.created.company.MobilityCompany;

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
            while(!MobilityCompany.isRoutesInExeEmpty() || !company.isCarsRepportEmpty()) // company.isAlive()
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
