/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package joinObjectToMongoTranslate;

import com.mongodb.DBObject;
import java.util.List;
import jsonJava.JSONArray;
import jsonJava.JSONObject;
import org.bson.types.BasicBSONList;

/**
 * * MyRunnable will count the sum of the number from 1 to the parameter *
 * countUntil and then write the result to the console.
 * <p>
 * MyRunnable is the task which will be performed
 *
 * * @author Lars Vogel *
 */
public class MyRunnable_SubDoc implements Runnable {

    private final int begin_index, end_index;
    private final List<DBObject> array;
    private final Acumulador ac; // objeto compartilhado
    private final String header;
    private final String subdoc;

    public MyRunnable_SubDoc(List<DBObject> array, String header, int begin_index, int end_index, Acumulador ac, String subdoc) {
        this.begin_index = begin_index;
        this.end_index = end_index;
        this.array = array;
        this.ac = ac;
        this.header = header;
        this.subdoc = subdoc;
    }

    @Override
    public void run() {
        String retorno_tmp = "";
        JSONObject my_obj_result_set;
        JSONArray array_header = new JSONArray("[" + header + "]");
        for (int i = begin_index; i <= end_index; i++) {
            DBObject objeto = array.get(i);
            DBObject objeto_sub = (DBObject) objeto.get(subdoc);
            BasicBSONList array_sub = (BasicBSONList) objeto_sub;
            if (array_sub != null) {
                for (int indice_sub = 0; indice_sub < array_sub.size(); indice_sub++) {
                    retorno_tmp += "{";
                    for (int j = 0; j < array_header.length(); j++) {
                        String atributo_atual = array_header.get(j).toString();
                        if (objeto.containsField(atributo_atual)) {
                            retorno_tmp += objeto.get(atributo_atual) + " , ";
                        } else {

                            if (objeto_sub != null) {
                                retorno_tmp += ((DBObject) array_sub.get(indice_sub)).get(atributo_atual) + " , ";
                            } else {
                                retorno_tmp += "'' , ";
                            }
                        }
                    }
                    retorno_tmp += "}, ";
                }
                //Se nao possui o subdocumento
            } else {
                retorno_tmp += "{";
                for (int j = 0; j < array_header.length(); j++) {
                    String atributo_atual = array_header.get(j).toString();
                    if (objeto.containsField(atributo_atual)) {
                        retorno_tmp += objeto.get(atributo_atual) + " , ";
                    } else {
                        retorno_tmp += "'' , ";
                    }
                }
                retorno_tmp += "}, ";
            }
        }

        ac.set(retorno_tmp);
    }
}
