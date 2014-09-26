package queryInterceptor;

import AggregationFramework.AggregationQuery;
import SimpleSelect.SimpleSelect;
import com.mongodb.AggregationOutput;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import consultaMongo.MongoConnection;
import deleteClause.DeleteClause;
import insertClause.InsertClause;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.ejb.Stateless;
import net.sf.jsqlparser.JSQLParserException;
import objectToMongoTranslate.DeleteObjectTranslate;
import objectToMongoTranslate.InsertObjectTranslate;
import objectToMongoTranslate.SelectAggObjectTranslate;
import objectToMongoTranslate.SelectObjectWithJoinTranslate;
import objectToMongoTranslate.SelectSimpleObjectTranslate;
import objectToMongoTranslate.UpdateObjectTranslate;
import queryToObjectTranslate.DeleteToObject;
import queryToObjectTranslate.InsertToObject;
import queryToObjectTranslate.SelectToObject;
import queryToObjectTranslate.UpdateToObject;
import selectClause.SelectClause;
import updateClause.UpdateClause;

/**
 *
 * @author fernando
 */
@WebService(serviceName = "interceptaQuery")
@Stateless()
public class QueryInterceptor {

    public static DB database = MongoConnection.getInstance().getDB();

    /**
     * Web service operation
     *
     * @param query
     * @param tipoQuery
     * @return
     */
    @WebMethod(operationName = "intercepta")
    public ArrayList<String> intercepta(@WebParam(name = "query") String query, @WebParam(name = "tipoQuery") String tipoQuery) {
        ArrayList<String> retorno = new ArrayList<>();

        try {

            long tempoInicialExecuteSelect, tempoFinalExecuteSelect, tempoTotalExecuteSelect = 0,
                    tempoInicialExecuteHeader, tempoFinalExecuteHeader, tempoTotalExecuteHeader = 0,
                    tempoInicialExecuteResultSet, tempoFinalExecuteResultSet, tempoTotalExecuteResultSet = 0;
            long tempoInicialDeclaracoes, tempoFinalDeclaracoes;
            long tempoInicial = System.currentTimeMillis();
            switch (tipoQuery) {
                case "SELECT":
                    /*Cria objeto a partir da consulta select*/
                    SelectClause selectTranslate = new SelectToObject(query).selectClause;
                    long tempoFinalQueryToObject = System.currentTimeMillis();
                    long tempoTotalQueryToObject = tempoFinalQueryToObject - tempoInicial;
                    DBCursor resultSimpleQuery = null;
                    String result_set = null,
                     header;
                    if (selectTranslate.isHasJoin()) {
                        SelectObjectWithJoinTranslate selectMongo = new SelectObjectWithJoinTranslate();
                        tempoInicialExecuteHeader = System.currentTimeMillis();
                        header = AggregationQuery.retornaCabecalho(selectTranslate);
                        tempoFinalExecuteHeader = System.currentTimeMillis();
                        tempoTotalExecuteHeader = tempoFinalExecuteHeader - tempoInicialExecuteHeader;

                        tempoInicialExecuteResultSet = System.currentTimeMillis();
                        result_set = selectMongo.executeMongoSelect(selectTranslate, header);
                        tempoFinalExecuteResultSet = System.currentTimeMillis();
                        tempoTotalExecuteResultSet = tempoFinalExecuteResultSet - tempoInicialExecuteResultSet;
                    } else {
                        //Decide entre usar ou não o Aggregation Framework
                        if (selectTranslate.getFuncoes().isEmpty()) {

                            SelectSimpleObjectTranslate selectMongo = new SelectSimpleObjectTranslate();
                            tempoInicialExecuteSelect = System.currentTimeMillis();

                            resultSimpleQuery = selectMongo.executeMongoSelect(selectTranslate);
                            tempoFinalExecuteSelect = System.currentTimeMillis();
                            tempoTotalExecuteSelect = tempoFinalExecuteSelect - tempoInicialExecuteSelect;

                            tempoInicialExecuteHeader = System.currentTimeMillis();
                            header = SimpleSelect.retornaCabecalho(selectTranslate);
                            tempoFinalExecuteHeader = System.currentTimeMillis();
                            tempoTotalExecuteHeader = tempoFinalExecuteHeader - tempoInicialExecuteHeader;
                            tempoInicialExecuteResultSet = System.currentTimeMillis();
                            result_set = SimpleSelect.retornaResultSet(resultSimpleQuery, header);
                            
                            tempoFinalExecuteResultSet = System.currentTimeMillis();
                            tempoTotalExecuteResultSet = tempoFinalExecuteResultSet - tempoInicialExecuteResultSet;

                        } else {
                            AggregationOutput resultAggregation;
                            SelectAggObjectTranslate selectMongo = new SelectAggObjectTranslate();
                            resultAggregation = selectMongo.executeMongoSelect(selectTranslate);
                            header = AggregationQuery.retornaCabecalho(selectTranslate);
                            result_set = AggregationQuery.retornaResultSet(resultAggregation, header);
                        }
                    }

                    retorno.add("header = {" + header + "}");
                    retorno.add("result_set = {"+result_set+"}");
                    //System.out.println(retorno.get(0));
                    //System.out.println(result_set);
                    
                    //retorno.add(header);
                    //retorno.add(resultado_select.toString().substring(2,resultado_select.toString().length()-2));
                    long tempoFinal = System.currentTimeMillis();
                    //System.out.println("Tempo Query to Object: " + tempoTotalQueryToObject);
                    //System.out.println("Tempo Execute Select: " + tempoTotalExecuteSelect);
                    //System.out.println("Tempo Execute Header: " + tempoTotalExecuteHeader);
                    //System.out.println("tempo Execute Result Set: " + tempoTotalExecuteResultSet);
                    //System.out.println("Tempo Total de execução: " + (tempoFinal - tempoInicial) + " ms");

                    //System.out.println(retorno.get(0));
                    //System.out.println(retorno.get(1));
                    break;
                case "INSERT":
                    /*Cria objeto a partir da consulta insert*/
                    InsertClause insertTranslate = new InsertToObject(query).insertClause;

                    InsertObjectTranslate insertMongo = new InsertObjectTranslate();
                    insertMongo.executeMongoInsert(insertTranslate);
                    break;

                case "UPDATE":
                    /*Cria objeto a partir da consulta update*/
                    UpdateClause updateTranslate = new UpdateToObject(query).updateClause;
                    UpdateObjectTranslate updateMongo = new UpdateObjectTranslate();
                    updateMongo.executeMongoUpdate(updateTranslate);
                    break;

                case "DELETE":
                    
                    /*Cria objeto a partir da consulta delete*/
                    DeleteClause deleteTranslate = new DeleteToObject(query).deleteClause;

                    DeleteObjectTranslate deleteMongo = new DeleteObjectTranslate();
                    deleteMongo.executeMongoDelete(deleteTranslate);
                    break;
            }
        } catch (JSQLParserException ex) {
            Logger.getLogger(QueryInterceptor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retorno;
    }
}
