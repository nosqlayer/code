package objectToMongoTranslate;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import consultaMongo.MongoConnection;
import java.util.List;
import joinObjectToMongoTranslate.InnerJoinExecute;
import joinObjectToMongoTranslate.LeftJoinExecute;
import joinObjectToMongoTranslate.RightJoinExecute;
import net.sf.jsqlparser.JSQLParserException;
import selectClause.ProjectionParams;
import selectClause.SelectClause;
import selectClause.Sort;

public class SelectObjectWithJoinTranslate {

    public static DB database = MongoConnection.getInstance().getDB();

    public String executeMongoSelect(SelectClause selectStatement, String header) throws JSQLParserException {

        String subdocumento = possui_subdocumento(selectStatement);
        //Se possui subdocumento
        if (!subdocumento.equals("")) {
            switch (selectStatement.getJoinList().get(0).getTipoJoin()) {
                case "INNER":
                    InnerJoinExecute innerJoin = new InnerJoinExecute();
                    return innerJoin.executeInnerJoinSubDoc(selectStatement, header, subdocumento);
                case "LEFT":
                    LeftJoinExecute leftJoin = new LeftJoinExecute();
                    return leftJoin.executeLeftJoinSubDoc(selectStatement, header, subdocumento);
                case "RIGHT":
                    System.out.println("right sub doc");
                    RightJoinExecute rightJoin = new RightJoinExecute();
                    return rightJoin.executeRightJoinSubDoc(selectStatement, header, subdocumento);
            }
        } else {
            System.out.println("nao sub");
            switch (selectStatement.getJoinList().get(0).getTipoJoin()) {
                case "INNER":
                    InnerJoinExecute innerJoin = new InnerJoinExecute();
                    return innerJoin.executeInnerJoin(selectStatement, header);
                case "LEFT":
                    LeftJoinExecute leftJoin = new LeftJoinExecute();
                    return leftJoin.executeLeftJoin(selectStatement, header);
                case "RIGHT":
                    RightJoinExecute rightJoin = new RightJoinExecute();
                    return rightJoin.executeRightJoin(selectStatement, header);
            }
        }

        return "";
    }

    public String possui_subdocumento(SelectClause selectStatement) {
        DBCollection join_collection = database.getCollection("join_tables");
        String table_left = selectStatement.getJoinList().get(0).getTableLeftExpression();
        String table_right = selectStatement.getJoinList().get(0).getTableRightExpression();
        String subdocumento;

        //Buscando somente 1 subdoc na tabela left por enquanto
        DBObject search_table = new BasicDBObject("document", table_left);
        DBObject embbed_doc = new BasicDBObject("embbed", 1);
        DBCursor procura_embbed = join_collection.find(search_table, embbed_doc);

        if (procura_embbed.size() != 0) {
            while (procura_embbed.hasNext()) {
                subdocumento = procura_embbed.next().get("embbed").toString();

                if (subdocumento.equals(table_right)) {
                    return subdocumento;
                }
            }
        } else {
            //Procura subdoc da tabela esquerda
            DBObject search_table_right = new BasicDBObject("document", table_right);
            DBObject embbed_doc_left = new BasicDBObject("embbed", 1);
            DBCursor procura_embbed_right = join_collection.find(search_table_right, embbed_doc_left);
            while (procura_embbed_right.hasNext()) {
                subdocumento = procura_embbed_right.next().get("embbed").toString();
                if (subdocumento.equals(table_left)) {
                    return subdocumento;
                }
            }
        }

        return "";
    }
    
    public DBCursor result_consulta(SelectClause selectStatement, String selectedTable) {
        String alias_table, table;
        DBCollection collection;
        List<ProjectionParams> project_colecao;
        DBCursor dbCursor = null;

        if (selectedTable.equals("left")) {
            //Realiza  a primeira consulta do Join na primeira tabela encontrada
            alias_table = selectStatement.getJoinList().get(0).getTableAliasLeftExpression();
            if (alias_table != null) {
                table = selectStatement.convertAliasToName(alias_table);
            } else {
                table = selectStatement.getJoinList().get(0).getTableLeftExpression();
            }
        } else {
            alias_table = selectStatement.getJoinList().get(0).getTableAliasRightExpression();
            if (alias_table != null) {
                table = selectStatement.convertAliasToName(alias_table);
            } else {
                table = selectStatement.getJoinList().get(0).getTableRightExpression();
            }
        }

        collection = database.getCollection(table);

        //Retorna a projeção em relação a uma tabela
        project_colecao = selectStatement.retornaAtributosPorAlias(alias_table);
        if (project_colecao == null) {
            project_colecao = selectStatement.retornaAtributosPorNome(table);
        }

        ProjectionParams atributo_juncao = new ProjectionParams();
        BasicDBObject projection = null;

        if (!returnProjection(project_colecao).equals("*")) {
            projection = returnProjection(project_colecao);
        }

        dbCursor = collection.find(selectStatement.getCriteriaIdentifier().whereQuery, projection);

        return dbCursor;
    }

    public DBCursor result_consulta_with_Order(SelectClause selectStatement, String selectedTable, Sort ordenacao) {
        String alias_table, table;
        DBCollection collection;
        List<ProjectionParams> project_colecao;
        DBCursor dbCursor = null;

        table = ordenacao.getTableReferenciada();
        collection = database.getCollection(table);
        DBObject order = (DBObject) JSON.parse(retornaOrderJoin(ordenacao));
        alias_table = selectStatement.convertNameToAlias(table);

        //Retorna a projeção em relação a uma tabela
        //No caso de Customers.Customer
        if (alias_table != null) {
            project_colecao = selectStatement.retornaAtributosPorAlias(alias_table);
        } else {
            project_colecao = selectStatement.retornaAtributosPorNome(table);
        }
        BasicDBObject projection = null;

        if (returnProjection(project_colecao) != null) {
            projection = returnProjection(project_colecao);
        } else {
            projection = new BasicDBObject("_id", 0);
        }

        dbCursor = collection.find(selectStatement.getCriteriaIdentifier().whereQuery, projection).sort(order);

        return dbCursor;
    }

    public BasicDBObject returnProjection(List<ProjectionParams> project_params) {
        BasicDBObject fields = new BasicDBObject();
        if (project_params.get(0).getName().equals("*")) {
            return null;
        } else {
            //Verifica se o item buscado é *
            if (project_params.size() > 0) {
                for (int i = 0; i < project_params.size(); i++) {
                    if (project_params.get(i).getAlias() != null) {
                        String alias = project_params.get(i).getAlias();
                        fields.put(project_params.get(i).getName(), alias);
                    } else {
                        fields.put(project_params.get(i).getName(), 1);
                    }
                }
            } else {
                return null;
            }
            fields.put("_id", 0);
            return fields;
        }
    }

    public String retornaOrderJoin(Sort ordenacao) {

        String queryMongo = "{" + ordenacao.getAtributo() + ": " + ordenacao.getOrdem() + "}";

        return queryMongo;
    }
}
