/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package selectClause;

/**
 *
 * @author fernando
 */
public class JoinClause {
    private String tipoJoin;
    private String tableAliasLeftExpression;
    private String tableLeftExpression;
    private ProjectionParams leftExpression;
    private String tableAliasRightExpression;
    private String tableRightExpression;
    private ProjectionParams rightExpression;
    private String operador;

    public String getTableLeftExpression() {
        return tableLeftExpression;
    }

    public void setTableLeftExpression(String tableLeftExpression) {
        this.tableLeftExpression = tableLeftExpression;
    }

    public String getTableRightExpression() {
        return tableRightExpression;
    }

    public void setTableRightExpression(String tableRightExpression) {
        this.tableRightExpression = tableRightExpression;
    }
 
    public ProjectionParams getLeftExpression() {
        return leftExpression;
    }

    public String getTableAliasLeftExpression() {
        return tableAliasLeftExpression;
    }

    public void setTableAliasLeftExpression(String tableAliasLeftExpression) {
        this.tableAliasLeftExpression = tableAliasLeftExpression;
    }

    public String getTableAliasRightExpression() {
        return tableAliasRightExpression;
    }

    public void setTableAliasRightExpression(String tableAliasRightExpression) {
        this.tableAliasRightExpression = tableAliasRightExpression;
    }

    public void setLeftExpression(ProjectionParams leftExpression) {
        this.leftExpression = leftExpression;
    }

    public ProjectionParams getRightExpression() {
        return rightExpression;
    }

    public void setRightExpression(ProjectionParams rightExpression) {
        this.rightExpression = rightExpression;
    }

    public String getOperador() {
        return operador;
    }

    public void setOperador(String operador) {
        this.operador = operador;
    }
    
    public String getTipoJoin() {
        return tipoJoin;
    }

    public void setTipoJoin(String tipoJoin) {
        this.tipoJoin = tipoJoin;
    }

    
}
