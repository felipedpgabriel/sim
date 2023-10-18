package io.sim.created;

import org.json.JSONObject;

import io.sim.DrivingData;

/**Classe para tratar as comunicacoes de Cl
 * IMP# Falta acrescentar metodos para converter transacoes financeiras
 */
public class JSONConverter
{
    /**Converte uma string no formato JSON para um objeto do tipo RouteN
     * @param _string String - no formato JSON
     * @return RouteN
     */
    public static RouteN stringToRouteN(String _string)
	{
		JSONObject jsonOut = new JSONObject(_string);
		String jsRouteID = jsonOut.getString("RouteID");
		String jsEdges = jsonOut.getString("Edges");
		RouteN route = new RouteN(jsRouteID, jsEdges);
		return route;
	}

    /**Converte um objeto do tipo RouteN para uma string no formato JSON
     * @param _route RouteN 
     * @return String - no formato JSON
     */
    public static String routeNtoString(RouteN _route)
    {
        JSONObject jsonOut = new JSONObject();
        jsonOut.put("RouteID",_route.getRouteID());
        jsonOut.put("Edges",_route.getEdges());
        return jsonOut.toString();
    }

    /**Converte uma string no formato JSON para um objeto do tipo DrivingData
     * @param _string String - no formato JSON
     * @return DrivingData
     */
    public static DrivingData stringToDrivingData(String _string)
	{
		JSONObject jsonOut = new JSONObject(_string);
		String jsCarState = jsonOut.getString("CarState");
		String jsAutoID = jsonOut.getString("AutoID");
        String jsDriverID = jsonOut.getString("DriverID");
        long jsTimeStamp = jsonOut.getLong("TimeStamp");
        double jsX_Position = jsonOut.getDouble("X_Position");
        double jsY_Position = jsonOut.getDouble("Y_Position");
        String jsRoadIDSUMO = jsonOut.getString("RoadIDSUMO");
        String jsRouteIDSUMO = jsonOut.getString("RouteIDSUMO");
        double jsSpeed = jsonOut.getDouble("Speed");
        double jsOdometer = jsonOut.getDouble("Odometer");
        double jsFuelConsumption = jsonOut.getDouble("FuelConsumption");
        double jsAverageFuelConsumption = jsonOut.getDouble("AverageFuelConsumption");
        int jsFuelType = jsonOut.getInt("FuelType");
        double jsFuelPrice = jsonOut.getDouble("FuelPrice");
        double jsCo2Emission = jsonOut.getDouble("Co2Emission");
        double jsHCEmission = jsonOut.getDouble("HCEmission");
        int jsPersonCapacity = jsonOut.getInt("PersonCapacity");
        int jsPersonNumber = jsonOut.getInt("PersonNumber");

        DrivingData carRepport = new DrivingData(jsCarState, jsAutoID, jsDriverID, jsTimeStamp, jsX_Position, jsY_Position, jsRoadIDSUMO,
        jsRouteIDSUMO, jsSpeed, jsOdometer, jsFuelConsumption, jsAverageFuelConsumption, jsFuelType, jsFuelPrice, jsCo2Emission, jsHCEmission,
        jsPersonCapacity, jsPersonNumber);

		return carRepport;
	}

    /**Converte um objeto do tipo DrivingData para uma string no formato JSON
	 * @param _carRepport DrivingData
	 * @return String - no formato JSON
	 */
	public static String drivingDataToString(DrivingData _carRepport)
	{
        JSONObject jsonOut = new JSONObject();
        jsonOut.put("CarState",_carRepport.getCarState());
        jsonOut.put("AutoID",_carRepport.getAutoID());
		jsonOut.put("DriverID",_carRepport.getDriverID());
		jsonOut.put("TimeStamp",_carRepport.getTimeStamp());
		jsonOut.put("X_Position",_carRepport.getX_Position());
		jsonOut.put("Y_Position",_carRepport.getY_Position());
		jsonOut.put("RoadIDSUMO",_carRepport.getRoadIDSUMO());
		jsonOut.put("RouteIDSUMO",_carRepport.getRouteIDSUMO());
		jsonOut.put("Speed",_carRepport.getSpeed());
		jsonOut.put("Odometer",_carRepport.getOdometer());
		jsonOut.put("FuelConsumption",_carRepport.getFuelConsumption());
		jsonOut.put("AverageFuelConsumption",_carRepport.getAverageFuelConsumption());
		jsonOut.put("FuelType",_carRepport.getFuelType());
		jsonOut.put("FuelPrice",_carRepport.getFuelPrice());
		jsonOut.put("Co2Emission",_carRepport.getCo2Emission());
		jsonOut.put("HCEmission",_carRepport.getHCEmission());
		jsonOut.put("PersonCapacity",_carRepport.getPersonCapacity());
		jsonOut.put("PersonNumber",_carRepport.getPersonNumber());

        return jsonOut.toString();
	}
}
