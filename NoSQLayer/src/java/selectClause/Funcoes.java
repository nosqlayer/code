/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package selectClause;

import java.util.ArrayList;

/**
 *
 * @author fernando
 */
public class Funcoes {
    public String nome;
    public String alias;
    public boolean allColumns;
    public boolean distinct;
    public String nome_completo;
    public ArrayList<String> parametros = new ArrayList<>();

    public String getNome_completo() {
        return nome_completo;
    }

    public void setNome_completo(String nome_completo) {
        this.nome_completo = nome_completo;
    }

    public ArrayList<String> getParametros() {
        return parametros;
    }
    
    public void addParametros(String parametro){
        parametros.add(parametro);
    }

    public void setParametros(ArrayList<String> parametros) {
        this.parametros = parametros;
    }   
    
    public boolean isAllColumns() {
        return allColumns;
    }

    public void setAllColumns(boolean allColumns) {
        this.allColumns = allColumns;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }    

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
