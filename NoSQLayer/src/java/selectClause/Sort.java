/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package selectClause;

/**
 *
 * @author fernando
 */
public class Sort {
    private String atributo;
    private int ordem;          //ASC(1) ou DESC (-1)
    private String tableReferenciada;

    public String getTableReferenciada() {
        return tableReferenciada;
    }

    public void setTableReferenciada(String tableReferenciada) {
        this.tableReferenciada = tableReferenciada;
    }
    
    public String getAtributo() {
        return atributo;
    }

    public void setAtributo(String atributo) {
        this.atributo = atributo;
    }

    public int getOrdem() {
        return ordem;
    }

    public void setOrdem(int ordem) {
        this.ordem = ordem;
    }
}
