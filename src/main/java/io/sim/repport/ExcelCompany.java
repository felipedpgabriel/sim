package io.sim.repport;

import java.io.IOException;

import org.apache.poi.EncryptedDocumentException;

import io.sim.simulation.EnvSimulator;
import io.sim.company.MobilityCompany;

public class ExcelCompany extends Thread
{
    private MobilityCompany company;
    private int edgesSize;
    
    public ExcelCompany(MobilityCompany _company, int _edgesSize)
    {
        super("ExcelCompany");
        this.company = _company;
        this.edgesSize = _edgesSize;
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
            ExcelRepport.setFlowParam(this.edgesSize);
            ExcelRepport.setRecParam(this.edgesSize);
        }
        catch (EncryptedDocumentException | IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
