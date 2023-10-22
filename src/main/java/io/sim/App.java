package io.sim;

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
    }
}
