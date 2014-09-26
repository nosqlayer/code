package objectToMongoTranslate;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import consultaMongo.MongoConnection;
import net.sf.jsqlparser.JSQLParserException;
import queryInterceptor.QueryInterceptor;
import static queryInterceptor.QueryInterceptor.database;
import selectClause.SelectClause;

public class SelectSimpleObjectTranslate {

    

    public DBCursor executeMongoSelect(SelectClause selectStatement) throws JSQLParserException{
        QueryInterceptor.database = MongoConnection.getInstance().getDB();
        //Considerando apenas uma coleçao
        DBCollection collection = database.getCollection(selectStatement.getTablesQueried().get(0).getName());
        DBCursor dbCursor;
        DBObject order = (DBObject) JSON.parse(retornaOrder(selectStatement));
        int limit = 0, offset = 0;
        BasicDBObject projection, projection_ppl;

        if (returnProjection(selectStatement) != null) {
            projection = returnProjection(selectStatement);
        } else {
            projection = new BasicDBObject("_id", 0);
        }

        if (selectStatement.getLimit() != null) {
            limit = (int) selectStatement.getLimit().getRowCount();
            offset = (int) selectStatement.getLimit().getOffset();
        }
        if (order != null) {
            dbCursor = collection.find(selectStatement.getCriteriaIdentifier().whereQuery, projection).sort(order).skip(offset).limit(limit);
        } else {
            dbCursor = collection.find(selectStatement.getCriteriaIdentifier().whereQuery, projection).skip(offset).limit(limit);
        }

        return dbCursor;
    }

    public BasicDBObject returnProjection(SelectClause select) {
        BasicDBObject fields = new BasicDBObject();
        if (select.getTablesQueried().get(0).isIsAllColumns()) {
            return null;
        } else {
            //Verifica se o item buscado é *
            if (select.getTablesQueried().get(0).getParams_projecao().size() > 0) {
                if (!(select.getTablesQueried().get(0).isIsAllColumns())) {
                    for (int i = 0; i < select.getTablesQueried().get(0).getParams_projecao().size(); i++) {
                        if (select.getTablesQueried().get(0).getParams_projecao().get(i).getAlias() != null) {
                            String alias = select.getTablesQueried().get(0).getParams_projecao().get(i).getAlias();
                            fields.put(select.getTablesQueried().get(0).getParams_projecao().get(i).getName(), alias);
                        } else {
                            fields.put(select.getTablesQueried().get(0).getParams_projecao().get(i).getName(), 1);
                        }
                    }
                }
            } else {
                return null;
            }
        }
        fields.put("_id", 0);
        return fields;
    }

    public String retornaOrder(SelectClause select) {

        String queryMongo = null;

        if (select.getOrdenacao().size() > 0) {
            queryMongo = "{";

            for (int i = 0; i < select.getOrdenacao().size(); i++) {
                queryMongo += select.getOrdenacao().get(i).getAtributo() + ": " + select.getOrdenacao().get(i).getOrdem() + ",";
            }
            queryMongo += "}";
        }

        return queryMongo;
    }
}
