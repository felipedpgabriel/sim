package io.sim;

// import java.io.IOException;

// import org.apache.poi.EncryptedDocumentException;

// import io.sim.reconciliation.Rec;
// import io.sim.repport.ExcelRepport;
import io.sim.simulation.EnvSimulator;

/**
 * Classe Princiapl com o metodo main
 */
public class App
{
   public static void main( String[] args ) throws InterruptedException
   {
        System.out.println("Inicia a simulacao");
        EnvSimulator ev = new EnvSimulator();
        ev.start(); // Inicia o simulador
        ev.join();
        System.exit(0); // Encerra o programa
        // try {
        //     double[][] recParam = ExcelRepport.getRecParam(26, 2);
        //     Rec rec = new Rec(recParam[0], recParam[1], recParam[2], recParam[3]);
        //     rec.start();
        //     rec.join();
        // } catch (EncryptedDocumentException | IOException e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        // }
    }
}
