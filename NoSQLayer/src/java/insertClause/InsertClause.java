/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package insertClause;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

/**
 *
 * @author fernando
 */
public class InsertClause {
    private Table table;
    private List<Column> columns = new ArrayList<>();
    private ExpressionList values;

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

    public ExpressionList getValues() {
        return values;
    }

    public void setValues(ExpressionList values) {
        this.values = values;
    }
}
