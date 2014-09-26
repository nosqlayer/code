package BancoModel;

import java.util.ArrayList;
import java.util.Collection;

public class Table {
    private String nome;
    private Collection<Column> colunas = new ArrayList<>();

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Collection<Column> getColunas() {
        return colunas;
    }

    public void addColunas(Column coluna) {
        this.colunas.add(coluna);
    }
    
}
