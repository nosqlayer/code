package sqlToMongoDAO;

import BancoModel.Table;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import sqlToMongoConverter.TableMapper;

public class TabelaDao extends EntityDao<Table>{
       
    public void save(Table tabela, ResultSet tupla) throws SQLException {
                    //Seta a coleção
        setCollection(tabela.getNome());
        Map<String, Object> mapTabela =  new TableMapper().converterToMap(tabela,tupla);
        save(mapTabela, tabela);
        
        //System.out.println("Save > "+tupla);
    }
 
}
