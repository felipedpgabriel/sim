package io.sim.created;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**Classe de rotas
 * Tem edges e routeID.
 */
public class RouteN implements Serializable
{
    private String edges;
    private String routeID;

    public RouteN(String routeID, String edges)
    {
        this.edges = edges;
        this.routeID = routeID;
    }

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
                Element edges = (Element) node; // extrai as edges -> FALTA ver se eh o primeiro ponto ou a edge toda
                RouteN route = new RouteN(idRouteAux, edges.getAttribute("edges"));
                routes.add(route);
            }
        }

        return routes;
    }

    public String getRouteID()
    {
        return routeID;
    }

    public String getEdges()
    {
        return edges;
    }
}
