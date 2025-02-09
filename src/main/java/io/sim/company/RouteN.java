package io.sim.company;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Classe de rotas (substitui Itinerary).
 *O nome RouteN eh devido a ja existencia de uma classe Route.
 */
public class RouteN implements Serializable
{
    private String edges;
    private String routeID;
    private ArrayList<String> edgesList;

    /**Contrutor da classe RouteN.
     * 
     * @param routeID String - ID da rota.
     * @param edges String - Edges que configuram o trajeto.
     */
    public RouteN(String routeID, String edges)
    {
        this.edges = edges;
        this.routeID = routeID;
        this.edgesList =  this.splitEdgesList();
    }

    /**Extrai as rotas no formato XML para RouteN e adiciona em uma lista.
     * @param arqXML Strinf - Path do arquivo XML com as rotas
     * @return ArrayList<RouteN> - Lista de rotas.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static ArrayList<RouteN> extractRoutes(String arqXML) throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(arqXML); // arquivo das rotas no formato Documents
        NodeList nList = doc.getElementsByTagName("vehicle"); // lista de rotas com a tag vehicle
        ArrayList<RouteN> routes = new ArrayList<RouteN>();
        
        for (int i = 0; i < nList.getLength(); i++)
        {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE)
            {
                Element elem = (Element) nNode; // pausa 
                String idRouteAux = elem.getAttribute("id"); 
                Node node = elem.getElementsByTagName("route").item(0);
                Element edges = (Element) node; // extrai as edges 
                RouteN route = new RouteN(idRouteAux, edges.getAttribute("edges"));
                routes.add(route);
            }
        }

        return routes;
    }

    private ArrayList<String> splitEdgesList()
    {
        List<String> vetString = Arrays.asList(edges.split("\\s+"));
        ArrayList<String> alString = new ArrayList<>(vetString);

        return alString;
    }

    /**
     * Get padrao para o atributo routeID.
     * @return String - ID da rota.
     */
    public String getRouteID() {
        return routeID;
    }

    /**
     * Get padrao para o atributo edges.
     * @return String - Edges da rota.
     */
    public String getEdges() {
        return edges;
    }

    /**
     * Set padrao para o atributo routeID.
     * @param newID String - novo ID da rota.
     */
    public void setRouteID(String newID) {
        this.routeID = newID;
    }

    /**
     * Get padrao para o atributo edgesList.
     * @return String[] - Lista de edges da rota.
     */
    public ArrayList<String> getEdgesList(){
        return edgesList;
    }
}
