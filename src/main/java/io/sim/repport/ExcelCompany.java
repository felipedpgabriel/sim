package io.sim.repport;

import java.io.IOException;

import org.apache.poi.EncryptedDocumentException;

import io.sim.simulation.EnvSimulator;
import io.sim.company.MobilityCompany;
import io.sim.reconciliation.Rec;

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
            int av = EnvSimulator.AV;
            long sleepTime = EnvSimulator.ACQUISITION_RATE/EnvSimulator.NUM_DRIVERS;
            while(!MobilityCompany.isServiceEnded() || !company.isCarsRepportEmpty()) // company.isAlive()
            {
                if(!company.isCarsRepportEmpty())
                {
                    ExcelRepport.updateSSDrivingData(this.company.removeRepport());
                }
                sleep(sleepTime);
            }
            if(av == 2)
            {
                ExcelRepport.setFlowParam(this.edgesSize);
                ExcelRepport.setStatistics(this.edgesSize);
                double[][] recParam = ExcelRepport.getRecParam(this.edgesSize, 2);

                Rec rec = new Rec(recParam[0], recParam[1], recParam[2], recParam[3]);
                rec.start();
                rec.join();
            }
        }
        catch (EncryptedDocumentException | IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
