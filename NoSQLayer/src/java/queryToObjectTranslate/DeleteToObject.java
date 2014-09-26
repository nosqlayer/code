package queryToObjectTranslate;

import criteria.CriteriaIdentifier;
import deleteClause.DeleteClause;
import java.io.StringReader;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.delete.Delete;

/**
 *
 * @author fernando
 */
public class DeleteToObject {
    
    public DeleteClause deleteClause = new DeleteClause();
    
    public DeleteToObject(String sql) throws JSQLParserException{
        CCJSqlParserManager pm = new CCJSqlParserManager();
        net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));
        
        Delete deleteStatement = (Delete) statement;

        deleteClause.setTable(deleteStatement.getTable());
        
        if(deleteStatement.getWhere() != null){
            CriteriaIdentifier criteriaIdentifier = new CriteriaIdentifier();
            criteriaIdentifier.redirectQuery(deleteStatement);
            deleteClause.setCriteriaIdentifier(criteriaIdentifier);
        }         

    }
    
}
