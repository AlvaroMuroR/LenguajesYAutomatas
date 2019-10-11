package compilador;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
public class Analisis
{
	int renglon=1;
	ArrayList<String> impresion; //para la salida
	ArrayList<Identificador> identi = new ArrayList<Identificador>();
	ArrayList<String> aux= new ArrayList<String>();
	ListaDoble<Token> tokens;
	final Token vacio=new Token("", 9,0);
	boolean bandera=true;

	public ArrayList<Identificador> getIdenti() {
		return identi;
	}
	public Analisis(String ruta) {//Recibe el nombre del archivo de texto
		analisaCodigo(ruta);
		if(bandera) {
			impresion.add("No hay errores lexicos");
			analisisSintactio(tokens.getInicio());
		}
		if(impresion.get(impresion.size()-1).equals("No hay errores lexicos"))

			impresion.add("No hay errores sintacticos");
		analisisSemantico(tokens.getInicio());


	}
	public void analisaCodigo(String ruta) {
		String linea="", token="";
		StringTokenizer tokenizer;
		try{
			FileReader file = new FileReader(ruta);
			BufferedReader archivoEntrada = new BufferedReader(file);
			linea = archivoEntrada.readLine();
			impresion=new ArrayList<String>();
			tokens = new ListaDoble<Token>();
			while (linea != null){
				linea = separaDelimitadores(linea);
				tokenizer = new StringTokenizer(linea);
				while(tokenizer.hasMoreTokens()) {
					token = tokenizer.nextToken();
					analisisLexico(token);
				}
				linea=archivoEntrada.readLine();
				renglon++;
			}
			archivoEntrada.close();
		}catch(IOException e) {
			JOptionPane.showMessageDialog(null,"No se encontro el archivo favor de checar la ruta","Alerta",JOptionPane.ERROR_MESSAGE);
		}
	}
	// La neta le falta hacer mantenimiento pero funciona
	public Token analisisSintactio(NodoDoble<Token> nodo) {
		Token  to;
		if(nodo!=null) // si no llego al ultimo de la lista
		{
			to =  nodo.dato;
			switch (to.getTipo()) // un switch para validar la estructura
			{
			case Token.MODIFICADOR:
				int sig=nodo.siguiente.dato.getTipo();
				// aqui se valida que sea 'public int' o 'public class' 
				if(sig!=Token.TIPO_DATO && sig!=Token.CLASE)// si lo que sigue 
					impresion.add("Error sinatactico en la linea "+to.getLinea()+" se esparaba un tipo de dato");
				break;
			case Token.IDENTIFICADOR:
				// lo que puede seguir despues de un idetificador
				if(!(Arrays.asList("{","=",";").contains(nodo.siguiente.dato.getValor()))) 
					impresion.add("Error sinatactico en la linea "+to.getLinea()+" se esparaba un simbolo");
				else
					if(nodo.anterior.dato.getValor().equals("class")) // se encontro la declaracion de la clase
					{
						identi.add( new Identificador(to.getValor(), " ", "class","Local",nodo.dato.getLinea()));
					}
				break;
				// Estos dos entran en el mismo caso
			case Token.TIPO_DATO:
			case Token.CLASE:
				// si lo anterior fue modificador
				if (nodo.anterior!=null) 
					if(cuenta(nodo.siguiente.dato.getValor())>=1) {

					}else {
						if(nodo.anterior.dato.getTipo()==Token.MODIFICADOR) {
							if(nodo.siguiente.dato.getTipo()!=Token.IDENTIFICADOR) 
								impresion.add("Error sinatactico en la linea "+to.getLinea()+" se esparaba un identificador");
						}else
							impresion.add("Error sinatactico en la linea "+to.getLinea()+" se esperaba un modificador");
					}
				break;
			case Token.SIMBOLO:
				// Verificar que el mismo numero de parentesis y llaves que abren sean lo mismo que los que cierran
				if(to.getValor().equals("}")) 
				{
					if(cuenta("{")!=cuenta("}"))
						impresion.add("Error sinatactico en la linea "+to.getLinea()+ " falta un {");
				}else if(to.getValor().equals("{")) {
					if(cuenta("{")!=cuenta("}"))
						impresion.add("Error sinatactico en la linea "+to.getLinea()+ " falta un }");

				}
				else if(to.getValor().equals("(")) {
					if(cuenta("(")!=cuenta(")"))
						impresion.add("Error sinatactico en la linea "+to.getLinea()+ " falta un )");
					else
					{
						if(!(nodo.anterior.dato.getValor().equals("if")&&nodo.siguiente.dato.getTipo()==Token.CONSTANTE)) {
							impresion.add("Error sinatactico en la linea "+to.getLinea()+ " se esperaba un valor");
						}
					}
				}else if(to.getValor().equals(")")) {
					if(cuenta("(")!=cuenta(")"))
						impresion.add("Error sinatactico en la linea "+to.getLinea()+ " falta un (");
				}
				// verificar la asignacion
				else if(to.getValor().equals("=")){
					if(nodo.anterior.dato.getTipo()==Token.IDENTIFICADOR) {
						if(nodo.siguiente.dato.getTipo()!=Token.CONSTANTE)
							impresion.add("Error sinatactico en la linea "+to.getLinea()+ " se esperaba una constante");
						else {
							if(nodo.anterior.anterior.dato.getTipo()==Token.TIPO_DATO)
								identi.add(new Identificador(nodo.anterior.dato.getValor(),nodo.siguiente.dato.getValor(),nodo.anterior.anterior.dato.getValor(),"Local",nodo.dato.getLinea()));
							else
								//Si el valor se repite
								if(cuenta(nodo.anterior.dato.getValor())>=2){


								}else {
									//Si no encuentra una variable declarada
									if(cuenta(nodo.anterior.dato.getValor())<2) {
										impresion.add("Error sinatactico en linea "+to.getLinea()+ " variable no declarada");
									}else {
										impresion.add("Error sinatactico en linea "+to.getLinea()+ " se esperaba un tipo de dato");
									}
								}
						}
					}else
						impresion.add("Error sinatactico en linea "+to.getLinea()+ " se esperaba un identificador");
				}
				break;

			case Token.CONSTANTE:
				if(nodo.anterior.dato.getValor().equals("="))
					if(nodo.siguiente.dato.getTipo()!=Token.OPERADOR_ARITMETICO&&nodo.siguiente.dato.getTipo()!=Token.CONSTANTE&&!nodo.siguiente.dato.getValor().equals(";"))
						impresion.add("Error sinatactico en linea "+to.getLinea()+ " asignacion no valida");
				break;
			case Token.PALABRA_RESERVADA:
				// verificar esructura de if
				if(to.getValor().equals("if"))
				{
					if(!nodo.siguiente.dato.getValor().equals("(")) {
						impresion.add("Error sinatactico en linea "+to.getLinea()+ " se esperaba un (");
					}
				}
				else 
				{
					// si es un else, buscar en los anteriores y si no hay un if ocurrira un error
					NodoDoble<Token> aux = nodo.anterior;
					boolean bandera=false;
					while(aux!=null&&!bandera) {
						if(aux.dato.getValor().equals("if"))
							bandera=true;
						aux =aux.anterior;
					}
					if(!bandera)
						impresion.add("Error sinatactico en linea "+to.getLinea()+ " else no valido");
				}
				break;
			case Token.OPERADOR_LOGICO:
				// verificar que sea  'numero' + 'operador' + 'numero' 
				if(nodo.anterior.dato.getTipo()!=Token.CONSTANTE) 
					impresion.add("Error sinatactico en linea "+to.getLinea()+ " se esperaba una constante");
				if(nodo.siguiente.dato.getTipo()!=Token.CONSTANTE)
					impresion.add("Error sinatactico en linea "+to.getLinea()+ " se esperaba una constante");
				break;

			case Token.OPERADOR_ARITMETICO:
				if(nodo.anterior.dato.getTipo()!=Token.CONSTANTE) 
					impresion.add("Error sinatactico en linea "+to.getLinea()+ " se esperaba una constante");
				if(nodo.siguiente.dato.getTipo()!=Token.CONSTANTE)
					impresion.add("Error sinatactico en linea "+to.getLinea()+ " se esperaba una constante");

				String aux1="", aux2="";
				aux1=TipoDato(nodo.anterior.dato.getValor());
				aux2=TipoDato(nodo.siguiente.dato.getValor());
				if(!aux1.equals(aux2))
					impresion.add("No se puede pude realizar la operacion en la linea "+to.getLinea());
				break;
			}

			analisisSintactio(nodo.siguiente); // buscar el siguiente de forma recursiva

			return to;
		}
		return  vacio;// para no regresar null y evitar null pointer
	}
	public void analisisLexico(String token) {
		int tipo=0;
		//Se usan listas con los tipos de token
		// Esto se asemeja a un in en base de datos 
		//Ejemplo select * from Clientes where Edad in (18,17,21,44)
		if(Arrays.asList("public","static","private").contains(token)) 
			tipo = Token.MODIFICADOR;
		else if(Arrays.asList("if","else").contains(token)) 
			tipo = Token.PALABRA_RESERVADA;
		else if(Arrays.asList("int","double","char","float","boolean").contains(token))
			tipo = Token.TIPO_DATO;
		else if(Arrays.asList("(",")","{","}","=",";").contains(token))
			tipo = Token.SIMBOLO;
		else if(Arrays.asList("<","<=",">",">=","==","!=").contains(token))
			tipo = Token.OPERADOR_LOGICO;
		else if(Arrays.asList("+","-","*","/").contains(token))
			tipo = Token.OPERADOR_ARITMETICO;
		else if(Arrays.asList("true","false").contains(token)||Pattern.matches("^\\d+$",token)||Pattern.matches("[0-9]+.[0-9]+",token)  )  
			tipo = Token.CONSTANTE;
		else if(token.equals("class")) 
			tipo =Token.CLASE;
		else {
			//Cadenas validas
			Pattern pat = Pattern.compile("^[a-zA-Z]+$");//Expresiones Regulares
			Matcher mat = pat.matcher(token);
			if(mat.find()) 
				tipo = Token.IDENTIFICADOR;
			else {
				impresion.add("Error en la linea "+renglon+" token "+token);
				bandera = false;
				return;
			}
		}
		tokens.insertar(new Token(token,tipo,renglon));
		impresion.add(new Token(token,tipo,renglon).toString());
	}
	//Analisis Semantico
	public Token analisisSemantico(NodoDoble<Token> nodo) {
		Token to;
		String cadena=null;
		if(nodo!=null) // si no llego al ultimo de la lista
		{
			to =  nodo.dato;
			//Recorrido de la tabla de simbolos
			for(int i=0;i<identi.size();i++) {
				int cont=0;
				boolean repetido = false;
				String bandera= "";
				bandera=identi.get(i).getNombre();
				for(int j=0;j<aux.size();j++) {
					//Para saber si un identificador esta repetido
					if(aux.get(j).contains(identi.get(i).getNombre())) {
						repetido=true;
					}
				}

				for(int k=0; k<identi.size();k++) {
					if(identi.get(k).getNombre().contains(bandera)&& !repetido) {
						cont++;
						if(cont>1) {
							impresion.add("Variable repetida en la linea: "+identi.get(k).getPosicion());
							aux.add(identi.get(k).getNombre());
						}
					}
				}
				repetido = false;

				//Metodos para validar los tipos de datos
				if(identi.get(i).getTipo().contains("int")) {
					cadena=identi.get(i).getValor();
					if(!isNumeric(cadena)) {
						impresion.add("Error semantico, se esperaba un valor entero en la linea: "+identi.get(i).getPosicion());
					}

				}

				else if(identi.get(i).getTipo().contains("float")) {
					cadena=identi.get(i).getValor();
					if(!isFloat(cadena)) {
						impresion.add("Error semantico, se esperaba valor flotante en la linea: "+identi.get(i).getPosicion());
					}

				}

				else if(identi.get(i).getTipo().contains("double")) {
					cadena=identi.get(i).getValor();
					if(!isDouble(cadena)) {
						impresion.add("Error semantico, se esperaba un valor double en la linea: "+identi.get(i).getPosicion());
					}

				}
				else if(identi.get(i).getTipo().contains("boolean")) {
					cadena=identi.get(i).getValor();
					if(!isBoolean(cadena)) {
						impresion.add("Error semantico, se esperaba un valor boolean en la linea: "+identi.get(i).getPosicion());
					}

				}

			}

			return to;
		}
		return vacio;
	}
	//Metodos para validar los valores por medio de una cadena
	public static boolean isNumeric(String cadena) {
		try {
			Integer.parseInt(cadena);
			return true;

		}catch(NumberFormatException nfe) {
			return false;
		}
	}

	public static boolean isFloat(String cadena) {
		try {
			Float.parseFloat(cadena);
			return true;

		}catch(NumberFormatException nfe) {
			return false;
		}
	}



	public static boolean isDouble(String cadena) {
		try {
			Double.parseDouble(cadena);
			return true;

		}catch(NumberFormatException nfe) {
			return false;
		}
	}

	public static boolean isBoolean(String cadena) {
		if(cadena.equals("true") || cadena.equals("false")) {
			return true;
		}
		return false;

	}
	// por si alguien escribe todo pegado 
	public String separaDelimitadores(String linea){
		for (String string : Arrays.asList("(",")","{","}","=",";")) {
			if(string.equals("=")) {
				if(linea.indexOf(">=")>=0) {
					linea = linea.replace(">=", " >= ");
					break;
				}
				if(linea.indexOf("<=")>=0) {
					linea = linea.replace("<=", " <= ");
					break;
				}
				if(linea.indexOf("==")>=0)
				{
					linea = linea.replace("==", " == ");
					break;
				}
			}
			if(linea.contains(string)) 
				linea = linea.replace(string, " "+string+" ");
		}
		return linea;
	}
	public int cuenta (String token) {

		int conta=0;
		NodoDoble<Token> Aux=tokens.getInicio();
		while(Aux !=null){
			if(Aux.dato.getValor().equals(token))
				conta++;
			Aux=Aux.siguiente;
		}	
		return conta;
	}
	//Metodo para saber el tipo de dato
	public String TipoDato(String aux) {
		if(Pattern.matches("[0-9]+", aux))
			return "int";
		if(Pattern.matches("[0-9]+.[0-9]", aux))
			return "float";
		if(Pattern.matches("true+", aux))
			return "boolean";

		return "";

	}




	public ArrayList<String> getmistokens() {
		return impresion;
	}

	


}
