
package sqlToMongoConverter;

import BancoModel.Column;
import BancoModel.Table;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class TableMapper {
    
    public Map<String, Object> converterToMap(Table tabela, ResultSet tupla) throws SQLException {
        Map<String, Object> mapTabela = new HashMap<>();
        for(Column coluna: tabela.getColunas()){
            switch (coluna.getTipoColuna().toUpperCase()) {
                case "TINYINT":
                case "TINYINT UNSIGNED":
                case "SMALLINT":
                case "SMALLINT UNSIGNED":
                case "MEDIUMINT":
                case "MEDIUMINT UNSIGNED":
                case "INT":
                case "INT UNSIGNED":
                case "BIGINT":
                case "BIGINT UNSIGNED":
                    mapTabela.put(coluna.getNome(), tupla.getLong(coluna.getNome()));
                    break;
                case "DECIMAL":
                case "FLOAT":
                case "DOUBLE":
                case "REAL":
                    mapTabela.put(coluna.getNome(), tupla.getFloat(coluna.getNome()));
                    break;
                case "BIT":
                case "SERIAL":
                    mapTabela.put(coluna.getNome(), tupla.getInt(coluna.getNome()));
                    break;
                case "BOOLEAN":
                    mapTabela.put(coluna.getNome(), tupla.getBoolean(coluna.getNome()));
                    break;
                case "DATE":
                    mapTabela.put(coluna.getNome(), tupla.getString(coluna.getNome()));
                    break;
               default:
                    mapTabela.put(coluna.getNome(), tupla.getString(coluna.getNome()));
                    break;
            }
        }
        return mapTabela;
    }
}
