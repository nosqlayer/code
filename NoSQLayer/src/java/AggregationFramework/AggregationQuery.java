package AggregationFramework;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import java.util.ArrayList;
import jsonJava.JSONArray;
import jsonJava.JSONObject;
import net.sf.jsqlparser.JSQLParserException;
import queryInterceptor.QueryInterceptor;
import selectClause.Funcoes;
import selectClause.SelectClause;

public class AggregationQuery {

    public ArrayList<DBObject> retornaVetorValores(DBObject criteria, DBObject projection,
            DBObject groupBy, DBObject order, Long limit, Long offset,
            SelectClause selectStatement) {

        ArrayList<DBObject> vetor_valores = new ArrayList<>();

        if (criteria != null) {
            BasicDBObject criteria_ppl = new BasicDBObject("$match", criteria);
            vetor_valores.add(criteria_ppl);
        }

        if (groupBy != null) {
            vetor_valores.add(groupBy);
            //No MongoDB o resultado do group já vai com o project correto
            projection = null;
        }

        if (order != null) {
            BasicDBObject orderBy = new BasicDBObject("$sort", order);
            vetor_valores.add(orderBy);
        }

        if (projection != null) {
            BasicDBObject projection_ppl = new BasicDBObject("$project", projection);
            vetor_valores.add(projection_ppl);
        }

        if (limit != null) {
            BasicDBObject limit_statement = new BasicDBObject("$limit", selectStatement.getLimit().getRowCount());
            vetor_valores.add(limit_statement);
        }
        if (offset != null) {
            BasicDBObject skip = new BasicDBObject("$skip", selectStatement.getLimit().getOffset());
            vetor_valores.add(skip);
        }
        //No caso de select * from actor (Cria-se uma consulta que casa com todos os atributos)
        if (vetor_valores.isEmpty()) {
            criteria = (DBObject) JSON.parse("{_id: { $ne : 0 }}");
            BasicDBObject criteria_ppl = new BasicDBObject("$match", criteria);
            vetor_valores.add(criteria_ppl);
        }

        return vetor_valores;
    }

    public AggregationOutput returnAggResult(ArrayList<DBObject> vetor_valores, DBCollection collection) {

        switch (vetor_valores.size()) {
            case 0:
                return null;
            case 1:
                return collection.aggregate(vetor_valores.get(0));
            case 2:
                return collection.aggregate(vetor_valores.get(0), vetor_valores.get(1));
            case 3:
                return collection.aggregate(vetor_valores.get(0), vetor_valores.get(1), vetor_valores.get(2));
            case 4:
                return collection.aggregate(vetor_valores.get(0), vetor_valores.get(1), vetor_valores.get(2), vetor_valores.get(3));
            case 5:
                return collection.aggregate(vetor_valores.get(0), vetor_valores.get(1), vetor_valores.get(2), vetor_valores.get(3), vetor_valores.get(4));
            case 6:
                return collection.aggregate(vetor_valores.get(0), vetor_valores.get(1), vetor_valores.get(2), vetor_valores.get(3), vetor_valores.get(4), vetor_valores.get(5));
        }

        return null;
    }

    public DBObject returnAggOperators(SelectClause select) {

        DBObject groupFields = new BasicDBObject();
        String atributo = "";
        Funcoes funcao = new Funcoes();
        //Quando existe apenas a função MAX() (ex) sem os atributos a serem buscados
        if (select.getGroupBy().isEmpty()) {
            groupFields.put("_id", "null");
        } else {
            DBObject tmpObject = new BasicDBObject();
            for (int i = 0; i < select.getGroupBy().size(); i++) {
                atributo = select.getGroupBy().get(i).getColumnName();
                tmpObject.put(atributo, "$" + atributo);
            }
            groupFields.put("_id", tmpObject);
        }

        for (int i = 0; i < select.getFuncoes().size(); i++) {
            if (!select.getFuncoes().get(i).getParametros().isEmpty()) {
                atributo = select.getFuncoes().get(i).getParametros().get(0);
            }
            switch (select.getFuncoes().get(i).getNome().toUpperCase()) {
                case "COUNT":
                    if (select.getFuncoes().get(i).isDistinct()) {
                        groupFields.put(atributo, new BasicDBObject("$sum", 1));
                    } else if (select.getFuncoes().get(i).getAlias() != null) {
                        groupFields.put(select.getFuncoes().get(i).getAlias(), new BasicDBObject("$sum", 1));
                    } else {
                        groupFields.put(select.getFuncoes().get(i).getNome_completo(), new BasicDBObject("$sum", 1));
                    }
                    break;
                default:
                    if (select.getFuncoes().get(i).getAlias() != null) {
                        groupFields.put(select.getFuncoes().get(i).getAlias(),
                                new BasicDBObject("$" + select.getFuncoes().get(i).getNome().toLowerCase(),
                                        "$" + atributo));
                    } else {
                        groupFields.put(select.getFuncoes().get(i).getNome_completo(),
                                new BasicDBObject("$" + select.getFuncoes().get(i).getNome().toLowerCase(), "$" + atributo));
                    }
                    break;
            }
        }
        DBObject group = new BasicDBObject("$group", groupFields);
        return group;
    }

    public static String retornaResultSet(AggregationOutput select_result, String header) throws JSQLParserException {

        String result_set = "";

        String json_str = select_result.results().toString();
        int tamanho_resultado = json_str.length();

        //resultado não vazio
        if (!json_str.equals("[ ]")) {
            //instancia um novo JSONObject passando a string como entrada
            JSONObject my_obj = new JSONObject(json_str.substring(1, tamanho_resultado - 1));
            my_obj.remove("_id");

            //Monta o Result Set        
            JSONArray array = new JSONArray(json_str);
            JSONObject my_obj_result_set;
            JSONArray array_header = new JSONArray("[" + header + "]");
            for (int i = 0; i < array.length(); i++) {
                result_set += "{";
                //instancia um novo JSONObject passando a string como entrada
                my_obj_result_set = array.getJSONObject(i);
                my_obj_result_set.remove("_id");

                for (int j = 0; j < array_header.length(); j++) {
                    String atributo_atual = array_header.get(j).toString();
                    if (my_obj_result_set.has(atributo_atual)) {
                        result_set += "'" + my_obj_result_set.get(atributo_atual) + "', ";
                    } else {
                        result_set += "'' , ";
                    }
                }
                result_set += "},";
            }
        }
        return result_set;
    }

    public static String retornaResultSetWithJoin(String select_result, String header) throws JSQLParserException {

        String result_set = "";

        String json_str = select_result;
        int tamanho_resultado = json_str.length();

        //resultado não vazio
        if (!json_str.equals("[ ]")) {
            //instancia um novo JSONObject passando a string como entrada
            JSONObject my_obj = new JSONObject(json_str.substring(1, tamanho_resultado - 1));
            my_obj.remove("_id");

            //Monta o Result Set        
            JSONArray array = new JSONArray(json_str);
            JSONObject my_obj_result_set;

            for (int i = 0; i < array.length(); i++) {
                result_set += "{";
                //instancia um novo JSONObject passando a string como entrada
                my_obj_result_set = array.getJSONObject(i);
                my_obj_result_set.remove("_id");

                JSONArray array_header = new JSONArray("[" + header + "]");

                for (int j = 0; j < array_header.length(); j++) {
                    String atributo_atual = array_header.get(j).toString();
                    if (my_obj_result_set.has(atributo_atual)) {
                        result_set += "'" + my_obj_result_set.get(atributo_atual) + "', ";
                    } else {
                        result_set += "'' , ";
                    }
                }
                result_set += "},";
            }
        }
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
                    AggregationQuery agg = new AggregationQuery();
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
                    header += "'" + selectStatement.getFuncoes().get(i).getAlias().toString() + "',";
                } else {
                    header += "'" + selectStatement.getFuncoes().get(i).getNome_completo() + "',";
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
