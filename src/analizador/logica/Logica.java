
package analizador.logica;

import java.util.ArrayList;
import javax.swing.JFileChooser;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import analizador.crearTokens.Tokens;
import analizador.crearTokens.PalabrasReservadas;
import analizador.gui.Ventana;
import java.util.ResourceBundle;

/**
 *Clase que contiene la logica del analizador
 * @author CJuanGO
 */
public class Logica {
    /**
     * Atributo que representa la lista de símbolos aceptados
     */
    public ArrayList<Character> simbolos;

    /**
     * Atributo que representa la lista de tokens encontrados
     */
    private ArrayList<Token> tokens;

    /**
     * Atributo que representa el seleccionador de archivos
     */
    private JFileChooser escogerArchivo;

    /**
     * Atributo que representa el archivo de recursos
     */
    private ResourceBundle bundle;

    /**
     * Atributo que contiene la ruta donde se guarda el archivo
     */
    private String rutaArchivo;

    /**
     * Atributo que representa la línea donde se encuentra el token
     */
    private int lineaToken;
    String rutaBundle="analizador/resources/Bundle";
    
    public Logica(){
        this.tokens = new ArrayList<>();
        this.simbolos = simbolosPermitidos();       
        this.bundle= java.util.ResourceBundle.getBundle(rutaBundle);
            }
      /**
     * Método utilizado para agregar cada uno de los tokens a la tabla de la
     * interfaz principal
     */
    public void agregarTokens() {
        DefaultTableModel model = (DefaultTableModel) Ventana.tabla.getModel();
        for (int i = 0; i < tokens.size(); i++) {
            model.addRow(new Object[]{tokens.get(i).getToken(), tokens.get(i).getLexema(), tokens.get(i).getLinea()});
        }
        Ventana.tabla.setModel(model);
    }

    /**
     * Método utilizado para obtener todos los tokens del área donde se ingresa
     * el código fuente
     *
     * @param texto código al que se le van a obtener los tokens
     * @param lineaNumero número de línea en que se encuentran los tokens
     */
    public void obtenerTokens(String texto, int lineaNumero) {
        int i = 0;
        lineaToken = lineaNumero;
        while (texto.length() != 0) {
            char empezar = texto.charAt(i);
            if (empezar == '\r' || empezar == '\n' || empezar == '\t' || empezar == ' ') {
                if (empezar == '\r') {
                    lineaToken = lineaToken + 1;
                }
                texto = texto.substring(i + 1, texto.length());

            } else {
                if (!Character.isLetterOrDigit(empezar) && simbolos.contains(empezar) == false) {
                    invalidoCaracter(empezar, lineaToken);
                    texto = texto.substring(i + 1, texto.length());
                } else {
                    if (Character.isLetter(empezar)) {
                        texto = tokenIdentificador(texto, lineaToken);
                    }
                    switch (empezar) {
                        case '.':
                        case '[':
                        case '|':
                        case '@':
                        case ']':
                        case ',':
                        case '&':{ texto = tokenSimbolo(texto, lineaToken);
                            break;}
                        case '+':
                        case '/':
                        case '-':
                        case '*':
                        case '>':{ texto = tokenSimbolo(texto, lineaToken);
                            break;}
                        case '=':
                        case '%':
                        case '{':
                        case '}': {
                            texto = tokenSimbolo(texto, lineaToken);
                            break;
                        }
                        case '<':{ texto = tokenSimbolo(texto, lineaToken);
                            break;}
                        case '#': {
                            String textoAux = tokenAsignacion(texto, lineaToken);
                            if (textoAux.equals(texto)) {
                                texto = tokenLineaComentario(texto, lineaToken);
                            } else {
                                texto = textoAux;
                            }
                            break;
                        }
                        
                        case '"': {
                            texto = tokenCadena(texto, lineaToken);
                        }
                    }
                }
            }
        }

        agregarTokens();
    }

    
    /**
     * Método utilizado para obtener el token identificador
     *
     * @param linea1 línea a ser analizada
     * @param lineaToken línea donde se encuentra el token
     * @return el resto de código que falta por analizar
     */
    public String tokenIdentificador(String linea1, int lineaToken) {
        Token palabraReservada = null;
        String aux = "";
        for (int i = 0; i < linea1.length(); i++) {
            if (!Character.isLetterOrDigit(linea1.charAt(i)) && linea1.charAt(i) != '_') {
                palabraReservada = tokePalabraReservda(linea1.substring(0, i), lineaToken);
                if (palabraReservada == null) {
                    Token token = new Token(Tokens.IDENTIFICADOR, linea1.substring(0, i), lineaToken);
                    tokens.add(token);
                } else {
                    tokens.add(palabraReservada);
                }
                aux = linea1.substring(i, linea1.length());
                return aux;
            }
        }
        palabraReservada = tokePalabraReservda(linea1, lineaToken);
        if (palabraReservada == null) {
            Token token = new Token(Tokens.IDENTIFICADOR, linea1, lineaToken);
            tokens.add(token);
        } else {
            tokens.add(palabraReservada);
        }
        return aux;
    }

    /**
     * Método utilizado para obtener el token palabra reservada
     *
     * @param linea línea a ser analizada
     * @param lineaNumero línea donde se encuentra el token
     * @return un token si es una palabra reservada,null si no es una palabra
     * reservada
     */
    public Token tokePalabraReservda(String linea, int lineaNumero) {
        Token token = null;
        try {
            PalabrasReservadas reservadaPalabra = PalabrasReservadas.valueOf(linea);
            token = new Token(Tokens.PALABRA_RESERVADA, linea, lineaNumero);
        } catch (IllegalArgumentException e) {
            return null;
        }
        return token;
    }

    /**
     * Método utilizado para obtener el token símbolo o fin de línea o separador
     *
     * @param linea línea a ser analizada
     * @param lineaNumero línea donde se encuentra el token
     * @return el resto de código que falta por analizar
     */
    public String tokenSimbolo(String linea, int lineaNumero) {
        switch (linea.charAt(0)) {
            case '.': {
                Token token = new Token(Tokens.TERMINAL_DE_LINEA, linea.substring(0, 1), lineaNumero);
                tokens.add(token);
                break;
            }
            case ',': {
                Token token = new Token(Tokens.SEPARADOR, linea.substring(0, 1), lineaNumero);
                tokens.add(token);
                break;
            }
             case '+':{
                Token token = new Token(Tokens.OPERADOR_ARITMETICO, linea.substring(0, 1), lineaNumero);
                tokens.add(token);
                break;
            }
             case '-':{
                Token token = new Token(Tokens.OPERADOR_ARITMETICO, linea.substring(0, 1), lineaNumero);
                tokens.add(token);
                break;
            }
             case '*':{
                Token token = new Token(Tokens.OPERADOR_ARITMETICO, linea.substring(0, 1), lineaNumero);
                tokens.add(token);
                break;
            }
             case '%':{
                 {
                Token token = new Token(Tokens.OPERADOR_ARITMETICO, linea.substring(0, 1), lineaNumero);
                tokens.add(token);
                break;
            }
             }
             case '/':{
                Token token = new Token(Tokens.OPERADOR_ARITMETICO, linea.substring(0, 1), lineaNumero);
                tokens.add(token);
                break;
            }
             case '<': {
                 Token token = new Token(Tokens.OPERADOR_LOGICO, linea.substring(0, 1), lineaNumero);
                tokens.add(token);
                break;
             }case '>': {
                  Token token = new Token(Tokens.OPERADOR_LOGICO, linea.substring(0, 1), lineaNumero);
                tokens.add(token);
                break;
             }case '=':
              {
                Token token = new Token(Tokens.ASIGNACION, linea.substring(0, 1), lineaNumero);
                tokens.add(token);
                break;
            }
              case '&': {
                Token token = new Token(Tokens.OPERADOR_LOGICO, linea.substring(0, 1), lineaNumero);
                tokens.add(token);
                break;
            }
              case '|': {
                Token token = new Token(Tokens.OPERADOR_LOGICO, linea.substring(0, 1), lineaNumero);
                tokens.add(token);
                break;
            }
            case '{':{
                Token token = new Token(Tokens.ABRIR_LLAVE, linea.substring(0, 1), lineaNumero);
                tokens.add(token);
                break;
            }
            case '}': {
                Token token = new Token(Tokens.CERRAR_LLAVE, linea.substring(0, 1), lineaNumero);
                tokens.add(token);
                break;
            }
             case '[':{
                Token token = new Token(Tokens.ABRIR_CORCHETE, linea.substring(0, 1), lineaNumero);
                tokens.add(token);
                break;
            }
            case ']': {
                Token token = new Token(Tokens.CERRAR_CORCHETE, linea.substring(0, 1), lineaNumero);
                tokens.add(token);
                break;
            }
             case '@': {
                Token token = new Token(Tokens.CONCATENACION, linea.substring(0, 1), lineaNumero);
                tokens.add(token);
                break;
            }
        }
        
     

              linea = linea.substring(1, linea.length());

      
        return linea;
    }

    /**
     * Método utilizado para obtener el token de asignación
     *
     * @param linea línea a ser analizada
     * @param lineaNumero línea donde se encuentra el token
     * @return el resto de código que falta por analizar
     */
    public String tokenAsignacion(String linea, int lineaNumero) {
        String aux = "";
               if (linea.length() >= 2) { 
            
            if ((linea.charAt(0) =='<') && (linea.charAt(1) =='-')) {
                 
                Token token = new Token(Tokens.ASIGNACION, linea.substring(0, 2), lineaNumero);
                tokens.add(token);
                aux = linea.substring(2, linea.length());
                return aux;
            } else {
                return linea;
            }
        }
        invalidoCaracter(linea.charAt(0), lineaNumero);
        aux = linea.substring(1, linea.length());
        return aux;
    }

    /**
     * Método utilizado para obtener el token comentario de linea
     *
     * @param linea línea a ser analizada
     * @param lineaNumero línea donde se encuentra el token
     * @return el resto de código que falta por analizar
     */
    public String tokenLineaComentario(String linea, int lineaNumero) {
        String aux ="";
        if (linea.length() >= 2) {
            if (linea.charAt(0) =='#' && linea.charAt(1) =='#') {
                for (int i = 2; i < linea.length(); i++) {
                    if (linea.charAt(i) == '#') {
                        if ((i + 1) < linea.length()) {
                            if (linea.charAt(i + 1) =='#') {
                                Token token = new Token(Tokens.COMENTARIO, linea.substring(0, i + 2), lineaNumero);
                                tokens.add(token);
                                aux = linea.substring(i + 2, linea.length());
                                return aux;
                            }
                        }
                        break;
                    }
                    if ((linea.charAt(i)) == '\r') {
                        lineaToken = lineaToken + 1;
                    }
                }
                invalidoSimbolo(lineaNumero);
                return aux;
            }
        }
        invalidoCaracter(linea.charAt(0), lineaNumero);
        aux = linea.substring(1, linea.length());
        return aux;
    }

    /**
     * Método utilizado para obtener el token comentario de cadena
     *
     * @param linea línea a ser analizada
     * @param lineaNumero línea donde se encuentra el token
     * @return el resto de código que falta por analizar
     */
    public String tokenCadena(String linea, int lineaNumero) {
        String aux = "";

        if (linea.length() >= 2) {
            for (int i = 1; i < linea.length(); i++) {
                if ((linea.charAt(i)) == '"') {
                    Token token = new Token(Tokens.CADENA, linea.substring(0, i + 1), lineaNumero);
                    tokens.add(token);
                    aux = linea.substring(i + 1, linea.length());
                    return aux;
                }
                if ((linea.charAt(i)) == '\r') {
                    lineaToken = lineaToken + 1;
                }
            }
            invalidoSimbolo(lineaNumero);
            return aux;
        }
        invalidoCaracter(linea.charAt(0), lineaNumero);
        return aux;
    }

    /**
     * Método utilizado para abrir un fichero externo
     *
     * @return true si se abre correctamente el archivo, false si no se abre
     */
    public boolean abrirArchivo() {
        boolean estado = false;
        styleChooser();
        String extension = bundle.getString("GuardarComo.extension");
        int resultado = escogerArchivo.showOpenDialog(null);
        if (resultado != JFileChooser.CANCEL_OPTION && resultado != JFileChooser.ERROR_OPTION) {
            if (escogerArchivo != null) {
                String ruta = escogerArchivo.getSelectedFile().getAbsolutePath();
                if (!ruta.endsWith(extension)) {
                    JOptionPane.showMessageDialog(null, bundle.getString("Logica.Error.Extension"), bundle.getString("Logica.Title.Error.Extension"), ERROR_MESSAGE);
                } else {
                    rutaArchivo = ruta;
                    leerArchivo(ruta);
                    return true;
                }
            }
        }
        return estado;
    }

    /**
     * Método utilizado para leer un fichero externo
     *
     * @param ruta ruta donde se encuentra el fichero
     */
    public void leerArchivo(String ruta) {
        String line;
        StyledDocument archivo = Ventana.txaCodigo.getStyledDocument();
        Ventana.txaCodigo.setText("");
        try {
            BufferedReader read = new BufferedReader(new FileReader(ruta));
            line = read.readLine();
            while (line != null) {
                archivo.insertString(archivo.getLength(), line + "\n", null);
                line = read.readLine();
            }
            String texto = archivo.getText(0, archivo.getLength());
            int lastLine = texto.lastIndexOf('\n');
            archivo.remove(lastLine, archivo.getLength() - lastLine);
            Ventana.txaCodigo.setStyledDocument(archivo);
        } catch (FileNotFoundException ex) {

        } catch (IOException | BadLocationException ex) {

        }
    }

    /**
     * Método utilizado para guardar automáticamente el código
     *
     * @param guardar código fuente a ser guardado
     */
    public boolean guardar(String guardar) {
        if(rutaArchivo != null){
        crearArchivo(rutaArchivo, guardar);
        return true;
        }
        return false;
    }

    /**
     * Método utilizado para guardar en una ruta especificada por el usuario
     *
     * @param textoIngresado código a ser guardado
     * @return true si se guardo correctamente, false si no se guardo
     */
    public boolean guardarComo(String textoIngresado) {
        boolean estado = false;
        styleChooser();
        String extension = bundle.getString("GuardarComo.extension");
        int resultado = escogerArchivo.showSaveDialog(null);
        if (resultado != JFileChooser.CANCEL_OPTION && resultado != JFileChooser.ERROR_OPTION) {
            // Se obtiene la ruta donde se va a guardar el archivo
            if (escogerArchivo != null) {
                String ruta = escogerArchivo.getSelectedFile().getAbsolutePath();
                if (ruta.endsWith(extension)) {
                    crearArchivo(ruta, textoIngresado);
                } else {
                    ruta = ruta + extension;
                    crearArchivo(ruta, textoIngresado);
                }
                rutaArchivo = ruta;
                return true;
            }

        }
        return estado;
    }

    /**
     * Método utilizado para crear el archivo
     *
     * @param ruta ruta donde se va a crear el archivo
     * @param texto código a ser escrito dentro del archivo
     */
    public void crearArchivo(String ruta, String texto) {

        try (FileWriter file = new FileWriter(ruta, false)) {
            String[] text = texto.split("\n");
            for (String x : text) {
                file.write(x + "\n");
            }
        } catch (IOException ex) {

        }

    }

    /** 
     * Método encargado de mostrar un mensaje de error cuando se encuentra
     * más de un carácter invalido en el código, es decir, cuando no se cierran
     * numeral o cuando no se cierra un comentario
     * @param lineaNumero línea desde donde se inicia el error
     */
       public void invalidoSimbolo( int lineaNumero) {
        Ventana.jTxtAreaErrores.append(bundle.getString("Ventana.jTxtAreaErroresFuente") + " < " + bundle.getString("Ventana.Error.Alguna") + " > "+bundle.getString("Ventana.jTxtAreaErroresDeLinea")+" "+lineaNumero+"\n");
    }
    /**
     * Método encargado de mostrar un mensaje de error cuando se encuentra 
     * un carácter invalido 
     *
     * @param caracter carácter invalido
     * @param lineaNumero número de línea donde se encuentra el cáracter
     */
    public void invalidoCaracter(Character caracter, int lineaNumero) {
        Ventana.jTxtAreaErrores.append(bundle.getString("Ventana.jTxtAreaErroresError") + " < " + caracter + " > " + bundle.getString("Ventana.jTxtAreaErroresLinea") + " " + lineaNumero + "\n");
    }
    /**
    *simbolos permitidos
    */
     public final ArrayList<Character> simbolosPermitidos() {
        ArrayList<Character> caracteres = new ArrayList<>();
        caracteres.add('.');
        caracteres.add('<');
        caracteres.add('>');
        caracteres.add('=');
        caracteres.add('+');
        caracteres.add('*');
        caracteres.add('/');
        caracteres.add('%');
        caracteres.add('"');
        caracteres.add(' ');
        caracteres.add('#');
        caracteres.add('&');
        caracteres.add('|');
        caracteres.add(',');
        caracteres.add('[');
        caracteres.add(']');
        caracteres.add('-');
        caracteres.add('{');
        caracteres.add('}');
        caracteres.add('\n');
        caracteres.add('\t');
        caracteres.add('\r');
        caracteres.add('@');
        return caracteres;

    }
     /**
     * Método utilizado para que el estilo del seleccionador de archivo sea
     * igual al del sistema operativo utilizado por el usuario y no se utilice
     * el defecto por java
     */
    public void styleChooser() {
        escogerArchivo = null;
        LookAndFeel previousLF = UIManager.getLookAndFeel();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            escogerArchivo = new JFileChooser();
            UIManager.setLookAndFeel(previousLF);
        } catch (IllegalAccessException | UnsupportedLookAndFeelException | InstantiationException | ClassNotFoundException e) {
        }

    }

    public ArrayList<Character> getSimbolos() {
        return simbolos;
    }

    public void setSimbolos(ArrayList<Character> simbolos) {
        this.simbolos = simbolos;
    }

    public ArrayList<Token> getTokens() {
        return tokens;
    }

    public void setTokens(ArrayList<Token> tokens) {
        this.tokens = tokens;
    }

    public JFileChooser getEscogerArchivo() {
        return escogerArchivo;
    }

    public void setEscogerArchivo(JFileChooser escogerArchivo) {
        this.escogerArchivo = escogerArchivo;
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    public void setBundle(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public int getLineaToken() {
        return lineaToken;
    }

    public void setLineaToken(int lineaToken) {
        this.lineaToken = lineaToken;
    }

   
    
}
