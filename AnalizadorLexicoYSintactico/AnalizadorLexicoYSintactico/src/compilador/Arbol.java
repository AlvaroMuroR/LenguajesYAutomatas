package compilador;

import java.util.*;


public class Arbol {

	String operador;
	String arg1;
	String arg2;
	String resultado;

	
	public Arbol(String Operador, String Arg1, String Arg2, String Resultado) {
		super();
		operador = Operador;
		arg1 = Arg1;
		arg2 = Arg2;
		resultado=Resultado;
		
	}


	public String getOperador() {
		return operador;
	}


	public void setOperador(String operador) {
		this.operador = operador;
	}


	public String getArg1() {
		return arg1;
	}


	public void setArg1(String arg1) {
		this.arg1 = arg1;
	}


	public String getArg2() {
		return arg2;
	}


	public void setArg2(String arg2) {
		this.arg2 = arg2;
	}


	public String getResultado() {
		return resultado;
	}


	public void setResultado(String resultado) {
		this.resultado = resultado;
	}


	
	

	

}
