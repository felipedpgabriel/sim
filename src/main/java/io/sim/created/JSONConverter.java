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
        long jsTimeStamp = jsonOut.getLong("TimeStamp");
		String jsAutoID = jsonOut.getString("AutoID");
        String jsRouteIDSUMO = jsonOut.getString("RouteIDSUMO");
        double jsSpeed = jsonOut.getDouble("Speed");
        double jsDistance = jsonOut.getDouble("Distance");
        double jsFuelConsumption = jsonOut.getDouble("FuelConsumption");
        int jsFuelType = jsonOut.getInt("FuelType");
        double jsCo2Emission = jsonOut.getDouble("Co2Emission");
        long jsLongitude = jsonOut.getLong("Longitude");
        long jsLatitude = jsonOut.getLong("Latitude");

        DrivingData carRepport = new DrivingData(jsCarState, jsTimeStamp, jsAutoID, jsRouteIDSUMO, jsSpeed, jsDistance, jsFuelConsumption,
        jsFuelType, jsCo2Emission,jsLongitude, jsLatitude);

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
        jsonOut.put("TimeStamp",_carRepport.getTimeStamp());
        jsonOut.put("AutoID",_carRepport.getAutoID());
        jsonOut.put("RouteIDSUMO",_carRepport.getRouteIDSUMO());
        jsonOut.put("Speed",_carRepport.getSpeed());
		jsonOut.put("Distance",_carRepport.getDistance());
        jsonOut.put("FuelConsumption",_carRepport.getFuelConsumption());
        jsonOut.put("FuelType",_carRepport.getFuelType());
        jsonOut.put("Co2Emission",_carRepport.getCo2Emission());
		jsonOut.put("Longitude",_carRepport.getLongitude());
		jsonOut.put("Latitude",_carRepport.getLatitude());

        return jsonOut.toString();
	}
}
