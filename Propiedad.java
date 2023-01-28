/**
 * Esta clase define una propiedad equivalente a "nombre":"valor" en JSON
 */
public class Propiedad {
	private final String nombre;
	private final String valor;

	public Propiedad(String nombre, String valor) {
		this.nombre = nombre;
		this.valor = valor;
	}

	public String getNombre() {
		return nombre;
	}

	public String getValor() {
		return valor;
	}
	
}
