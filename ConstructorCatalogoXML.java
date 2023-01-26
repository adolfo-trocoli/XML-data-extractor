package piat.opendatasearch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clase encargada de construir un fichero XML válido ante el esquema
 * "http://www.piat.dte.upm.es/practica3\" recibiendo como parámetros de entrada
 * los resultados del análisis del catálogo perteneciente al esquema
 * "http://www.piat.upm.es/catalogo".
 * 
 * @author Adolfo Trocolí Naranjo, 09218364X, a.trocoli@alumnos.upm.es
 *
 */
public class ConstructorCatalogoXML {

	/**
	 * Construye el fichero XML.
	 * 
	 * @param ficheroSalida    Fichero de salida.
	 * @param sNombreCategoria Nombre de la categoría buscada.
	 * @param lConcepts        Sub-conceptos del concept encontrado.
	 * @param hDatasets        Mapa de datasets relacionados con el concept
	 *                         encontrado.
	 * @param query            Código del concept.
	 * @param mResources 
	 * @throws FileNotFoundException Si el fichero de salida no existe y no puede
	 *                               ser creado.
	 */
	public static void construir(String ficheroSalida, String sNombreCategoria, List<String> lConcepts,
			Map<String, HashMap<String, String>> hDatasets, final String query, Map<String, List<Map<String, String>>> mResources) throws FileNotFoundException {
		final String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<searchResults xmlns=\"http://www.piat.dte.upm.es/ResultadosBusquedaP4\"\n"
				+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ "xsi:schemaLocation=\"http://www.piat.dte.upm.es/ResultadosBusquedaP4 ResultadosBusquedaP4.xsd \">\n";

		PrintWriter pw = new PrintWriter(new File(ficheroSalida));
		pw.println(header);
		pw.println("\t<summary>");
		pw.println("\t\t<query>" + query + "</query>");
		pw.println("\t\t<numConcepts>" + lConcepts.size() + "</numConcepts>");
		pw.println("\t\t<numDatasets>" + hDatasets.size() + "</numDatasets>");
		pw.println("\t</summary>");
		pw.println("\t<results>");
		pw.println("\t\t<concepts>");
		for (String concept : lConcepts) { // Imprime la lista de concepts
			pw.println("\t\t\t<concept id=\"" + concept + "\"/>");
		}
		pw.println("\t\t</concepts>");
		if (hDatasets.size() > 0) {
			printDatasets(pw, hDatasets);
		}
		if (mResources.size() > 0) {
			printResources(pw, mResources);
		}
		pw.println("\t</results>");
		pw.println("</searchResults>");
		pw.close();
	}

	private static void printResources(PrintWriter pw, Map<String, List<Map<String, String>>> mResources) {
		String resourceID;
		List<Map<String, String>> listaElementos;
		pw.println("\t\t<resources>");
		for(Map.Entry<String, List<Map<String, String>>> entradaResources : mResources.entrySet()) {
			resourceID = entradaResources.getKey();
			listaElementos = entradaResources.getValue();
			printResource(pw, resourceID, listaElementos);
		}
		pw.println("\t\t</resources>");		
	}

	private static void printResource(PrintWriter pw, String resourceID, List<Map<String, String>> listaElementos) {
		for(Map<String, String> element : listaElementos) {
			pw.println("\t\t\t<resource id=\"" + resourceID + "\">");
			pw.println("\t\t\t\t<concept id=\"" + element.get("concept") + "\"/>");
			if(!element.get("link").isBlank())
				pw.println("\t\t\t\t<link> <![CDATA[" + element.get("link") + "]]> </link>");
			else
				pw.println("\t\t\t\t<link>" + element.get("relation") + "</link>");
			pw.println("\t\t\t\t<title>" + element.get("title") + "</title>");
			pw.println("\t\t\t\t<location>");
			pw.println("\t\t\t\t\t<eventLocation>" + element.get("eventLocation") + "</eventLocation>");
			pw.println("\t\t\t\t\t<area>" + element.get("area") + "</area>");
			pw.println("\t\t\t\t\t<timetable>");
			pw.println("\t\t\t\t\t\t<start>" + element.get("dtstart") + "</start>");
			pw.println("\t\t\t\t\t\t<end>" + element.get("dtend") + "</end>");
			pw.println("\t\t\t\t\t</timetable>");
			pw.println("\t\t\t\t\t<georeference>" + element.get("latitude") + " " + element.get("longitude") + " " + "</georeference>");
			pw.println("\t\t\t\t</location>");
			pw.println("\t\t\t\t<organization>");
			pw.println("\t\t\t\t\t<accesibility>" + element.get("accesibility") + "</accesibility>");
			pw.println("\t\t\t\t\t<organizationName>" + element.get("organization-name") + "</organizationName>");
			pw.println("\t\t\t\t</organization>");
			pw.println("\t\t\t\t<description>" + element.get("description") + "</description>");
			pw.println("\t\t\t</resource>");
		}
	}

	private static void printDatasets(PrintWriter pw, Map<String, HashMap<String, String>> hDatasets) {
		pw.println("\t\t<datasets>");
		for (Map.Entry<String, HashMap<String, String>> entradaDataset : hDatasets.entrySet()) { // Imprime los
																									// datasets
			pw.println("\t\t\t<dataset id=\"" + entradaDataset.getKey() + "\">");
			pw.println("\t\t\t\t<title>" + entradaDataset.getValue().get("title") + "</title>");
			pw.println("\t\t\t\t<description>" + entradaDataset.getValue().get("description") + "</description>");
			pw.println("\t\t\t\t<theme>" + entradaDataset.getValue().get("theme") + "</theme>");
			pw.println("\t\t\t</dataset>");
		}
		pw.println("\t\t</datasets>");
	}

}
