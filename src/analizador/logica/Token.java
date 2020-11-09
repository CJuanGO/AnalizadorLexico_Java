/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analizador.logica;
import analizador.crearTokens.Tokens;

/**
 *Clase Objetos tipo token
 * @author CJuanGO
 */
public class Token {
      /**
     * Atributo que representa el tipo de token
     */
    private Tokens token;
    
    /**
     * Atributo que representa el lexema del token
     */
    private String lexema;
    
    /**
     * Atributo que representa la línea donde se encuentra el token
     */
    private int Linea;
 /**
     * Método constructor de la clase 
     * @param token tipo de token
     * @param lexema cadena de caracteres
     * @param numeroLinea línea donde se encuetra el token
     */
    public Token(Tokens token, String lexema, int numeroLinea) {
        this.token = token;
        this.lexema = lexema;
        this.Linea = numeroLinea;
    }

    /**
     * Método que permite obtener el valor del atributo token
     *
     * @return El valor del atributo token
     */
    public Tokens getToken() {
        return token;
    }

    /**
     * Método que permite asignar un valor al atributo token
     *
     * @param token Valor a ser asignado al atributo token
     */
    public void setToken(Tokens token) {
        this.token = token;
    }

    public String getLexema() {
        return lexema;
    }

    public void setLexema(String lexema) {
        this.lexema = lexema;
    }

    public int getLinea() {
        return Linea;
    }

    public void setLinea(int Linea) {
        this.Linea = Linea;
    }

  
    
}
