package BancoModel;

public class Column {

    private String nome;
    private String tipoColuna;
    private boolean primaryKey;
    private boolean isUnique = false;
    private boolean isForeignKey = false;
    private String tableForeignKeyReferency;
    private String colunaForeignKeyReferency;

    public String getColunaForeignKeyReferency() {
        return colunaForeignKeyReferency;
    }

    public void setColunaForeignKeyReferency(String colunaForeignKeyReferency) {
        this.colunaForeignKeyReferency = colunaForeignKeyReferency;
    }

    public boolean isIsForeignKey() {
        return isForeignKey;
    }

    public void setIsForeignKey(boolean isForeignKey) {
        this.isForeignKey = isForeignKey;
    }

    public String getTableForeignKeyReferency() {
        return tableForeignKeyReferency;
    }

    public void setTableForeignKeyReferency(String tableForeignKeyReferency) {
        this.tableForeignKeyReferency = tableForeignKeyReferency;
    }

    public boolean isIsUnique() {
        return isUnique;
    }

    public void setIsUnique(boolean isUnique) {
        this.isUnique = isUnique;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipoColuna() {
        return tipoColuna;
    }

    public void setTipoColuna(String tipoColuna) {
        this.tipoColuna = tipoColuna;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }
}
