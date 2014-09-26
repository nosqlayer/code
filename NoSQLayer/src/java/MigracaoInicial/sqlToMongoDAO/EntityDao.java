package sqlToMongoDAO;
 
import BancoModel.Column;
import BancoModel.Table;
import ConfigMongoDB.MongoConnection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import java.util.Map;

public class EntityDao<T> {
 
    private DBCollection dbCollection;
     
    public void setCollection(String collection){
        this.dbCollection = MongoConnection.getInstance().getDB().getCollection(collection);
    }
 
    protected DBCollection getDbCollection() {
        return dbCollection;
    }
 
    public void save(Map<String, Object> mapEntity, Table tabela) {
        BasicDBObject document = new BasicDBObject(mapEntity); 
        dbCollection.save(document);
        
       // System.out.println("Save :> " + document);
    }
    
    public void ensureIndex(Table tabela){
        setCollection(tabela.getNome());
                //Atualiza os índices
        String str_indices = "{";

        for(Column coluna:tabela.getColunas()){
            if(coluna.isIsUnique()){
                str_indices += "\""+coluna.getNome()+"\" : 1,";
            }
        }
        str_indices = str_indices.substring(0, str_indices.length()-1);
        str_indices += "}";
        System.out.println(str_indices);
        if(!str_indices.equals("{}") && !str_indices.equals("}")) {
            DBObject indices = (DBObject) JSON.parse(str_indices);
            
            dbCollection.ensureIndex(indices, new BasicDBObject("unique", true));
        }
        
    }
}