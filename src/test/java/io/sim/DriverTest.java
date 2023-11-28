package io.sim;

import static org.junit.Assert.*;

import java.net.ServerSocket;

import org.junit.Before;
import org.junit.Test;

import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.objects.SumoColor;
import io.sim.bank.AlphaBank;
import io.sim.driver.Car;
import io.sim.driver.Driver;

public class DriverTest {

    private Driver driver;
    private Car car;

    @Before
    public void setUp() throws Exception 
    {
        ServerSocket bankServer = new ServerSocket(22222);
        new AlphaBank(bankServer, 4);

        SumoColor color = new SumoColor(0, 255, 0, 126);
        SumoTraciConnection sumo = new SumoTraciConnection("sumo-gui", "map/map.sumo.cfg");
        car = new Car(true, "127.0.0.1", 11111, "CAR1", "D1", color,
        sumo, 2, 1, 1,1);

        driver = new Driver("127.0.0.1", 22222, "D1", car, 300);

    }

    @Test
    public void testCalcFuelQtd() {

        // Saldo inicial por padrao eh 20,55

        // 

        // Configurar o preço do combustível
        car.setFuelTank(3000);

        // Chamar o método calcFuelQtd()
        double fuelQtd = driver.calcFuelQtd();

        // Verificar se o resultado é o esperado
        // O saldo da conta do motorista é 100 unidades monetárias, e o preço do combustível é 2 unidades monetárias por litro.
        // Portanto, o máximo que o motorista pode comprar é 100/2 = 50 litros.
        // Como o carro já tem 50 litros no tanque, o método deve retornar 0.
        assertEquals(3500.85, fuelQtd, 0.01);
    }
}

