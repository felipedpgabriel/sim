package io.sim;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.annotation.Obsolete;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**Representa um Itinerario de rota. PODE VIRAR A CLASSE ROUTE
 *Contem um ID e as edges para seguir
 */
@Obsolete
public class Itinerary {

	private boolean on; // indica se a classe esta ativa
	private String uriItineraryXML; 
	private String[] itinerary; // {id_do_itinerario, edges}
	private String idItinerary;

	public Itinerary(String _uriRoutesXML, String _idRoute) {
		this.uriItineraryXML = _uriRoutesXML; //Arquivo com as rotas
		this.idItinerary = _idRoute; // id da rota para extracao
		try {
			// Armazena as rotas do arquivo em uma NodeList
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(this.uriItineraryXML); // arquivo das rotas no formato Documents
			NodeList nList = doc.getElementsByTagName("vehicle"); // lista de rotas com a tag vehicle

			for (int i = 0; i < nList.getLength(); i++) {
				Node nNode = nList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element elem = (Element) nNode; // pausa 
					String idRouteAux = this.idItinerary; 
					Node node = elem.getElementsByTagName("route").item(0);
					Element edges = (Element) node; // extrai as edges -> FALTA ver se eh o primeiro ponto ou a edge toda
					this.itinerary = new String[] { idRouteAux, edges.getAttribute("edges") };
				}
			}

			Thread.sleep(100);
			this.on = true;

		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public String getIDItinerary() {
		return this.idItinerary;
	}

	public String getUriItineraryXML() {
		return this.uriItineraryXML;
	}

	public String[] getItinerary() {
		return this.itinerary;
	}

	public String getIdItinerary() {
		return this.idItinerary;
	}

	public boolean isOn() {
		return this.on;
	}
}