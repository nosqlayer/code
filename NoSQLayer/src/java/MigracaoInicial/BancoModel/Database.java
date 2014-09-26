package BancoModel;

import java.util.ArrayList;
import java.util.Collection;

public class Database {
    private String nome;
    private String host;
    private int port;
    private Collection<Table> tabelas = new ArrayList<>();
    
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
    

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Collection<Table> getTabelas() {
        return tabelas;
    }

    public void addTabela(Table tabela) {
        this.tabelas.add(tabela);
    }    
}
