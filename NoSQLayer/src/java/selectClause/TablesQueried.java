
package selectClause;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.expression.Function;

/**
 *
 * @author fernando
 */
public class TablesQueried {
    private String name;
    private String alias;
    private List<ProjectionParams> params_projecao = new ArrayList<>();
    private List<Function> funcoes = new ArrayList<>();
    private List<String> aliasFuncoes = new ArrayList<>();
    private boolean isAllColumns;
            
    public boolean isIsAllColumns() {
        return isAllColumns;
    }

    public void setIsAllColumns(boolean isAllColumns) {
        this.isAllColumns = isAllColumns;
    }

    public List<String> getAliasFuncoes() {
        return aliasFuncoes;
    }

    public void setAliasFuncoes(List<String> aliasFuncoes) {
        this.aliasFuncoes = aliasFuncoes;
    }     

    public List<Function> getFuncoes() {
        return funcoes;
    }

    public void setFuncoes(List<Function> funcoes) {
        this.funcoes = funcoes;
    }
    
    public void addFuncao(Function function){
        this.getFuncoes().add(function);
    }
    
    public void addAliasFuncoes(String aliasFuncao){
        this.aliasFuncoes.add(aliasFuncao);
    }
    
    public void addParametro(ProjectionParams parametro){
        this.params_projecao.add(parametro);
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public List<ProjectionParams> getParams_projecao() {
        return params_projecao;
    }

    public void setParams_projecao(List<ProjectionParams> params_projecao) {
        this.params_projecao = params_projecao;
    }
}
