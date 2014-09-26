package queryToObjectTranslate;

import insertClause.InsertClause;
import java.io.StringReader;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.insert.Insert;

public class InsertToObject {
    
    public InsertClause insertClause = new InsertClause();
    
    public InsertToObject(String sql) throws JSQLParserException{
        CCJSqlParserManager pm = new CCJSqlParserManager();
        net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));
        
        Insert insertStatement = (Insert) statement;
        
        insertClause.setTable(insertStatement.getTable());
        insertClause.setColumns(insertStatement.getColumns());
        insertClause.setValues(((ExpressionList) insertStatement.getItemsList()));        
    }   
}
