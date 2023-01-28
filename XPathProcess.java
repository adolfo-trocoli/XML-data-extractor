import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Clase encargada de procesar el fichero XML generado por la anterior fase. Extraerá información acerca de la query
 * realizada, los datasets existentes y los recursos.
 * 
 * @author Adolfo Trocolí Naranjo, 09218364X, a.trocoli@alumnos.upm.es
 *
 */
public class XPathProcess {

	private final Document doc;
	private List<Propiedad> propiedades;
	private XPath xPath;

	/**
	 * Constructor del objeto XPathProcess. Tan solo necesita el documento a leer.
	 * 
	 * @param fSalidaP4 Documento XML a leer por el procesador.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public XPathProcess(String fSalidaP4) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		doc = builder.parse(new File(fSalidaP4));
		xPath = XPathFactory.newInstance().newXPath();
		propiedades = new ArrayList<>();
	}

	/**
	 * Método encargado de evaluar el documento XML. Devuelve una lista de arrays de propiedades, de los cuales el
	 * primero y el segundo están compuestos por un solo elemento (query y número de datasets), el tercero y el cuarto
	 * llevan la información de los datasets (datasets y contadores), y el quinto lleva la colección de títulos de los
	 * resources.
	 * 
	 * @return
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	public List<Propiedad> evaluar() throws IOException, XPathExpressionException {
		propiedades.add(extractQuery());
		propiedades.add(extractNumDatasets()); // Meto los títulos al final para poder buscar por "title" en el
		propiedades.addAll(extractInfDatasets()); // constructor del documento JSON para poder separar la lista de
		propiedades.addAll(extractTitles()); // propiedades relacionadas con title y las relacionadas con infDatasets
		return propiedades;
	}

	/**
	 * Extrae el valor de la query.
	 * 
	 * @return Valor textual de la query.
	 * @throws XPathExpressionException
	 */
	private Propiedad extractQuery() throws XPathExpressionException {
		final String queryXPath = "//query";
		String query = (String) xPath.evaluate(queryXPath, doc, XPathConstants.STRING);
		return new Propiedad("query", query);
	}

	/**
	 * Cuenta los datasets presentes en el documento.
	 * 
	 * @return Número de datasets.
	 * @throws XPathExpressionException
	 */
	private Propiedad extractNumDatasets() throws XPathExpressionException {
		final String numDatasetsXPath = "count(//datasets/dataset)";
		Double numDatasets = (Double) xPath.evaluate(numDatasetsXPath, doc, XPathConstants.NUMBER);
		return new Propiedad("numDataset", numDatasets.toString());
	}

	/**
	 * Devuelve objetos Propiedad cuyo nombre es el id del dataset y el valor la cuenta de resources que lo tienen. Esto
	 * no debería ser así, puesto que lo suyo es que los objetos Propiedad sean o bien {"id": "https://ejemplo.json"}
	 * {"num": "4.0"} Sin embargo, como evaluar() solo puede devolver una lista de propiedades, escribirlas por separado
	 * conllevaría el riesgo de que al reconstruir los datos no se puediese saber el orden y, por tanto, qué cuenta
	 * corresponde a cada dataset.
	 * 
	 * @return Lista de propiedades cuyo nombre es el id del dataset y valor es la cuenta de recursos asociados.
	 * @throws XPathExpressionException
	 */
	private List<Propiedad> extractInfDatasets() throws XPathExpressionException {
		List<String> datasetIDs = extractDatasetIDs();
		List<Propiedad> propiedades = new ArrayList<>();
		String countExpression;
		Double count;
		for (String datasetID : datasetIDs) {
			countExpression = "count(//resource[@id= \"" + datasetID + "\"])";
			count = (Double) xPath.evaluate(countExpression, doc, XPathConstants.NUMBER);
			propiedades.add(new Propiedad(datasetID, count.toString()));
		}
		return propiedades;
	}

	/**
	 * Extrae los IDs de los datasets presentes en el documento.
	 * 
	 * @return IDs de los datasets.
	 * @throws XPathExpressionException
	 */
	private List<String> extractDatasetIDs() throws XPathExpressionException {
		final String datasetsXPath = "//dataset";
		final String idXPath = "./@id";
		List<String> datasetIDs = new ArrayList<>();
		NodeList datasetNodes = (NodeList) xPath.evaluate(datasetsXPath, doc, XPathConstants.NODESET);
		String id;
		for (int i = 0; i < datasetNodes.getLength(); i++) {
			id = (String) xPath.evaluate(idXPath, datasetNodes.item(i), XPathConstants.STRING);
			datasetIDs.add(id);
		}
		return datasetIDs;
	}

	/**
	 * Extrae los títulos de los recursos presentes en el documento.
	 * 
	 * @return Títulos de los recursos.
	 * @throws XPathExpressionException
	 */
	private List<Propiedad> extractTitles() throws XPathExpressionException {
		List<Propiedad> titles = new ArrayList<>();
		final String titleXPath = "./title";
		final List<Element> resources = extractResources();
		String title;
		for (Element resource : resources) {
			title = (String) xPath.evaluate(titleXPath, resource, XPathConstants.STRING);
			titles.add(new Propiedad("title", title));
		}
		return titles;
	}

	/**
	 * Extrae los recursos presentes en el documento en forma de Element.
	 * 
	 * @return Recursos presentes en el documento.
	 * @throws XPathExpressionException
	 */
	private List<Element> extractResources() throws XPathExpressionException {
		final String resourcesXPath = "//resource";
		List<Element> resources = new ArrayList<>();
		NodeList resourceNodes = (NodeList) xPath.evaluate(resourcesXPath, doc, XPathConstants.NODESET);
		for (int i = 0; i < resourceNodes.getLength(); i++)
			resources.add((Element) resourceNodes.item(i));
		return resources;
	}
}