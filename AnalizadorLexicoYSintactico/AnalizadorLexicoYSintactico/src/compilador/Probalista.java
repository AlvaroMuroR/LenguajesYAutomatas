package compilador;

public class Probalista {

	public static void main(String[] args) {
		ListaDoble<Token> l = new ListaDoble<Token>();
		for (int i = 0; i <20; i++) {
			l.insertar(new Token("Hola", 2, 3));
			
		}
		NodoDoble<Token> Aux=l.getInicio();
		while(Aux !=null){
			System.out.println(Aux.dato);
			Aux=Aux.siguiente;
		}
	}

}
