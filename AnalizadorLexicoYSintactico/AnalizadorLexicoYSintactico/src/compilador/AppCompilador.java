package compilador;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
public class AppCompilador extends JFrame implements ActionListener{
	// Componentes o Atributos
	private JMenuBar barraMenu;
	private JMenu menuArchivo;
	// Menu Archivo
	private JMenuItem itemNuevo,itemAbrir,itemGuardar,itemSalir,itemAnalisLexico;
	private JFileChooser ventanaArchivos;
	private File archivo;
	private JTextArea areaTexto;
	private JScrollPane barrita; 
	private JList<String> tokens;
	private JTabbedPane documentos,consola,tabla,tabla2;
	private String [] titulos ={"Tipo","Nombre","Valor","Alcance","Renglon"};
	DefaultTableModel modelo = new DefaultTableModel(new Object[0][0],titulos);
	private String [] titulos2 ={"Operador","arg 1","arg 2","Resultado"};
	DefaultTableModel modelo2 = new DefaultTableModel(new Object[0][0],titulos2);
	public JTable mitabla = new JTable(modelo);
	public JTable mitabla2 = new JTable(modelo2);
	private JButton btnAnalizar;

	public static void main(String[] args) {
		/*try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {
		}*/
		new AppCompilador();
	}
	public AppCompilador() {
		super("Analizador Lexico y Sintáctico");
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		setLayout(new GridLayout(3,2));
		//mitabla.setBackground(new Color(255,45,0));
		setSize(1000,550);
		setResizable(false);
		setLocationRelativeTo(null);
		creaInterFaz();
		setVisible(true);
	}
	private void creaInterFaz() {
		barraMenu = new JMenuBar();
		setJMenuBar(barraMenu);
		menuArchivo = new JMenu("Archivo");
		menuArchivo.setIcon(new ImageIcon("archivo.png"));
		ventanaArchivos = new JFileChooser();
		itemNuevo = new JMenuItem("Nuevo");
		itemAbrir = new JMenuItem("Abrir...");
		itemGuardar = new JMenuItem("Guardar...");
		itemSalir = new JMenuItem("Salir");
		itemSalir.addActionListener(this);
		itemGuardar.addActionListener(this);
		itemAbrir.addActionListener(this);
		itemNuevo.addActionListener(this);
		itemAnalisLexico  = new JMenuItem("Analizar codigo");
		itemAnalisLexico.addActionListener(this);
		btnAnalizar = new JButton("ANALIZAR");
		btnAnalizar.setFont(new Font("Dialog",Font.PLAIN,40));
		btnAnalizar.addActionListener(this);


		ventanaArchivos = new JFileChooser();
		menuArchivo.add(itemNuevo);
		menuArchivo.add(itemAbrir);
		menuArchivo.add(itemGuardar);
		menuArchivo.addSeparator();
		menuArchivo.add(itemSalir);
		
		barraMenu.add(menuArchivo);
		areaTexto = new JTextArea();
		ventanaArchivos= new JFileChooser("Guardar");
		areaTexto.setFont(new Font("Consolas", Font.PLAIN, 12));
		barrita = new JScrollPane(areaTexto);
		barrita.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		barrita.setPreferredSize(new Dimension(870, 65));


		documentos = new JTabbedPane();
		consola = new JTabbedPane();
		tabla = new JTabbedPane();
		tabla2 = new JTabbedPane();

		documentos.addTab("Nuevo",barrita);
		documentos.setToolTipText("Aqui se muestra el codigo");
		add(documentos);
		tokens=new JList<String>();
		consola.addTab("Consola",new JScrollPane(tokens));
		//consola.addTab("Tabla",new JScrollPane(mitabla));
		tabla.addTab("Tabla de simbolos",new JScrollPane(mitabla) );
		tabla2.addTab("Tabla de Cuadruplos",new JScrollPane(mitabla2) );
		add(consola);
		consola.setToolTipText("Aqui se muestra el resultado del analisis");
		add(tabla2);
		add(btnAnalizar);
		add(tabla);
		
		


	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==btnAnalizar) {
			if(guardar()){
				Analisis analisador = new Analisis(archivo.getAbsolutePath());
				tokens.setListData(analisador.getmistokens().toArray( new String [0]));
				modelo = new DefaultTableModel(new Object[0][0],titulos);
				modelo2 = new DefaultTableModel(new Object[0][0],titulos2);


				for (int i=0; i < analisador.getIdenti().size(); i++) {
					Identificador id = analisador.getIdenti().get(i);						
					mitabla.setModel(modelo);
					if(!id.tipo.equals("")) {
						Object datostabla[]= {id.tipo,id.nombre,id.valor,id.alcance,id.posicion};

						modelo.addRow(datostabla);
					}
				}


				for (int i=0; i < analisador.getIdenti2().size(); i++) {
					Arbol id2 =analisador.getIdenti2().get(i);								
					mitabla2.setModel(modelo2);
					Object datostabla2[]= {id2.operador,id2.arg1,id2.arg2,id2.resultado};

					modelo2.addRow(datostabla2);

				}

			}

			return;
		}
		if (e.getSource()==itemSalir) {
			System.exit(0);
			return;
		}
		if(e.getSource()==itemNuevo) {
			documentos.setTitleAt(0, "Nuevo");
			areaTexto.setText("");
			archivo=null;
			tokens.setListData(new String[0]);
			return;
		}
		if(e.getSource()==itemAbrir) {
			ventanaArchivos.setDialogTitle("Abrir..");
			ventanaArchivos.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if(ventanaArchivos.showOpenDialog(this)==JFileChooser.CANCEL_OPTION) 
				return;
			archivo=ventanaArchivos.getSelectedFile();
			documentos.setTitleAt(0, archivo.getName());
			abrir();
		}
		if(e.getSource()==itemGuardar) {
			guardar();
		}
	}
	public boolean guardar() {
		try {
			if(archivo==null) {
				ventanaArchivos.setDialogTitle("Guardando..");
				ventanaArchivos.setFileSelectionMode(JFileChooser.FILES_ONLY);
				if(ventanaArchivos.showSaveDialog(this)==JFileChooser.CANCEL_OPTION) 
					return false;
				archivo=ventanaArchivos.getSelectedFile();
				documentos.setTitleAt(0, archivo.getName());
			}
			FileWriter fw = new FileWriter(archivo);
			BufferedWriter bf = new BufferedWriter(fw);
			bf.write(areaTexto.getText());
			bf.close();
			fw.close();
		}catch (Exception e) {
			System.out.println("Houston tenemos un problema?");
			return false;
		}
		return true;
	}
	public boolean abrir() {
		String texto="",linea;
		try {
			FileReader fr = new FileReader(archivo) ; 
			BufferedReader br= new BufferedReader(fr);
			while((linea=br.readLine())!=null) 
				texto+=linea+"\n";
			areaTexto.setText(texto);
			return true;
		}catch (Exception e) {
			archivo=null;
			JOptionPane.showMessageDialog(null, "Tipo de archivo incompatible", "Warning",
					JOptionPane.WARNING_MESSAGE);
			return false;
		}
	}



}
