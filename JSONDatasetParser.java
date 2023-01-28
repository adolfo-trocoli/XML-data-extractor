import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.stream.JsonReader;

/**
 * 
 * 
 * @author Adolfo Trocolí Naranjo, 09218364X, a.trocoli@alumnos.upm.es
 *
 */
public class JSONDatasetParser implements Runnable {

	private String fichero;
	private List<String> lConcepts;
	private Map<String, List<Map<String, String>>> mDatasetConcepts;
	private String nombreHilo;

	private JsonReader reader;

	public JSONDatasetParser(String fichero, List<String> lConcepts,
			Map<String, List<Map<String, String>>> mDatasetConcepts) {
		this.fichero = fichero;
		this.lConcepts = lConcepts;
		this.mDatasetConcepts = mDatasetConcepts;
	}

	@Override
	public void run() {
		Thread.currentThread().setName("JSON " + fichero);
		nombreHilo = "[" + Thread.currentThread().getName() + "] ";
		try {
			InputStreamReader url = abrirURL();
			if (url == null) return;
			List<Map<String, String>> graphs = leerFicheroJSON(url);
			mDatasetConcepts.put(fichero, graphs);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private InputStreamReader abrirURL() {
		System.out.println(nombreHilo + "Empezar a descargar de internet el JSON");
		try {
			return new InputStreamReader(new URL(fichero).openStream(), "UTF-8");
		} catch (FileNotFoundException e) {
			System.err.println(nombreHilo + "El fichero no existe. Ignorándolo");
		} catch (IOException e) {
			System.err.println(nombreHilo + "Hubo un problema al abrir el fichero. Ignorándolo" + e);
		}
		return null;
	}

	private List<Map<String, String>> leerFicheroJSON(InputStreamReader input) throws IOException {
		reader = new JsonReader(input);
		try {
			if (!colocarPunteroEnGraph()) return null;
			return leerGraph();
		} finally {
			reader.close();
		}
	}

	private List<Map<String, String>> leerGraph() throws IOException {
		List<Map<String, String>> lista = new ArrayList<Map<String, String>>();
		Map<String, String> mapaResource;
		reader.beginArray(); // Nunca se llamará a endArray porque cuando se vaya a terminar el objeto
								// "@graphs" ya no será necesario seguir leyendo
		while (reader.hasNext() && (lista.size() < 5)) {
			mapaResource = leerResource();
			if (mapaResource != null) {
				System.out.println(mapaResource.get("concept"));
				lista.add(mapaResource);
			}
		}
		return lista;
	}

	private boolean colocarPunteroEnGraph() throws IOException {
		reader.beginObject(); // Empieza a leer el documento
		reader.nextName(); // Consume el nombre "@context"
		reader.skipValue(); // Salta el valor de "@context", colocándose así en "@graphs", el siguiente objeto
		return (reader.nextName().equals("@graph"));
	}

	private Map<String, String> leerResource() throws IOException {
		String concept = "";
		String link = "";
		String relation = "";
		String title = "";
		String eventLocation = "";
		String area = "";
		String description = "";
		String dtstart = "";
		String dtend = "";
		String latitude = "";
		String longitude = "";
		String accesibility = "";
		String organization_name = "";
		String propertyName = ""; // Se usará para iterar sobre los nombres de las propiedades de los objetos del
									// resource.
		boolean interestingResource = false;

		reader.beginObject();
		while (reader.hasNext()) {
			propertyName = reader.nextName();
			switch (propertyName) {
			case "@type":
				concept = reader.nextString();
				if (lConcepts.contains(concept)) interestingResource = true;
				break;
			case "link":
				link = reader.nextString();
				break;
			case "description":
				description = reader.nextString();
				break;
			case "title":
				title = reader.nextString();
				break;
			case "relation":
				relation = leerRelation();
				break;
			case "event-location":
				eventLocation = reader.nextString();
				break;
			case "address":
				area = leerIDAreaDeAddress();
				break;
			case "dtstart":
				dtstart = reader.nextString();
				break;
			case "dtend":
				dtend = reader.nextString();
				break;
			case "organization":
				String[] organization = leerOrganization();
				organization_name = organization[0];
				accesibility = organization[1];
				break;
			case "location":
				String[] location = leerLocation();
				longitude = location[0];
				latitude = location[1];
				break;
			default:
				reader.skipValue();
			}
		}
		reader.endObject();
		if (interestingResource) {
			return buildMap(concept, link, relation, title, eventLocation, area, description, dtstart, dtend, latitude,
					longitude, accesibility, organization_name);
		} else
			return null;
	}

	private Map<String, String> buildMap(String concept, String link, String relation, String title,
			String eventLocation, String area, String description, String dtstart, String dtend, String latitude,
			String longitude, String accesibility, String organization_name) {
		Map<String, String> mapa = new HashMap<String, String>();
		mapa.put("concept", concept);
		mapa.put("link", link);
		mapa.put("relation", relation);
		mapa.put("title", title);
		mapa.put("eventLocation", eventLocation);
		mapa.put("area", area);
		mapa.put("description", description);
		mapa.put("dtstart", dtstart);
		mapa.put("dtend", dtend);
		mapa.put("latitude", latitude);
		mapa.put("longitude", longitude);
		mapa.put("accesibility", accesibility);
		mapa.put("organization-name", organization_name);
		return mapa;
	}

	private String[] leerLocation() throws IOException {
		String[] location = new String[2];
		String propertyName;
		reader.beginObject();
		while (reader.hasNext()) {
			propertyName = reader.nextName();
			switch (propertyName) {
			case "longitude":
				location[0] = reader.nextString();
				break;
			case "latitude":
				location[1] = reader.nextString();
				break;
			}
		}
		reader.endObject();
		return location;
	}

	private String[] leerOrganization() throws IOException {
		String[] organization = new String[2];
		String propertyName;
		reader.beginObject();
		while (reader.hasNext()) {
			propertyName = reader.nextName();
			switch (propertyName) {
			case "organization-name":
				organization[0] = reader.nextString();
				break;
			case "accesibility":
				organization[1] = reader.nextString();
				break;
			}
		}
		reader.endObject();
		return organization;
	}

	private String leerIDAreaDeAddress() throws IOException {
		String areaID = null;
		String propertyName;
		reader.beginObject();
		while (reader.hasNext()) {
			propertyName = reader.nextName();
			if (propertyName.equals("area")) areaID = leerIDArea();
			else
				reader.skipValue();
		}
		reader.endObject();
		return areaID;
	}

	private String leerIDArea() throws IOException {
		String areaID = null;
		String propertyName;
		reader.beginObject();
		while (reader.hasNext()) {
			propertyName = reader.nextName();
			if (propertyName.equals("@id")) areaID = reader.nextString();
			else
				reader.skipValue();
		}
		reader.endObject();
		return areaID;
	}

	private String leerRelation() throws IOException {
		String relationID = null;
		String propertyName;
		reader.beginObject();
		while (reader.hasNext()) {
			propertyName = reader.nextName();
			if (propertyName.equals("@id")) relationID = reader.nextString();
			else
				reader.skipValue();
		}
		reader.endObject();
		return relationID;
	}
}
