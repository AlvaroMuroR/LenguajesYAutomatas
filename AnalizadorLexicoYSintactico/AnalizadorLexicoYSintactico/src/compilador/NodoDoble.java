package compilador;
public class NodoDoble<E> {
	E dato;
	NodoDoble<E> siguiente;
	NodoDoble<E> anterior;
	public NodoDoble(E dato) {
		this.dato=dato;
	}
	
}
