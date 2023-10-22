package io.sim.created.messages;

import org.json.JSONObject;

import io.sim.DrivingData;
import io.sim.created.BankService;
import io.sim.created.RouteN;

/**Classe para tratar as comunicacoes de Cl
 * 
 */
public class JSONConverter
{
    /**Converte uma string no formato JSON para um objeto do tipo RouteN
     * @param _string String - no formato JSON
     * @return RouteN
     */
    public static RouteN stringToRouteN(String _string)
	{
		JSONObject jsonIn = new JSONObject(_string);
		String jsRouteID = jsonIn.getString("RouteID");
		String jsEdges = jsonIn.getString("Edges");
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
		JSONObject jsonIn = new JSONObject(_string);
		String jsCarState = jsonIn.getString("CarState");
        String jsDriverLogin = jsonIn.getString("DriverLogin");
        long jsTimeStamp = jsonIn.getLong("TimeStamp");
		String jsAutoID = jsonIn.getString("AutoID");
        String jsRouteIDSUMO = jsonIn.getString("RouteIDSUMO");
        double jsSpeed = jsonIn.getDouble("Speed");
        double jsDistance = jsonIn.getDouble("Distance");
        double jsFuelConsumption = jsonIn.getDouble("FuelConsumption");
        int jsFuelType = jsonIn.getInt("FuelType");
        double jsCo2Emission = jsonIn.getDouble("Co2Emission");
        double jsLongitude = jsonIn.getDouble("Longitude");
        double jsLatitude = jsonIn.getDouble("Latitude");

        DrivingData carRepport = new DrivingData(jsCarState, jsDriverLogin,jsTimeStamp, jsAutoID, jsRouteIDSUMO, jsSpeed, jsDistance, jsFuelConsumption,
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
        jsonOut.put("DriverLogin",_carRepport.getDriverLogin());
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

    public static String setJSONboolean(boolean _bool)
    {
        JSONObject jsonOut = new JSONObject();
        jsonOut.put("Bool",_bool);

		return jsonOut.toString();
    }

    public static boolean getJSONboolean(String _bool)
    {
        JSONObject jsonIn = new JSONObject(_bool);
        return jsonIn.getBoolean("Bool");
    }

    public static BankService stringToBankService(String _transaction)
    {
        JSONObject jsonIn = new JSONObject(_transaction);
        String jsService = jsonIn.getString("Service");
		String jsSenha = jsonIn.getString("Senha");
		String jsOrigem = jsonIn.getString("Origem");
        double jsValor = jsonIn.getDouble("Valor");
        String jsDestino = jsonIn.getString("Destino");
		BankService transaction = new BankService(jsService, jsSenha, jsOrigem, jsValor, jsDestino);
		return transaction;
    }

    public static String bankServiceToString(BankService _transaction)
    {
        JSONObject jsonOut = new JSONObject();
        jsonOut.put("Service",_transaction.getService());
        jsonOut.put("Senha",_transaction.getSenha());
        jsonOut.put("Origem",_transaction.getOrigem());
        jsonOut.put("Valor",_transaction.getValor());
        jsonOut.put("Destino",_transaction.getDestino());
        return jsonOut.toString();
    }
}
