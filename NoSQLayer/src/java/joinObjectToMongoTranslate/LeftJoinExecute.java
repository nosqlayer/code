package joinObjectToMongoTranslate;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.util.List;
import jsonJava.JSONArray;
import objectToMongoTranslate.SelectObjectWithJoinTranslate;
import selectClause.ProjectionParams;
import selectClause.SelectClause;

public class LeftJoinExecute {

    public String executeLeftJoin(SelectClause selectStatement, String header) {
        SelectObjectWithJoinTranslate select_join = new SelectObjectWithJoinTranslate();
        ProjectionParams first_param = null, second_param = null;
        DBCursor output_agg1 = null, output_agg2 = null;
        String table_first, table_second;
        boolean encontrou_join = false;
        String join_result = "";

        //Tabela do Left Join
        String table_left_join = selectStatement.getTablesQueried().get(0).getName();
        boolean first_is_left = false;      //Identifica se a primeira tabela é tambem a left

        //Verificar criterios de ordenação para poder setar corretamente quem é left ou right
        if (!selectStatement.getOrdenacao().isEmpty()) {
            String alias_table_first = selectStatement.getJoinList().get(0).getTableAliasLeftExpression();
            if (alias_table_first == null) {
                table_first = selectStatement.getJoinList().get(0).getTableLeftExpression();
            } else {
                table_first = selectStatement.convertAliasToName(alias_table_first);
            }
            String alias_table_second = selectStatement.getJoinList().get(0).getTableAliasRightExpression();
            if (alias_table_second == null) {
                table_second = selectStatement.getJoinList().get(0).getTableRightExpression();
            } else {
                table_second = selectStatement.convertAliasToName(alias_table_second);
            }

            if (selectStatement.getOrdenacao().size() == 1) {

                output_agg1 = select_join.result_consulta_with_Order(selectStatement, "", selectStatement.getOrdenacao().get(0));

                if (selectStatement.getOrdenacao().get(0).getTableReferenciada().equals(table_left_join)) {
                    first_is_left = true;
                } else {
                    first_is_left = false;
                }

                if (selectStatement.getOrdenacao().get(0).getTableReferenciada().equals(table_first)) {
                    output_agg2 = select_join.result_consulta(selectStatement, "right");
                    first_param = selectStatement.getJoinList().get(0).getLeftExpression();
                    second_param = selectStatement.getJoinList().get(0).getRightExpression();
                } else {
                    output_agg2 = select_join.result_consulta(selectStatement, "left");
                    first_param = selectStatement.getJoinList().get(0).getRightExpression();
                    second_param = selectStatement.getJoinList().get(0).getLeftExpression();
                }
            } else if (selectStatement.getOrdenacao().size() == 2) {
                //TODO
            }
        } else {
            first_param = selectStatement.getJoinList().get(0).getLeftExpression();
            second_param = selectStatement.getJoinList().get(0).getRightExpression();

            if (table_left_join.equals(selectStatement.getJoinList().get(0).getTableLeftExpression())) {
                //Por enquanto consideramos apenas uma coleçao
                output_agg1 = select_join.result_consulta(selectStatement, "left");
                output_agg2 = select_join.result_consulta(selectStatement, "right");
            } else {
                output_agg1 = select_join.result_consulta(selectStatement, "right");
                output_agg2 = select_join.result_consulta(selectStatement, "left");
            }
        }

        /*
         *   Otimização Possivel: Tentar ja retornar o result set final (utilizat MAP)
         */
        JSONArray array_header = new JSONArray("[" + header + "]");
        List<DBObject> lista_resultados = output_agg1.toArray();
        List<DBObject> lista_resultados2 = output_agg2.toArray();
        int num_tarefas_por_thread = lista_resultados.size() / 4;
        // Objeto compartilhado entre threads
        Acumulador compartilha = new Acumulador();
        MyRunnable my1 = new MyRunnable(lista_resultados2, array_header, lista_resultados.subList(0, num_tarefas_por_thread), compartilha, first_param.getName(), second_param.getName(), "LEFT");
        MyRunnable my2 = new MyRunnable(lista_resultados2, array_header, lista_resultados.subList(num_tarefas_por_thread + 1, 2 * num_tarefas_por_thread), compartilha, first_param.getName(), second_param.getName(), "LEFT");
        MyRunnable my3 = new MyRunnable(lista_resultados2, array_header, lista_resultados.subList(2 * num_tarefas_por_thread + 1, 3 * num_tarefas_por_thread), compartilha, first_param.getName(), second_param.getName(), "LEFT");
        MyRunnable my4 = new MyRunnable(lista_resultados2, array_header, lista_resultados.subList(3 * num_tarefas_por_thread + 1, lista_resultados.size() - 1), compartilha, first_param.getName(), second_param.getName(), "LEFT");

        // Cria threads para executar os objetos Runnable
        Thread te1 = new Thread(my1);
        Thread te2 = new Thread(my2);
        Thread te3 = new Thread(my3);
        Thread te4 = new Thread(my4);

        // Inicia threads
        te1.start();
        te2.start();
        te3.start();
        te4.start();
        // Espera as threads encerrarem a sua execucao
        try {
            te1.join();
            te2.join();
            te3.join();
            te4.join();
        } catch (InterruptedException e) {
        }

        join_result += compartilha.get();
        return join_result;
    }

    public String executeLeftJoinSubDoc(SelectClause selectStatement, String header, String subdoc) {
        DBCollection collection = SelectObjectWithJoinTranslate.database.getCollection(selectStatement.getJoinList().get(0).getTableRightExpression());
        DBCursor resultado = collection.find();
        String join_result = "";
        //Otimizaçao possivel, utilizar objeto de projeçao, pra recuperar apenas valores que interessam
        JSONArray array_header = new JSONArray("[" + header + "]");
        int cont = 0;
        List<DBObject> lista_resultados = resultado.toArray();
        int num_tarefas_por_thread = lista_resultados.size() / 4;
        // Objeto compartilhado entre threads
        Acumulador compartilha = new Acumulador();
        MyRunnable_SubDoc my1 = new MyRunnable_SubDoc(lista_resultados, header, 0, num_tarefas_por_thread, compartilha, subdoc);
        MyRunnable_SubDoc my2 = new MyRunnable_SubDoc(lista_resultados, header, num_tarefas_por_thread + 1, 2 * num_tarefas_por_thread, compartilha, subdoc);
        MyRunnable_SubDoc my3 = new MyRunnable_SubDoc(lista_resultados, header, 2 * num_tarefas_por_thread + 1, 3 * num_tarefas_por_thread, compartilha, subdoc);
        MyRunnable_SubDoc my4 = new MyRunnable_SubDoc(lista_resultados, header, 3 * num_tarefas_por_thread + 1, lista_resultados.size() - 1, compartilha, subdoc);

        // Cria threads para executar os objetos Runnable
        Thread te1 = new Thread(my1);
        Thread te2 = new Thread(my2);
        Thread te3 = new Thread(my3);
        Thread te4 = new Thread(my4);

        // Inicia threads
        te1.start();
        te2.start();
        te3.start();
        te4.start();
        // Espera as threads encerrarem a sua execucao
        try {
            te1.join();
            te2.join();
            te3.join();
            te4.join();
        } catch (InterruptedException e) {
        }

        join_result += compartilha.get();
        return join_result;
    }
}
