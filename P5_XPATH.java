import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

/**
 * @author Adolfo Trocolí Naranjo, 09218364X, a.trocoli@alumnos.upm.es
 * @version 3.0
 */

public class P5_XPATH {

	/**
	 * Clase principal de la aplicación de extracción de información del Portal de Datos Abiertos del Ayuntamiento de
	 * Madrid
	 */

	public static void main(String[] args) {
		
		final String ficheroEntrada = args[0];
		final String codigo = args[1];
		final String ficheroSalidaXML = args[2];
		final String ficheroSalidaJSON = args[3];

		// Verificar número, formato y permisos de los argumentos.
		verificarArgs(args);
		
		try {
			ManejadorXML manejadorXML = crearManejador(ficheroEntrada, codigo);
			String sNombreCategoria = manejadorXML.getLabel(); 
			List<String> lConcepts = manejadorXML.getConcepts(); 
			Map<String, HashMap<String, String>> hDatasets = manejadorXML.getDatasets();
			Map<String, List<Map<String, String>>> mDatasetConcepts = getDatasetConcepts(lConcepts, hDatasets);
			ConstructorCatalogoXML.construir(ficheroSalidaXML, sNombreCategoria, lConcepts, hDatasets, codigo,
					mDatasetConcepts);
			XPathProcess parser = new XPathProcess(ficheroSalidaXML);
			ConstructorDocumentoJSON.construir(parser.evaluar(), ficheroSalidaJSON);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	private static Map<String, List<Map<String, String>>> getDatasetConcepts(List<String> lConcepts,
			Map<String, HashMap<String, String>> hDatasets) throws InterruptedException {
		final int coreCount = Runtime.getRuntime().availableProcessors();
		final ExecutorService executor = Executors.newFixedThreadPool(coreCount);
		Map<String, List<Map<String, String>>> mDatasetConcepts = new ConcurrentHashMap<String, List<Map<String, String>>>();
		for (Map.Entry<String, HashMap<String, String>> entradaFicheroJSON : hDatasets.entrySet()) {
			executor.execute(new JSONDatasetParser(entradaFicheroJSON.getKey(), lConcepts, mDatasetConcepts));
		}
		executor.shutdown();
		return mDatasetConcepts;
	}

	/**
	 * Llama a todos los submétodos pertinentes de verificación de los argumentos iniciales.
	 * 
	 * @param args Argumentos de la aplicación.
	 */
	private static void verificarArgs(String[] args) {
		verificarNumArgs(args);
		verificarFormatoArgs(args);
		verificarPermisosArgs(args);
	}

	/**
	 * Crea el manejador conociendo el fichero de entrada y el codigo a buscar.
	 * 
	 * @param ficheroEntrada Fichero de entrada.
	 * @param codigo         Codigo a buscar.
	 * @return Manejador con los resultados de escanear el archivo de entrada.
	 */
	private static ManejadorXML crearManejador(final String ficheroEntrada, final String codigo)
			throws ParserConfigurationException, SAXException, IOException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		SAXParser saxParser = factory.newSAXParser();
		ManejadorXML manejadorXML = new ManejadorXML(codigo);
		saxParser.parse(new File(ficheroEntrada), manejadorXML);
		return manejadorXML;
	}

	/**
	 * Verifica que los permisos de los argumentos sean correctos y envía un mensaje de error en caso contrario
	 */
	private static void verificarPermisosArgs(String[] args) {
		File archivoLectura = new File(args[0]);
		File archivoEscrituraXML = new File(args[2]);
		File archivoEscrituraJSON = new File(args[3]);
		if (!archivoLectura.canRead() || !archivoEscrituraXML.canWrite() || !archivoEscrituraJSON.canWrite()) {
			String mensaje = "ERROR: Argumentos incorrectos.";
			mensaje += "Error de permisos, he recibido estos argumentos: " + Arrays.asList(args).toString() + "\n";
			mostrarUso(mensaje);
			System.exit(1);
		}
	}

	/**
	 * Verifica que el formato de los argumentos sea correcto y envía un mensaje de error en caso contrario
	 */
	private static void verificarFormatoArgs(String[] args) {
		String regexXML = ".*\\.xml\\z";
		String regexJSON = ".*\\.json\\z";
		String regexCriterio = "^\\d{3,4}(-[0-9A-Z]{3,8})?\\z";
		if (!args[0].matches(regexXML) || !args[1].matches(regexCriterio) || !args[2].matches(regexXML)
				|| !args[3].matches(regexJSON)) {
			String mensaje = "ERROR: Argumentos incorrectos.";
			mensaje += "Error de formato, he recibido estos argumentos: " + Arrays.asList(args).toString() + "\n";
			mostrarUso(mensaje);
			System.exit(1);
		}
	}

	/**
	 * Verifica que el número de argumentos sea correcto y envía un mensaje de error en caso contrario
	 */
	private static void verificarNumArgs(String[] args) {
		if (args.length != 4) {
			String mensaje = "ERROR: Argumentos incorrectos.";
			if (args.length > 0) mensaje += "Error en el número de argumentos, he recibido estos argumentos: "
					+ Arrays.asList(args).toString() + "\n";
			mostrarUso(mensaje);
			System.exit(1);
		}
	}

	/**
	 * Muestra mensaje de los argumentos esperados por la aplicación. Deberá invocase en la fase de validación ante la
	 * detección de algún fallo
	 *
	 * @param mensaje Mensaje adicional informativo (null si no se desea)
	 */
	private static void mostrarUso(String mensaje) {
		Class<? extends Object> thisClass = new Object() {
		}.getClass();

		if (mensaje != null) System.err.println(mensaje + "\n");
		System.err.println("Uso: " + thisClass.getEnclosingClass().getCanonicalName()
				+ " <ficheroCatalogo> <códigoCategoría> <ficheroSalida>\n" + "donde:\n"
				+ "\t ficheroCatalogo:\t path al fichero XML con el cat�logo de datos\n"
				+ "\t códigoCategor�a:\t código de la categoría de la que se desea obtener datos\n"
				+ "\t ficheroSalidaXML:\t\t nombre del fichero XML de salida\n"
				+ "\t ficheroSalidaJSON:\t\t nombre del fichero JSON de salida\n");
	}
}
