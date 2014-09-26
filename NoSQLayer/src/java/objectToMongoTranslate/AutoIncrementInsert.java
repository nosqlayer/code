/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package objectToMongoTranslate;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 *
 * @author fernando
 */
public class AutoIncrementInsert {

    public String getNextId(DB db, String seq_name) {
        String sequence_collection = "seq"; // the name of the sequence collection
        String sequence_field = "seq"; // the name of the field which holds the sequence

        DBCollection seq = db.getCollection(sequence_collection); // get the collection (this will create it if needed)

        // this object represents your "query", its analogous to a WHERE clause in SQL
        DBObject query = new BasicDBObject();
        query.put("_id", seq_name); // where _id = the input sequence name

        // this object represents the "update" or the SET blah=blah in SQL
        DBObject change = new BasicDBObject(sequence_field, 1);
        DBObject update = new BasicDBObject("$inc", change); // the $inc here is a mongodb command for increment

        // Atomically updates the sequence field and returns the value for you
        DBObject res = seq.findAndModify(query, new BasicDBObject(), new BasicDBObject(), false, update, true, true);
        return res.get(sequence_field).toString();
    }
    
     public String getAtributoAutoInc(DB db, String collection) {
        String sequence_collection = "metadata"; // the name of the sequence collection

        DBCollection seq = db.getCollection(sequence_collection); // get the collection (this will create it if needed)

        // this object represents your "query", its analogous to a WHERE clause in SQL
        DBObject query = new BasicDBObject();
        query.put("table", collection); // where _id = the input sequence name

        DBObject res  = seq.findOne(query);
        return res.get("auto_inc").toString();
    }
}
