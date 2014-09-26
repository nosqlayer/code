package objectToMongoTranslate;

import com.mongodb.CommandResult;
import consultaMongo.MongoConnection;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;
import deleteClause.DeleteClause;
import net.sf.jsqlparser.JSQLParserException;

public class DeleteObjectTranslate {
    public DB database = MongoConnection.getInstance().getDB();
    
    public void executeMongoDelete(DeleteClause deleteObject) throws JSQLParserException{

        DBObject objeto = retornaMongoObjectDelete(deleteObject);
        DBCollection collection = database.getCollection(deleteObject.getTable().getName());
        collection.remove(objeto); 
    }
    
    public DBObject retornaMongoObjectDelete(DeleteClause deleteStatement) throws JSQLParserException{
        String queryMongo = deleteStatement.getCriteriaIdentifier().whereQuery.toString();

        return (DBObject) JSON.parse(queryMongo);
    }
}
