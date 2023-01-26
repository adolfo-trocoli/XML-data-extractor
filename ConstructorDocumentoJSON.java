package piat.opendatasearch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;

/**
 * Clase encargada de construir un documento JSON a partir los datos facilitados por el procesador de XML.
 * 
 * @author Adolfo Trocolí Naranjo, 09218364X, a.trocoli@alumnos.upm.es
 *
 */
public class ConstructorDocumentoJSON {

	private static PrintWriter pw;

	/**
	 * Permite construir un documento JSON que indica la query realizada al portal de información, el número de datasets
	 * que se han recabado con esta query, la información acerca del número de recursos asociados a cada uno de los
	 * datasets, y los títulos de estos recursos.
	 * 
	 * @param propiedades       Lista de propiedades necesarias para construir el documento JSON.
	 * @param ficheroSalidaJSON URI del fichero de salida.
	 * @throws FileNotFoundException
	 */
	public static void construir(List<Propiedad> propiedades, String ficheroSalidaJSON) throws FileNotFoundException {

		pw = new PrintWriter(new File(ficheroSalidaJSON));
		// Indice de la primera propiedad "title", nos sirve para ver donde cortar la lista de propiedades
		int indice = indexOfTitle(propiedades);

		Propiedad query = propiedades.get(0);
		Propiedad numDataset = propiedades.get(1);
		List<Propiedad> infDatasets = propiedades.subList(2, indice);
		List<Propiedad> titles = propiedades.subList(indice, propiedades.size());

		beginDocument();
		printProperty(query);
		printProperty(numDataset);
		printInfDatasets(infDatasets);
		printTitles(titles);
		endDocument();
	}

	/**
	 * Devuelve el índice del primero objeto de la lista de propiedades que cumple que su nombre es "title". Sirve para
	 * conocer el punto donde terminan las propiedades relacionadas dcon "infDatasets" y donde empiezan las relacionadas
	 * con "titles".
	 * 
	 * @param propiedades Lista de propiedades.
	 * @return Posicion del primer objeto Propiedad cuyo nombre es igual a "title".
	 */
	private static int indexOfTitle(List<Propiedad> propiedades) {
		 Optional<Propiedad> posiblePropiedad = propiedades.stream() // Es el primer objeto Propiedad que devuelve "title" al llamar a getNombre();
				.filter(p -> p.getNombre().equals("title"))
				.findFirst();
		 if (posiblePropiedad.isPresent()) return propiedades.indexOf(posiblePropiedad.get());
		 else return propiedades.size();
	}

	/**
	 * Imprime los títulos en formato JSON.
	 * 
	 * @param titles Títulos a imprimir.
	 */
	private static void printTitles(List<Propiedad> propiedades) {
		pw.println("\t\"titles\": [");
		String line;
		for (int i = 0; i < propiedades.size(); i++) {
			line = "\t\t{ \"title\": \"" + propiedades.get(i).getValor() + "\" }";
			if (i != propiedades.size() - 1) line += ",";
			pw.println(line);
		}
		pw.println("\t]");
	}

	/**
	 * Imprime información acerca de cuantos recursos se han recabado por cada dataset.
	 * 
	 * @param ids  Array de String con los nombres de los datasets.
	 * @param nums Array con los valores numéricos correspondientes por índice.
	 */
	private static void printInfDatasets(List<Propiedad> propiedades) {
		pw.println("\t\"infDatasets\": [{");
		Propiedad propiedad;
		for (int i = 0; i < propiedades.size(); i++) {
			propiedad = propiedades.get(i);
			if (i != 0) pw.println("\t\t{");
			pw.println("\t\t\t\"id\": \"" + propiedad.getNombre() + "\",");
			pw.println("\t\t\t\"num\": \"" + propiedad.getValor() + "\"");
			if (i != propiedades.size() - 1) pw.println("\t\t},");
			else pw.println("\t\t}");
		}
		pw.println("\t],");
	}

	/**
	 * Imprime una propiedad en JSON.
	 * 
	 * @param propiedad Propiedad a imprimir.
	 */
	private static void printProperty(Propiedad propiedad) {
		pw.println("\t\"" + propiedad.getNombre() + "\": \"" + propiedad.getValor() + "\",");
	}

	private static void beginDocument() {
		pw.println("{");
	}

	private static void endDocument() {
		pw.println("}");
		pw.close();
	}
}
