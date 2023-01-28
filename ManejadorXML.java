import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Clase encargada de parsear el documento xml y extraer la información
 * requerida: Label del \<concept\> cuyo \<code\> sea el buscado, sus
 * \<concepts\> anidados y los datasets relacionados.
 * 
 * @author Adolfo Trocolí Naranjo, 09218364X, a.trocoli@alumnos.upm.es
 */
public class ManejadorXML extends DefaultHandler implements ParserCatalogo {

	// Atributos del parser
	private String codigo;
	private StringBuilder sb;

	// Resultados extraídos
	private String sNombreCategoria; // Nombre de la categoría
	private List<String> lConcepts; // Lista con los uris de los elementos <concept> que pertenecen a la categoría
	private Map<String, HashMap<String, String>> hDatasets; // Mapa con información de los dataset que pertenecen a la
															// categoría
	// Variables auxiliares
	private boolean dentroDeConcept, encontrado, datasets, introducirEntrada;
	private String title, description, theme, idDataset, atributoID, chars;
	private int treeDepth;

	/**
	 * @param sCodigoConcepto código de la categoría a procesar
	 * @throws ParserConfigurationException
	 */
	public ManejadorXML(String sCodigoConcepto) throws SAXException, ParserConfigurationException {
		super();
		this.codigo = sCodigoConcepto;
		sb = new StringBuilder();
		lConcepts = new ArrayList<String>();
		hDatasets = new HashMap<String, HashMap<String, String>>();
	}

	/**
	 * <code><b>getLabel</b></code>
	 * 
	 * @return Valor de la cadena del elemento <code>label</code> del
	 *         <code>concept</code> cuyo elemento <code><b>code</b></code> sea
	 *         <b>igual</b> al criterio a búsqueda. <br>
	 *         null si no se ha encontrado el concept pertinente o no se dispone de
	 *         esta información
	 */
	@Override
	public String getLabel() {
		return sNombreCategoria;
	}

	/**
	 * <code><b>getConcepts</b></code> Devuelve una lista con información de los
	 * <code><b>concepts</b></code> resultantes de la búsqueda. <br>
	 * Cada uno de los elementos de la lista contiene la <code><em>URI</em></code>
	 * del <code>concept</code>
	 * 
	 * <br>
	 * Se considerarán pertinentes el <code><b>concept</b></code> cuyo código sea
	 * igual al criterio de búsqueda y todos sus <code>concept</code> descendientes.
	 * 
	 * @return - List con la <em>URI</em> de los concepts pertinentes. <br>
	 *         - null si no hay concepts pertinentes.
	 * 
	 */
	@Override
	public List<String> getConcepts() {
		return lConcepts;
	}

	/**
	 * <code><b>getDatasets</b></code>
	 * 
	 * @return Mapa con información de los <code>dataset</code> resultantes de la
	 *         búsqueda. <br>
	 *         Si no se ha realizado ninguna búsqueda o no hay dataset pertinentes
	 *         devolverá el valor <code>null</code> <br>
	 *         Estructura de cada elemento del map: <br>
	 *         . <b>key</b>: valor del atributo ID del elemento
	 *         <code>dataset</code>con la cadena de la <code><em>URI</em></code>
	 *         <br>
	 *         . <b>value</b>: Mapa con la información a extraer del
	 *         <code>dataset</code>. Cada <code>key</code> tomará los valores
	 *         <em>title</em>, <em>description</em> o <em>theme</em>, y
	 *         <code>value</code> sus correspondientes valores.
	 * 
	 * @return - Map con información de los <code>dataset</code> resultantes de la
	 *         búsqueda. <br>
	 *         - null si no hay datasets pertinentes.
	 */
	@Override
	public Map<String, HashMap<String, String>> getDatasets() {
		return hDatasets;
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		System.out.println("Comenzando escaneo del archivo");
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
		System.out.println("Escaneo del archivo finalizado");
	}

	/**
	 * Comprueba, en cada comienzo de elemento su nombre y a partir de este dato
	 * determina qué acciones tomar en base al estado (valor de las flags). También
	 * se encarga de reiniciar el StringBuilder para empezar a leer el contenido del
	 * siguiente elemento.
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		sb = new StringBuilder();
		if (attributes.getLength() > 0) atributoID = attributes.getValue(0);
		switch (localName) {
		case "concept":
			if (!datasets) {
				if (!dentroDeConcept) {
					dentroDeConcept = true;
				} else if (encontrado) {
					lConcepts.add(atributoID);
				}
			} else if (lConcepts.contains(atributoID)) {
				introducirEntrada = true;
			}
			break;
		case "concepts":
			treeDepth++;
			break;
		case "datasets":
			datasets = true;
			break;
		case "dataset":
			idDataset = atributoID;
			break;
		}
	}

	/**
	 * Realiza las acciones necesarias al terminar cada elemento, habitualmente para
	 * guardar resultados o extraer el valor del elemento a la variable auxiliar
	 * "chars" desde el StringBuilder.
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		chars = sb.toString();
		switch (localName) {
		case "code":
			if (dentroDeConcept && chars.equals(codigo)) {
				encontrado = true;
				treeDepth = 0;
				lConcepts.add(atributoID);
			}
			break;
		case "label":
			if (encontrado && treeDepth == 0) {
				sNombreCategoria = chars;
			}
			break;
		case "concept":
			if (!datasets) {
				if (treeDepth == 0) {
					dentroDeConcept = false;
					encontrado = false;
				}
			} else if (introducirEntrada) {
				añadirEntradaMapa();
				introducirEntrada = false;
			}
			break;
		case "concepts":
			treeDepth--;
			break;
		case "datasets":
			datasets = false;
			break;
		case "title":
			title = chars;
			break;
		case "description":
			description = chars;
			break;
		case "theme":
			theme = chars;
			break;
		}
	}

	/**
	 * Añade los caracteres recibidos al StringBuilder de la clase.
	 */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		super.characters(ch, start, length);
		sb.append(ch, start, length);
	}

	/**
	 * Añade una entrada al mapa de datasets. Para ello, crea un mapa con las
	 * características del dataset y lo introduce como valor de la clave
	 * correspondiente al id del dataset.
	 */
	private void añadirEntradaMapa() {
		HashMap<String, String> mapaAuxiliar = new HashMap<String, String>();
		mapaAuxiliar.put("title", title);
		mapaAuxiliar.put("description", description);
		mapaAuxiliar.put("theme", theme);
		hDatasets.put(idDataset, mapaAuxiliar);
	}
}
