package SimpleSelect;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import jsonJava.JSONArray;
import jsonJava.JSONObject;
import net.sf.jsqlparser.JSQLParserException;
import queryInterceptor.QueryInterceptor;
import selectClause.SelectClause;

public class SimpleSelect {

    public static String retornaResultSet(DBCursor select_result, String header) throws JSQLParserException {

        String result_set = "";
        DBObject objetoAtual;
        JSONArray array_header = new JSONArray("[" + header + "]");
       
        long inicio = System.currentTimeMillis();
            while (select_result.hasNext()) {
                result_set += "{";

                objetoAtual = select_result.next();

                for (int j = 0; j < array_header.length(); j++) {
                    String atributo_atual = array_header.get(j).toString();
                    if (objetoAtual.containsField(atributo_atual)) {
                        result_set += "\"" + objetoAtual.get(atributo_atual) + "\", ";
                    } else {
                        result_set += "\" \" , ";
                    }
                }
                result_set += "},";
            }
            long fim = System.currentTimeMillis();
        //}

        return result_set;
    }

    /*
     * Método responsável por retornar o header correto
     * O header deve ser o maior possível, pelo fato de que documentos
     * podem ter diferentes atributos
     */
    public static String retornaCabecalho(SelectClause selectStatement) {
        String header = "";

        //Consideramos que se tiver função, é apenas uma tabela
        if (selectStatement.getFuncoes().isEmpty()) {
            for (int i = 0; i < selectStatement.getTablesQueried().size(); i++) {
                if (selectStatement.getTablesQueried().get(i).isIsAllColumns()) {
                    SimpleSelect agg = new SimpleSelect();
                    String table = selectStatement.getTablesQueried().get(i).getName().toString();
                    String cabecalhoAllColumn = agg.retornaCabecalhoAllColumns(table);
                    header += cabecalhoAllColumn + ",";

                } else {
                    for (int j = 0; j < selectStatement.getTablesQueried().get(i).getParams_projecao().size(); j++) {
                        if (selectStatement.getTablesQueried().get(i).getParams_projecao().get(j).getAlias() != null) {
                            header += "'" + selectStatement.getTablesQueried().get(i).getParams_projecao().get(j).getAlias() + "',";
                        } else {
                            header += "'" + selectStatement.getTablesQueried().get(i).getParams_projecao().get(j).getName() + "',";
                        }

                    }
                }
            }
            //Se possui funções
        } else {
            for (int i = 0; i < selectStatement.getFuncoes().size(); i++) {
                if (selectStatement.getFuncoes().get(i).getAlias() != null) {
                    header += selectStatement.getFuncoes().get(i).getAlias().toString() + ",";
                } else {
                    header += selectStatement.getFuncoes().get(i).getNome_completo() + ",";
                }
            }
        }

        return header;
    }

    public String retornaCabecalhoAllColumns(String table) {
        DBCollection collection = QueryInterceptor.database.getCollection("metadata");
        DBObject projection = (DBObject) JSON.parse("{columns:1, _id:0}");
        BasicDBObject projection_ppl = new BasicDBObject("$project", projection);
        DBObject criteria = (DBObject) JSON.parse("{table:'" + table + "'}");
        BasicDBObject criteria_ppl = new BasicDBObject("$match", criteria);

        AggregationOutput saida = collection.aggregate(criteria_ppl, projection_ppl);
        String json_str = saida.results().toString();
        String atributos = "";
        int tamanho_resultado = json_str.length();
        if (!json_str.toString().equals("[ ]")) {
            JSONObject my_obj = new JSONObject(json_str.substring(1, tamanho_resultado - 1));
            atributos = my_obj.get("columns").toString().substring(1, my_obj.get("columns").toString().length() - 1);
            atributos = atributos.replaceAll("[\"]", "'");
        }
        return atributos;
    }
}
