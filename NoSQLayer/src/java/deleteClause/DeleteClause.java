
package deleteClause;

import criteria.CriteriaIdentifier;
import net.sf.jsqlparser.schema.Table;
 
public class DeleteClause {
    private Table table;
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

}
