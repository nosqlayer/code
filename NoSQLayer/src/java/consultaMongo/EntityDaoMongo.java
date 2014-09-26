package consultaMongo;
import com.mongodb.DBCollection;

public class EntityDaoMongo {
 
    private DBCollection dbCollection;
     
    public void setCollection(String collection){
        this.dbCollection = MongoConnection.getInstance().getDB().getCollection(collection);
    }
 
    protected DBCollection getDbCollection() {
        return dbCollection;
    }
}