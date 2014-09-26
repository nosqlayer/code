/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package joinObjectToMongoTranslate;

public class Acumulador {

    private String resultado_thread;

    public Acumulador() {
        resultado_thread = "";
    }

    public void set(String tupla) {
        resultado_thread += tupla;
    }

    public String get() {
        return resultado_thread;
    }
}  