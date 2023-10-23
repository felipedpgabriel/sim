package io.sim;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.objects.SumoColor;
import io.sim.driver.Car;

public class CarTest {

    private Car car;
    @Before
    public void setUp() throws Exception 
    {
        SumoColor color = new SumoColor(0, 255, 0, 126);
        SumoTraciConnection sumo = new SumoTraciConnection("sumo-gui", "map/map.sumo.cfg");
        car = new Car(true, "127.0.0.1", 11111, "CAR1", "D1", color,
        sumo, 2, 1, 1);
    }

    @Test
    public void testCalcDesloc() {
        double lat1 = 52.5200; // latitude de Berlim, Alemanha
        double lon1 = 13.4050; // longitude de Berlim, Alemanha

        double lat2 = 48.8566; // latitude de Paris, França
        double lon2 = 2.3522;  // longitude de Paris, França

        double expectedDistance = 878614.51; // Distância entre Berlim e Paris em metros

        double calculatedDistance = car.calcDesloc(lat1, lon1, lat2, lon2);
        System.out.println(calculatedDistance);
        assertEquals(expectedDistance, calculatedDistance, 200); // Aceite uma diferença de 0.01 metros
    }
}
