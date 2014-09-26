/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package objectToMongoTranslate;

import com.mongodb.DBObject;
import java.util.List;
import jsonJava.JSONArray;
import jsonJava.JSONObject;

/**
 * * MyRunnable will count the sum of the number from 1 to the parameter *
 * countUntil and then write the result to the console.
 * <p>
 * MyRunnable is the task which will be performed
 *
 * * @author Lars Vogel *
 */
public class MyRunnable implements Runnable {

    private final Acumulador ac; // objeto compartilhado
    private final JSONArray header;
    private final List<DBObject> sub_resultado;

    public MyRunnable(JSONArray header, List<DBObject> sub_resultado, Acumulador ac) {
        this.ac = ac;
        this.header = header;
        this.sub_resultado = sub_resultado;
    }

    @Override
    public void run() {
        String result_tmp = "";
        for (int i = 0; i < sub_resultado.size(); i++) {
            result_tmp += "{";

            DBObject objeto = sub_resultado.get(i);

            for (int j = 0; j < header.length(); j++) {
                String atributo_atual = header.get(j).toString();
                if (objeto.containsField(atributo_atual)) {
                    result_tmp += "\"" + objeto.get(atributo_atual) + "\", ";
                } else {
                    result_tmp += "\" \" , ";
                }
            }
            result_tmp += "},";
        }
        ac.set(result_tmp);
    }
}
