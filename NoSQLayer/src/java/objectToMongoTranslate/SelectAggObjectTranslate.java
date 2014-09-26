package objectToMongoTranslate;

import AggregationFramework.AggregationQuery;
import com.mongodb.AggregationOutput;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import consultaMongo.MongoConnection;
import java.util.ArrayList;
import net.sf.jsqlparser.JSQLParserException;
import selectClause.SelectClause;

public class SelectAggObjectTranslate {

    public DB database = MongoConnection.getInstance().getDB();

    public AggregationOutput executeMongoSelect(SelectClause selectStatement) throws JSQLParserException {

        //Considerando apenas uma coleçao
        DBCollection collection = database.getCollection(selectStatement.getTablesQueried().get(0).getName());
        DBCursor dbCursor;
        DBObject criteria = (DBObject) JSON.parse(returnCriteria(selectStatement));
        DBObject order = (DBObject) JSON.parse(retornaOrder(selectStatement));
        DBObject groupBy = null;
        Long limit = null, offset = null;

        AggregationQuery aggregation = new AggregationQuery();

        if (!selectStatement.getFuncoes().isEmpty()) {
            groupBy = aggregation.returnAggOperators(selectStatement);
        }
        DBObject projection = null, projection_ppl = null;

        if (this.returnProjection(selectStatement) != null && !returnProjection(selectStatement).equals("*")) {
            projection = (DBObject) JSON.parse(returnProjection(selectStatement));
        }

        if (selectStatement.getLimit() != null) {
            limit = selectStatement.getLimit().getRowCount();
            offset = selectStatement.getLimit().getOffset();
        }
        long tempoInicial = System.currentTimeMillis();
        ArrayList<DBObject> vetor_valores = aggregation.retornaVetorValores(criteria, projection, groupBy, order, limit, offset, selectStatement);
        AggregationOutput output_agg = aggregation.returnAggResult(vetor_valores, collection);
        long tempoFinal = System.currentTimeMillis();

        return output_agg;
    }

    public String returnProjection(SelectClause select) {
        if (select.getTablesQueried().get(0).isIsAllColumns()) {
            return "*";
        } else {
            String queryMongo = "{";
            //Verifica se o item buscado é *
            if (select.getTablesQueried().get(0).getParams_projecao().size() > 0) {
                if (!(select.getTablesQueried().get(0).isIsAllColumns())) {
                    for (int i = 0; i < select.getTablesQueried().get(0).getParams_projecao().size(); i++) {
                        if (select.getTablesQueried().get(0).getParams_projecao().get(i).getAlias() != null) {
                            String alias = select.getTablesQueried().get(0).getParams_projecao().get(i).getAlias();
                            queryMongo += " '" + alias + "': '$" + select.getTablesQueried().get(0).getParams_projecao().get(i).getName() + "' ,";
                        } else {
                            queryMongo += select.getTablesQueried().get(0).getParams_projecao().get(i).getName() + " : 1,";
                        }
                    }
                }
                queryMongo += " _id: 0}";
            } else {
                return null;
            }

            return queryMongo;
        }
    }

    public String returnCriteria(SelectClause select) {
        return select.getCriteriaIdentifier().whereQuery.toString();
    }

    public String retornaOrder(SelectClause select) {

        String queryMongo = null;

        if (select.getOrdenacao().size() > 0) {
            queryMongo = "{";

            for (int i = 0; i < select.getOrdenacao().size(); i++) {
                queryMongo += select.getOrdenacao().get(i).getAtributo() + ": " + select.getOrdenacao().get(i).getOrdem() + ",";
            }
            queryMongo += "}";
        }

        return queryMongo;
    }
}
