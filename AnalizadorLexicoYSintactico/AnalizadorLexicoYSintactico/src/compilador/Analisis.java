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
		Token tokensig, to;
		if(nodo!=null) // si no llego al ultimo de la lista
		{
			to =  nodo.dato;
			tokensig=analisisSintactio(nodo.siguiente); // buscar el siguiente de forma recursiva
			switch (to.getTipo()) // un switch para validar la estructura
			{
			case Token.MODIFICADOR:
				int sig=tokensig.getTipo();
				// aqui se valida que sea 'public int' o 'public class' 
				if(sig!=Token.TIPO_DATO && sig!=Token.CLASE)// si lo que sigue 
					impresion.add("Error sinatactico en la linea "+to.getLinea()+" se esparaba un tipo de dato");
				break;
			case Token.IDENTIFICADOR:
				// lo que puede seguir despues de un idetificador
				if(!(Arrays.asList("{","=",";").contains(tokensig.getValor()))) 
					impresion.add("Error sinatactico en la linea "+to.getLinea()+" se esparaba un simbolo");
				else
					if(nodo.anterior.dato.getValor().equals("class")) // se encontro la declaracion de la clase
					{
						identi.add( new Identificador(to.getValor(), " ", "class"));
					}
				break;
			// Estos dos entran en el mismo caso
			case Token.TIPO_DATO:
			case Token.CLASE:
				// si lo anterior fue modificador
				if (nodo.anterior!=null) 
					if(nodo.anterior.dato.getTipo()==Token.MODIFICADOR) {
						if(tokensig.getTipo()!=Token.IDENTIFICADOR) 
							impresion.add("Error sinatactico en la linea "+to.getLinea()+" se esparaba un identificador");
					}else
						impresion.add("Error sinatactico en la linea "+to.getLinea()+" se esperaba un modificador");
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
						if(!(nodo.anterior.dato.getValor().equals("if")&&tokensig.getTipo()==Token.CONSTANTE)) {
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
						if(tokensig.getTipo()!=Token.CONSTANTE)
							impresion.add("Error sinatactico en la linea "+to.getLinea()+ " se esperaba una constante");
						else {
							if(nodo.anterior.anterior.dato.getTipo()==Token.TIPO_DATO)
								identi.add(new Identificador(nodo.anterior.dato.getValor(),tokensig.getValor(),nodo.anterior.anterior.dato.getValor()));
							else
								impresion.add("Error sinatactico en linea "+to.getLinea()+ " se esperaba un tipo de dato");

						}
					}else
						impresion.add("Error sinatactico en linea "+to.getLinea()+ " se esperaba un identificador");
				}
				break;
			
			case Token.CONSTANTE:
				if(nodo.anterior.dato.getValor().equals("="))
					if(tokensig.getTipo()!=Token.OPERADOR_ARITMETICO&&tokensig.getTipo()!=Token.CONSTANTE&&!tokensig.getValor().equals(";"))
						impresion.add("Error sinatactico en linea "+to.getLinea()+ " asignacion no valida");
				break;
			case Token.PALABRA_RESERVADA:
				// verificar esructura de if
				if(to.getValor().equals("if"))
				{
					if(!tokensig.getValor().equals("(")) {
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
				if(tokensig.getTipo()!=Token.CONSTANTE)
					impresion.add("Error sinatactico en linea "+to.getLinea()+ " se esperaba una constante");
				break;
			}

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
		else if(Arrays.asList("int","char","float","boolean").contains(token))
			tipo = Token.TIPO_DATO;
		else if(Arrays.asList("(",")","{","}","=",";").contains(token))
			tipo = Token.SIMBOLO;
		else if(Arrays.asList("<","<=",">",">=","==","!=").contains(token))
			tipo = Token.OPERADOR_LOGICO;
		else if(Arrays.asList("+","-","*","/").contains(token))
			tipo = Token.OPERADOR_ARITMETICO;
		else if(Arrays.asList("true","false").contains(token)||Pattern.matches("^\\d+$",token)) 
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
	public ArrayList<String> getmistokens() {
		return impresion;
	}
}
