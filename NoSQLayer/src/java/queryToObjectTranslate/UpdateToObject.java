package queryToObjectTranslate;

import criteria.CriteriaIdentifier;
import java.io.StringReader;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.update.Update;
import updateClause.UpdateClause;

public class UpdateToObject {
    
    public UpdateClause updateClause = new UpdateClause();
    
    public UpdateToObject(String sql) throws JSQLParserException{
        CCJSqlParserManager pm = new CCJSqlParserManager();
        net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));
        
        Update updateStatement = (Update) statement;
        
        for(int i=0;i< updateStatement.getColumns().size();i++){
            updateClause.addColumn((Column) updateStatement.getColumns().get(i));
            updateClause.addExpression((Expression) updateStatement.getExpressions().get(i));
            updateClause.setTable(updateStatement.getTable());          
        }
        
        if(updateStatement.getWhere() != null){
            CriteriaIdentifier criteriaIdentifier = new CriteriaIdentifier();
            criteriaIdentifier.redirectQuery(updateStatement);
            updateClause.setCriteriaIdentifier(criteriaIdentifier);
        }        
    }    
}
