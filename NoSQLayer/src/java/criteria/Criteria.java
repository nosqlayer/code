package criteria;

/**
 *
 * @author fernando
 */
public class Criteria {
    private String leftExpression;
    private String rightExpression;
    private String operador;

    public String getLeftExpression() {
        return leftExpression;
    }

    public void setLeftExpression(String leftExpression) {
        this.leftExpression = leftExpression;
    }

    public String getRightExpression() {
        return rightExpression;
    }

    public void setRightExpression(String rightExpression) {
        this.rightExpression = rightExpression;
    }

    public String getOperador() {
        return operador;
    }

    public void setOperador(String operador) {
        this.operador = operador;
    }   
}
