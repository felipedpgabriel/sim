package io.sim;

import static org.junit.Assert.*;

import java.net.ServerSocket;

import org.junit.Before;
import org.junit.Test;

import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.objects.SumoColor;
import io.sim.bank.AlphaBank;
import io.sim.driver.Car;

public class FuelStationTest
{
    private Car car;

    @Before
    public void setUp() throws Exception 
    {
        ServerSocket bankServer = new ServerSocket(22222);
        new AlphaBank(bankServer, 4);
        // Configurar objetos necessários para o teste
        SumoColor color = new SumoColor(0, 255, 0, 126);
        SumoTraciConnection sumo = new SumoTraciConnection("sumo-gui", "map/map.sumo.cfg");
        car = new Car(true, "127.0.0.1", 11111, "CAR1", "D1", color, sumo,
        2, 1, 1,1);

        new FuelStation("127.0.0.1", 22222, 2, 5);
    }

    @Test
    public void testFuel() throws InterruptedException {
        // Configurar o estado inicial do carro
        car.setFuelTank(10); // Tanque de combustível com 10 litros

        // Testar o método fuel com uma quantidade de combustível de 5 litros
        boolean abastecido = FuelStation.fuel(car, 5);

        // Verificar se o carro foi abastecido corretamente
        assertTrue(abastecido);
        assertEquals(15, car.getFuelTank(), 0.01); // Verificar se o tanque de combustível foi aumentado em 5 litros
    }
}

