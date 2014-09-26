/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package joinObjectToMongoTranslate;

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

    private final String first_param, second_param;
    private final List<DBObject> output_agg2;
    private final Acumulador ac; // objeto compartilhado
    private final JSONArray header;
    private final List<DBObject> subResultado;
    private final String tipoJoin;

    public MyRunnable(List<DBObject> output_agg2, JSONArray header,
            List<DBObject> subResultado, Acumulador ac,
            String first_param, String second_param, String tipoJoin) {
        this.output_agg2 = output_agg2;
        this.ac = ac;
        this.header = header;
        this.tipoJoin = tipoJoin;
        this.subResultado = subResultado;
        this.first_param = first_param;
        this.second_param = second_param;
    }

    @Override
    public void run() {
        String retorno_tmp = "";
        JSONObject my_obj_result_set;
        boolean encontrou_join = false;

        if (tipoJoin.equals("INNER")) {
            for (int i = 0; i <= this.subResultado.size() - 1; i++) {
                DBObject objeto1 = this.subResultado.get(i);
                for (int k = 0; k <= this.output_agg2.size() - 1; k++) {
                    DBObject objeto2 = this.output_agg2.get(k);
                    if (objeto1.get(first_param).equals(objeto2.get(second_param))) {
                        objeto1.putAll(objeto2);
                        retorno_tmp += "{";
                        for (int j = 0; j < header.length(); j++) {
                            String atributo_atual = header.get(j).toString();
                            if (objeto1.containsField(atributo_atual)) {
                                retorno_tmp += objeto1.get(atributo_atual) + " , ";
                            } else {
                                retorno_tmp += "'' , ";
                            }
                        }
                        retorno_tmp += "} , ";
                    }
                }
            }
            ac.set(retorno_tmp);
        } else if (tipoJoin.equals("LEFT") || tipoJoin.equals("RIGHT")) {
            for (int i = 0; i <= this.subResultado.size() - 1; i++) {
                DBObject objeto1 = this.subResultado.get(i);
                for (int k = 0; k <= this.output_agg2.size() - 1; k++) {
                    DBObject objeto2 = this.output_agg2.get(k);
                    if (objeto1.get(first_param).equals(objeto2.get(second_param))) {
                        encontrou_join = true;
                        objeto1.putAll(objeto2);
                        retorno_tmp += "{";
                        for (int j = 0; j < header.length(); j++) {
                            String atributo_atual = header.get(j).toString();
                            if (objeto1.containsField(atributo_atual)) {
                                retorno_tmp += objeto1.get(atributo_atual) + " , ";
                            } else {
                                retorno_tmp += "'' , ";
                            }
                        }
                        retorno_tmp += "} , ";
                    }
                }

                if (!encontrou_join) {
                    retorno_tmp += "{";
                    for (int j = 0; j < header.length(); j++) {
                        String atributo_atual = header.get(j).toString();
                        if (objeto1.containsField(atributo_atual)) {
                            retorno_tmp += objeto1.get(atributo_atual) + " , ";
                        } else {
                            retorno_tmp += "'' , ";
                        }
                    }
                    retorno_tmp += "} , ";
                }
                encontrou_join = false;
            }
            ac.set(retorno_tmp);
        }
    }
}
