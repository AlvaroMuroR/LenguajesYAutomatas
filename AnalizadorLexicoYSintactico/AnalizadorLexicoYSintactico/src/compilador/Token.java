package compilador;

public class Token 
{
	private int tipo;
	final static int MODIFICADOR=0; 
	final static int PALABRA_RESERVADA=1;
	final static int TIPO_DATO=2;
	final static int SIMBOLO=3;
	final static int OPERADOR_LOGICO=4;
	final static int OPERADOR_ARITMETICO=5;
	final static int CONSTANTE=6;
	final static int IDENTIFICADOR=7;
	final static int CLASE=8;
	private final static String types[]= 
	{
	"Modificador",
	"Palabra resevada",
	"Tipo de dato",
	"Simbolo",
	"Operador logico",
	"Operador aritmetico",
	"Constante",
	"Identificador",
	"Declaracion de clase"
	};
	private String valor;
	private int linea;
	public Token(String valor, int tipo, int linea) {
		this.tipo=tipo;
		this.valor=valor;
		this.linea=linea;
	}
	public int getTipo() {
		return tipo;
	}
	public String getValor() {
		return valor;
	}
	public int getLinea() {
		return linea;
	}
	public void setValor(String valor) {
		this.valor = valor;
	}
	public String toString() {
		return "Token encontrado.... " +types[tipo]+": "+valor;
	}
}
