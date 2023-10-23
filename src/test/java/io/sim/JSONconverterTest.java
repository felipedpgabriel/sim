package io.sim;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import io.sim.company.RouteN;
import io.sim.messages.JSONconverter;

public class JSONconverterTest
{
    private RouteN originalRoute;

    @Before
    public void setUp()
    {
        originalRoute = new RouteN("1", "341671964#3 341671964#4 341671964#5 341671964#6");
    }

    @Test
    public void testRouteNConversion()
    {
        String jsonRoute = JSONconverter.routeNtoString(originalRoute);
        RouteN convertedRoute = JSONconverter.stringToRouteN(jsonRoute);

        // Verificar se o objeto convertido e igual ao objeto original
        assertEquals(originalRoute.getRouteID(), convertedRoute.getRouteID());
        assertEquals(originalRoute.getEdges(), convertedRoute.getEdges());
    }   
}
