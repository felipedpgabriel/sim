package io.sim;

/**
 * Classe Princiapl com o metodo main
 */
public class App
{
   public static void main( String[] args ) throws InterruptedException
   {
        System.out.println("Inicia o simulador");
        EnvSimulator ev = new EnvSimulator();
        ev.start();
        ev.join();
        System.exit(0);
    }
}
