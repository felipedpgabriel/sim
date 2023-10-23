package io.sim.created.repport;

import java.io.IOException;

import org.apache.poi.EncryptedDocumentException;

import io.sim.EnvSimulator;
import io.sim.created.bank.AlphaBank;

public class ExcelBank extends Thread
{
    private AlphaBank bank;
    
    public ExcelBank(AlphaBank _bank) {
        super("ExcelBank");
        this.bank = _bank;
    }

    @Override
    public void run()
    {
        try
        {
            while(bank.isAlive())
            {
                if(!bank.isBankServicesEmpty())
                {
                    ExcelRepport.updateSSBankService(this.bank.removeServices());
                }
                sleep(EnvSimulator.ACQUISITION_RATE/3);
            }
        }
        catch (EncryptedDocumentException | IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
