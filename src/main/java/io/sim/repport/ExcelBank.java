package io.sim.repport;

import java.io.IOException;

import org.apache.poi.EncryptedDocumentException;

import io.sim.simulation.EnvSimulator;
import io.sim.bank.AlphaBank;

public class ExcelBank extends Thread
{
    private AlphaBank bank;
    // Escalonamento
    private long initRunTime;
    private long endRunTime;
    private long birthTime;
    
    public ExcelBank(AlphaBank _bank)
    {
        super("ExcelBank");
        this.birthTime = System.nanoTime();
        this.bank = _bank;
    }

    @Override
    public void run()
    {
        this.initRunTime = System.nanoTime();
        try
        {
            long sleepTime = EnvSimulator.ACQUISITION_RATE/3;
            while(!AlphaBank.isAccountsEnded() || !bank.isBankServicesEmpty()) // bank.isAlive()
            {
                if(!bank.isBankServicesEmpty())
                {
                    ExcelRepport.updateSSBankService(this.bank.removeServices());
                }
                sleep(sleepTime);
            }
            this.endRunTime = System.nanoTime();
			ExcelRepport.updateSSScheduling("ExcelBank", this.initRunTime, this.endRunTime, this.birthTime);
        }
        catch (EncryptedDocumentException | IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
