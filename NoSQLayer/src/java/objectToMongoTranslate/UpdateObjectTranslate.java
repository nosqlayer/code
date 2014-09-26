package objectToMongoTranslate;

import consultaMongo.MongoConnection;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import net.sf.jsqlparser.JSQLParserException;
import updateClause.UpdateClause;

public class UpdateObjectTranslate {
    
    public DB database = MongoConnection.getInstance().getDB();
    
    public void executeMongoUpdate(UpdateClause updateObject) throws JSQLParserException{

        DBCollection collection = database.getCollection(updateObject.getTable().getWholeTableName());
        DBObject criteria;
        criteria = (DBObject) JSON.parse(updateObject.getCriteriaIdentifier().whereQuery.toString());
        
                //Criando a clausua SET
        BasicDBObject setQuery = returnSetQuery(updateObject);
        BasicDBObject updateQuery = new BasicDBObject();
        updateQuery.append("$set", setQuery);
                collection.updateMulti(criteria, updateQuery);
        //collection.updateMulti(criteria,updateQuery,true,true );
        
    }
    
    public BasicDBObject returnSetQuery(UpdateClause updateStatement){
        String queryMongo = this.retornaSetDocument(updateStatement); 
        return (BasicDBObject)  JSON.parse(queryMongo);
    }
    
    public String retornaSetDocument(UpdateClause update){
        String queryMongo;
        
        queryMongo = "{";
        
        for(int i=0;i<update.getColumns().size();i++){
            String coluna = update.getColumns().get(i).getWholeColumnName();
            String valor = update.getExpressions().get(i).toString();
            queryMongo += coluna +":"+valor;
            if(i!= update.getColumns().size()-1) {
                queryMongo += ",";
            }
        } 
        
        queryMongo += "}";        
        return queryMongo;
    }
    
}
