package updateClause;

import criteria.CriteriaIdentifier;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

public class UpdateClause {
    private List<Column> columns = new ArrayList<>();
    private List<Expression> expressions = new ArrayList<>();
    private Table table = new Table();
    private CriteriaIdentifier criteriaIdentifier = new CriteriaIdentifier();

    public CriteriaIdentifier getCriteriaIdentifier() {
        return criteriaIdentifier;
    }

    public void setCriteriaIdentifier(CriteriaIdentifier criteriaIdentifier) {
        this.criteriaIdentifier = criteriaIdentifier;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    public void setExpressions(List<Expression> expressions) {
        this.expressions = expressions;
    }
    
    
    public void addColumn(Column column){
        this.columns.add(column);
    }
    
    public void addExpression(Expression expression){
        this.expressions.add(expression);
    }
}
