package sqlToMongoConverter;

import BancoModel.Column;
import BancoModel.Database;
import BancoModel.Table;
import ConfigMongoDB.MongoConnection;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import jsonJava.JSONArray;
import jsonJava.JSONObject;
import sqlToMongoDAO.EntityDao;
import sqlToMongoDAO.TabelaDao;

public class Converter {
    /*Dados sobre o banco relacional*/

    static final String DATABASE = "w3c";
    static final String HOST = "localhost";
    static final int PORT = 3306;
    static final String USER = "root";
    static final String PASS = "root";
    public static DBCollection dbCollection;
    private static final String DBURL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE + "?user=" + USER + "&password=" + PASS + "&useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull";
    private static final String DBDRIVER = "org.gjt.mm.mysql.Driver";

    static {
        try {
            Class.forName(DBDRIVER).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
        }
    }

    private static Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(DBURL);
        } catch (Exception e) {
        }
        return connection;
    }

    public static void main(String[] args) throws SQLException {
        Connection con = getConnection();
        Database dataBase = new Database();
        dataBase.setNome(DATABASE);
        dataBase.setHost(HOST);
        dataBase.setPort(PORT);

        try {
            DatabaseMetaData dbmd = con.getMetaData();
            
            ResultSet result_tables = dbmd.getTables(null, null, null, null);
            System.out.println("Montando os objetos");
            while (result_tables.next()) {
                Table tabela = new Table();
                tabela.setNome(result_tables.getString("TABLE_NAME"));
                ResultSet result_columns = dbmd.getColumns(null, null, tabela.getNome(), null);

                ResultSet foreignKeys = dbmd.getImportedKeys(null, null, result_tables.getString("TABLE_NAME"));
                ArrayList<String> colunasContainsForeignKey = new ArrayList<>();
                ArrayList<String> colunasReferenciadas = new ArrayList<>();
                ArrayList<String> tabelasReferenciadas = new ArrayList<>();
                while (foreignKeys.next()) {
                    String tabelaChaveEstrangeira = foreignKeys.getString("FKTABLE_NAME");
                    String colunaChaveEstrangeira = foreignKeys.getString("FKCOLUMN_NAME");
                    String tabelaReferenciadaChaveEstrangeira = foreignKeys.getString("PKTABLE_NAME");
                    String colunaReferenciadaChaveEstrangeira = foreignKeys.getString("PKCOLUMN_NAME");
                    colunasContainsForeignKey.add(colunaChaveEstrangeira);
                    colunasReferenciadas.add(colunaReferenciadaChaveEstrangeira);
                    tabelasReferenciadas.add(tabelaReferenciadaChaveEstrangeira);
                    /*System.out.println(tabelaChaveEstrangeira+" - "+colunaChaveEstrangeira+
                     " - "+tabelaReferenciadaChaveEstrangeira+" - "+
                     colunaReferenciadaChaveEstrangeira);*/
                }

                while (result_columns.next()) {
                    Column coluna = new Column();
                    coluna.setNome(result_columns.getString("COLUMN_NAME"));
                    coluna.setTipoColuna(result_columns.getString("TYPE_NAME"));
                    if (result_columns.getString("IS_AUTOINCREMENT").equals("YES")) {
                        coluna.setPrimaryKey(true);
                    }
                    ResultSet index_information = dbmd.getIndexInfo(null, null, tabela.getNome(), true, true);
                    /*Percorre a lista de índices da tabela*/
                    while (index_information.next()) {

                        String index_column = index_information.getString("COLUMN_NAME");
                        if (index_column.equals(result_columns.getString("COLUMN_NAME"))) {
                            coluna.setIsUnique(true);
                        }
                    }

                    for (int i = 0; i < colunasContainsForeignKey.size(); i++) {
                        if (coluna.getNome().equals(colunasContainsForeignKey.get(i))) {
                            coluna.setIsForeignKey(true);
                            coluna.setColunaForeignKeyReferency(colunasReferenciadas.get(i));
                            coluna.setTableForeignKeyReferency(tabelasReferenciadas.get(i));
                        }
                    }

                    tabela.addColunas(coluna);
                }
                dataBase.addTabela(tabela);
            }
        } catch (SQLException e) {
            e.getSQLState();
        }
        System.out.println("Objetos montados");
        /*
         System.out.println("Verificando as chaves estrangeiras");
         for (Table tabela : dataBase.getTabelas()) {
         System.out.println("TABELA: " +tabela.getNome());
         for(Column coluna : tabela.getColunas()){
         System.out.println("Coluna: "+coluna.getNome());
         if(coluna.isIsForeignKey()){
         System.out.println("FOREIGN KEY");
         System.out.println("Tabela referenciada: "+coluna.getTableForeignKeyReferency());
         System.out.println("Coluna referenciada: "+coluna.getColunaForeignKeyReferency());
         }
         }
         }
         */
        System.out.println("Criando metadados");
        dbCollection = MongoConnection.getInstance().getDB().getCollection("metadata");
        // System.out.println("Informações sobre as tabelas");
        for (Table tabela : dataBase.getTabelas()) {
            // System.out.println("Tabela: " + tabela.getNome());
            BasicDBObject table_metadata = new BasicDBObject("table", tabela.getNome());
            ArrayList<String> columns = new ArrayList<>();
            for (Column coluna : tabela.getColunas()) {
                if (coluna.isPrimaryKey()) {
                    table_metadata.append("auto_inc", coluna.getNome());
                }
                // System.out.println(coluna.getNome());
                columns.add(coluna.getNome());
                //System.out.println("   UNICO? "+coluna.isIsUnique());
            }
            table_metadata.append("columns", columns);
            dbCollection.save(table_metadata);
        }
        System.out.println("Metadados criados");
        Connection connection = getConnection();
        //Realizar consultas nas tabelas buscando os dados

        for (Table tabela : dataBase.getTabelas()) {
            int valor_salvo = 1;
            int subPartes = 10;
            long inicio = 0;
            
            long total = 20198310;
            long sub_total = total/subPartes;

            for (int i = 0; i < subPartes; i++) {
                String sql2 = "SELECT * FROM " + tabela.getNome() + " LIMIT "+inicio+","+sub_total;
                try (PreparedStatement stmt2 = connection.prepareStatement(sql2)) {
                    ResultSet resultado2 = stmt2.executeQuery();
                    while (resultado2.next()) {

                        new TabelaDao().save(tabela, resultado2);
                        System.out.println(tabela.getNome() + " - " + valor_salvo);
                        valor_salvo++;
                    }
                }
                inicio = inicio+sub_total;
            }

            /*
             int cont = 0;
             while (resultado.next()) {
             if (tabela.getNome().equals("user")) {
             System.out.println(cont);
             cont++;
             }
             new TabelaDao().save(tabela, resultado);
             }*/
            /*
             int total_tmp = 154167620 / 100;
             int inicio = 5792694;
             int cont = 0;
             while (cont < 100) {
             String sql2 = "SELECT * FROM " + tabela.getNome() + " LIMIT " + inicio + "," + total_tmp;
             try (PreparedStatement stmt2 = connection.prepareStatement(sql2)) {
             ResultSet resultado2 = stmt2.executeQuery();
             while (resultado2.next()) {

             new TabelaDao().save(tabela, resultado2);
             System.out.println(valor_salvo);
             valor_salvo++;
             }
             }
             cont++;
             inicio = cont*total_tmp;
             }
             */
            
             DBObject criteria = (DBObject) JSON.parse("{table:'" + tabela.getNome() + "'}");
             DBObject projection = (DBObject) JSON.parse("{auto_inc:1}");
             BasicDBObject criteria_ppl = new BasicDBObject("$match", criteria);
             BasicDBObject projection_ppl = new BasicDBObject("$project", projection);
             AggregationOutput busca_auto_inc = dbCollection.aggregate(criteria_ppl, projection_ppl);
             String json_str = busca_auto_inc.results().toString();

             JSONArray array = new JSONArray(json_str);
             JSONObject result_set;
             String valor_auto_inc = null;
             for (int i = 0; i < array.length(); i++) {
             result_set = array.getJSONObject(i);
             result_set.remove("_id");
             if (result_set.has("auto_inc")) {
             valor_auto_inc = result_set.get("auto_inc").toString();
             }
             }

             String find_max_auto_inc = "SELECT MAX(" + valor_auto_inc + ") AS maximo_id FROM " + tabela.getNome() + " ";

             int maximo_id = 0;
             try (PreparedStatement stmt = connection.prepareStatement(find_max_auto_inc)) {
             ResultSet resultado = stmt.executeQuery();

             while (resultado.next()) {
             maximo_id = resultado.getInt("maximo_id");
             }
             }
             String sequence_field = "seq"; // the name of the field which holds the sequence
             DBCollection seq = MongoConnection.getInstance().getDB().getCollection("seq"); // get the collection (this will create it if needed)
             DBObject new_seq = (DBObject) JSON.parse("{'_id':'" + tabela.getNome() + "', 'seq':" + maximo_id + "}");
             seq.insert(new_seq);

             new EntityDao<>().ensureIndex(tabela);
        }
    }
}
