package objectToMongoTranslate;

import consultaMongo.MongoConnection;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import insertClause.InsertClause;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.sf.jsqlparser.JSQLParserException;

public class InsertObjectTranslate {

    public DB database = MongoConnection.getInstance().getDB();

    public void executeMongoInsert(InsertClause insertStatement) throws JSQLParserException {

        String queryMongo = this.retornaConsultaMongo(insertStatement);
        DBObject objeto = (DBObject) JSON.parse(queryMongo);
        DBCollection collection = database.getCollection(insertStatement.getTable().getName());

        AutoIncrementInsert auto_inc = new AutoIncrementInsert();
        String next_id = auto_inc.getNextId(database, collection.getName());
        int id_final = Integer.parseInt(next_id);
        String atributo_auto_inc = auto_inc.getAtributoAutoInc(database, collection.getName());
        objeto.put(atributo_auto_inc, id_final);
        collection.insert(objeto);

    }

    public String retornaConsultaMongo(InsertClause insert) {
        String queryMongo;

        queryMongo = "{";
        for (int i = 0; i < insert.getColumns().size(); i++) {

            queryMongo += insert.getColumns().get(i).toString() + ": ";
            if (insert.getValues().getExpressions().get(i).toString().equals("NOW")) {
                queryMongo += "'" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()).toString() + "'";
            } else {
                queryMongo += insert.getValues().getExpressions().get(i).toString();
            }
            if (i != insert.getColumns().size() - 1) {
                queryMongo += ",";
            }
        }
        queryMongo += "}";
        return queryMongo;
    }
}
