package criteria;

import SimpleSelect.SimpleSelect;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jsonJava.JSONArray;
import jsonJava.JSONObject;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.update.Update;
import objectToMongoTranslate.SelectAggObjectTranslate;
import objectToMongoTranslate.SelectSimpleObjectTranslate;
import queryToObjectTranslate.SelectToObject;
import selectClause.SelectClause;

public class CriteriaIdentifier {

    public List<Criteria> criterios = new ArrayList<>();
    public List<String> conectivoLogico = new ArrayList<>();
    public List inList = new ArrayList<>();
    private String inColumn;
    public BasicDBObject whereQuery = new BasicDBObject();
    public BasicDBObject whereEsquerdaTmp = new BasicDBObject();

    public void redirectQuery(Object statement) throws JSQLParserException {
        Expression expressionWhere = null;

        if (statement instanceof Select) {
            PlainSelect select = (PlainSelect) ((Select) statement).getSelectBody();
            expressionWhere = select.getWhere();
        } else if (statement instanceof Delete) {
            Delete delete = (Delete) statement;
            expressionWhere = delete.getWhere();
        } else if (statement instanceof Update) {
            Update update = (Update) statement;
            expressionWhere = update.getWhere();
        } else if (statement instanceof Insert) {
            //TODO
        }

        if (expressionWhere instanceof AndExpression) {
            AndExpression e = (AndExpression) expressionWhere;
            this.fragmentaCriteria(e.getLeftExpression(), e.getRightExpression(), "$and");
        } else if (expressionWhere instanceof OrExpression) {
            OrExpression e = (OrExpression) expressionWhere;
            this.fragmentaCriteria(e.getLeftExpression(), e.getRightExpression(), "$or");
        } else if (expressionWhere != null) {
            this.criterioSimples(expressionWhere);
        }
    }
    /*
     * Função recursiva que busca todas os criterios em uma clausula WHERE composta
     */

    public void fragmentaCriteria(Expression leftExpression, Expression rightExpression, String conectivo) {

        if (rightExpression instanceof Parenthesis) {
            rightExpression = ((Parenthesis) rightExpression).getExpression();
        }

        if (rightExpression instanceof AndExpression) {
            AndExpression e = (AndExpression) rightExpression;
            fragmentaCriteria(e.getLeftExpression(), e.getRightExpression(), "$and");
        } else if (rightExpression instanceof OrExpression) {
            OrExpression e = (OrExpression) rightExpression;
            fragmentaCriteria(e.getLeftExpression(), e.getRightExpression(), "$or");
        }

        if (leftExpression instanceof Parenthesis) {
            leftExpression = ((Parenthesis) leftExpression).getExpression();
        }

        if (leftExpression instanceof AndExpression) {
            AndExpression e = (AndExpression) leftExpression;
            fragmentaCriteria(e.getLeftExpression(), e.getRightExpression(), "$and");

        } else if (leftExpression instanceof OrExpression) {
            OrExpression e = (OrExpression) leftExpression;
            fragmentaCriteria(e.getLeftExpression(), e.getRightExpression(), "$or");
        }

        //Fim da recursão
        if (leftExpression instanceof EqualsTo) {
            EqualsTo equalsTo = (EqualsTo) leftExpression;
            //Ja salva as operações conforme sao utilizadas no documento
            this.addCriteria(equalsTo.getLeftExpression().toString(), ":", equalsTo.getRightExpression().toString());

        } else if (leftExpression instanceof GreaterThan) {
            GreaterThan greatherThan = (GreaterThan) leftExpression;
            this.addCriteria(greatherThan.getLeftExpression().toString(), "$gt", greatherThan.getRightExpression().toString());

        } else if (leftExpression instanceof GreaterThanEquals) {
            GreaterThanEquals greatherThanEquals = (GreaterThanEquals) leftExpression;
            this.addCriteria(greatherThanEquals.getLeftExpression().toString(), "$gte", greatherThanEquals.getRightExpression().toString());

        } else if (leftExpression instanceof MinorThan) {
            MinorThan minorThan = (MinorThan) leftExpression;
            this.addCriteria(minorThan.getLeftExpression().toString(), "$lt", minorThan.getRightExpression().toString());

        } else if (leftExpression instanceof MinorThanEquals) {
            MinorThanEquals minorThanEquals = (MinorThanEquals) leftExpression;
            this.addCriteria(minorThanEquals.getLeftExpression().toString(), "$lte", minorThanEquals.getRightExpression().toString());

        } else if (leftExpression instanceof NotEqualsTo) {
            NotEqualsTo notEqualsTo = (NotEqualsTo) leftExpression;
            this.addCriteria(notEqualsTo.getLeftExpression().toString(), "$ne", notEqualsTo.getRightExpression().toString());
        }

        //Verifica o lado direito
        if (rightExpression instanceof EqualsTo) {

            EqualsTo equalsTo = (EqualsTo) rightExpression;
            this.addCriteria(equalsTo.getLeftExpression().toString(), ":", equalsTo.getRightExpression().toString());

        } else if (rightExpression instanceof GreaterThan) {
            GreaterThan greatherThan = (GreaterThan) rightExpression;
            this.addCriteria(greatherThan.getLeftExpression().toString(), "$gt", greatherThan.getRightExpression().toString());

        } else if (rightExpression instanceof GreaterThanEquals) {
            GreaterThanEquals greatherThanEquals = (GreaterThanEquals) rightExpression;
            this.addCriteria(greatherThanEquals.getLeftExpression().toString(), "$gte", greatherThanEquals.getRightExpression().toString());

        } else if (rightExpression instanceof MinorThan) {
            MinorThan minorThan = (MinorThan) rightExpression;
            this.addCriteria(minorThan.getLeftExpression().toString(), "$lt", minorThan.getRightExpression().toString());

        } else if (rightExpression instanceof MinorThanEquals) {
            MinorThanEquals minorThanEquals = (MinorThanEquals) rightExpression;
            this.addCriteria(minorThanEquals.getLeftExpression().toString(), "$lte", minorThanEquals.getRightExpression().toString());

        } else if (rightExpression instanceof NotEqualsTo) {
            NotEqualsTo notEqualsTo = (NotEqualsTo) rightExpression;
            this.addCriteria(notEqualsTo.getLeftExpression().toString(), "$ne", notEqualsTo.getRightExpression().toString());
        }

        if (conectivo.equals("$and") || conectivo.equals("$or")) {
            List<BasicDBObject> obj = new ArrayList<>();
            for (int i = 0; i < criterios.size(); i++) {

                if (criterios.size() > 0 && criterios.get(i).getOperador().equals(":")) {
                    //Se for Integer
                    if (criterios.get(i).getRightExpression().matches("^[0-9]*$")) {
                        obj.add(new BasicDBObject(criterios.get(i).getLeftExpression(), Long.parseLong(criterios.get(i).getRightExpression().trim())));
                    } else {
                        //Se for String remove as aspas simples
                        String valor_direita = criterios.get(i).getRightExpression().substring(1, criterios.get(i).getRightExpression().length() - 1);
                        obj.add(new BasicDBObject(criterios.get(i).getLeftExpression(), valor_direita));
                    }
                } else {
                    //Se for Integer
                    if (criterios.get(i).getRightExpression().matches("^[0-9]*$")) {
                        BasicDBObject operador_comp = new BasicDBObject(criterios.get(i).getOperador(), Long.parseLong(criterios.get(i).getRightExpression().trim()));
                        obj.add(new BasicDBObject(criterios.get(i).getLeftExpression(), operador_comp));

                    } else {
                        //Se for String remove as aspas simples
                        String valor_direita = criterios.get(i).getRightExpression().substring(1, criterios.get(i).getRightExpression().length() - 1);
                        BasicDBObject operador_comp = new BasicDBObject(criterios.get(i).getOperador(), valor_direita);
                        obj.add(new BasicDBObject(criterios.get(i).getLeftExpression(), operador_comp));
                    }
                }
            }
            if (whereQuery.isEmpty()) {
                whereQuery.put(conectivo, obj);
            } else {
                ArrayList conectivoLista = new ArrayList();

                if (!obj.isEmpty()) {
                    //Para clausulas tipo column1 = value 1 OR (column2 = value 2 AND column3 = value3)
                    if (obj.size() == 1) {
                        conectivoLista.add(obj.get(0));
                        conectivoLista.add(whereQuery);
                        whereQuery.remove(0);
                        whereQuery = new BasicDBObject(conectivo, conectivoLista);

                        //Para clausulas tipo (column1 = value 1 AND column4 = value4) OR (column2 = value 2 AND column3 = value3)
                    } else {
                        ArrayList novaLista = new ArrayList();
                        for (int i = 0; i < obj.size(); i++) {
                            novaLista.add(obj.get(i));
                        }
                        whereEsquerdaTmp = new BasicDBObject(conectivo, novaLista);
                    }
                } else {
                    conectivoLista.add(whereEsquerdaTmp);
                    whereEsquerdaTmp.remove(0);
                    conectivoLista.add(whereQuery);
                    whereQuery.remove(0);
                    whereQuery = new BasicDBObject(conectivo, conectivoLista);
                }

            }
        }
        criterios.clear();
        this.addConectivoLogico(conectivo);
    }

    /*
     * Função que analisa criterios simples. Ex WHERE id='1'
     */
    public void criterioSimples(Expression expression) throws JSQLParserException {

        ArrayList<String> select_result;
        //Fim da recursão
        if (expression instanceof EqualsTo) {
            EqualsTo equalsTo = (EqualsTo) expression;
            if (equalsTo.getLeftExpression() instanceof SubSelect) {
                select_result = retornaValorSubSelect(((SubSelect) equalsTo.getLeftExpression()).getSelectBody().toString());
                //Para os subselect poder retornar mais de um valor, utiliza-se o in
                setaValoresInSubselect(select_result);
                this.setInColumn(equalsTo.getRightExpression().toString());
            } else if (equalsTo.getRightExpression() instanceof SubSelect) {
                select_result = retornaValorSubSelect(((SubSelect) equalsTo.getRightExpression()).getSelectBody().toString());
                //Para os subselect poder retornar mais de um valor, utiliza-se o in
                setaValoresInSubselect(select_result);
                this.setInColumn(equalsTo.getLeftExpression().toString());
            } else {
                this.addCriteria(equalsTo.getLeftExpression().toString(), ":", equalsTo.getRightExpression().toString());
            }

        } else if (expression instanceof GreaterThan) {
            GreaterThan greatherThan = (GreaterThan) expression;
            if (greatherThan.getLeftExpression() instanceof SubSelect) {
                select_result = retornaValorSubSelect(((SubSelect) greatherThan.getLeftExpression()).getSelectBody().toString());
                this.addCriteria(select_result.get(0), "$gt", greatherThan.getRightExpression().toString());
            } else if (greatherThan.getRightExpression() instanceof SubSelect) {
                select_result = retornaValorSubSelect(((SubSelect) greatherThan.getRightExpression()).getSelectBody().toString());
                this.addCriteria(greatherThan.getLeftExpression().toString(), "$gt", select_result.get(0));
            } else {
                this.addCriteria(greatherThan.getLeftExpression().toString(), "$gt", greatherThan.getRightExpression().toString());
            }
        } else if (expression instanceof GreaterThanEquals) {
            GreaterThanEquals greatherThanEquals = (GreaterThanEquals) expression;
            if (greatherThanEquals.getLeftExpression() instanceof SubSelect) {
                select_result = retornaValorSubSelect(((SubSelect) greatherThanEquals.getLeftExpression()).getSelectBody().toString());
                this.addCriteria(select_result.get(0), "$gte", greatherThanEquals.getRightExpression().toString());
            } else if (greatherThanEquals.getRightExpression() instanceof SubSelect) {
                select_result = retornaValorSubSelect(((SubSelect) greatherThanEquals.getRightExpression()).getSelectBody().toString());
                this.addCriteria(greatherThanEquals.getLeftExpression().toString(), "$gte", select_result.get(0));
            } else {
                this.addCriteria(greatherThanEquals.getLeftExpression().toString(), "$gte", greatherThanEquals.getRightExpression().toString());
            }

        } else if (expression instanceof MinorThan) {
            MinorThan minorThan = (MinorThan) expression;
            if (minorThan.getLeftExpression() instanceof SubSelect) {
                select_result = retornaValorSubSelect(((SubSelect) minorThan.getLeftExpression()).getSelectBody().toString());
                this.addCriteria(select_result.get(0), "$lt", minorThan.getRightExpression().toString());
            } else if (minorThan.getRightExpression() instanceof SubSelect) {
                select_result = retornaValorSubSelect(((SubSelect) minorThan.getRightExpression()).getSelectBody().toString());
                this.addCriteria(minorThan.getLeftExpression().toString(), "$lt", select_result.get(0));
            } else {
                this.addCriteria(minorThan.getLeftExpression().toString(), "$lt", minorThan.getRightExpression().toString());
            }

        } else if (expression instanceof MinorThanEquals) {
            MinorThanEquals minorThanEquals = (MinorThanEquals) expression;
            if (minorThanEquals.getLeftExpression() instanceof SubSelect) {
                select_result = retornaValorSubSelect(((SubSelect) minorThanEquals.getLeftExpression()).getSelectBody().toString());
                this.addCriteria(select_result.get(0), "$lte", minorThanEquals.getRightExpression().toString());
            } else if (minorThanEquals.getRightExpression() instanceof SubSelect) {
                select_result = retornaValorSubSelect(((SubSelect) minorThanEquals.getRightExpression()).getSelectBody().toString());
                this.addCriteria(minorThanEquals.getLeftExpression().toString(), "$lte", select_result.get(0));
            } else {
                this.addCriteria(minorThanEquals.getLeftExpression().toString(), "$lte", minorThanEquals.getRightExpression().toString());
            }

        } else if (expression instanceof NotEqualsTo) {
            NotEqualsTo notEqualsTo = (NotEqualsTo) expression;
            if (notEqualsTo.getLeftExpression() instanceof SubSelect) {
                select_result = retornaValorSubSelect(((SubSelect) notEqualsTo.getLeftExpression()).getSelectBody().toString());
                this.addCriteria(select_result.get(0), "$ne", notEqualsTo.getRightExpression().toString());
            } else if (notEqualsTo.getRightExpression() instanceof SubSelect) {
                select_result = retornaValorSubSelect(((SubSelect) notEqualsTo.getRightExpression()).getSelectBody().toString());
                this.addCriteria(notEqualsTo.getLeftExpression().toString(), "$ne", select_result.get(0));
            } else {
                this.addCriteria(notEqualsTo.getLeftExpression().toString(), "$ne", notEqualsTo.getRightExpression().toString());
            }

        } else if (expression instanceof LikeExpression) {
            LikeExpression likeExpression = (LikeExpression) expression;
            if (likeExpression.getLeftExpression() instanceof SubSelect) {
                //TODO
            } else if (likeExpression.getRightExpression() instanceof SubSelect) {
                //TODO
            } else {
                this.addCriteria(likeExpression.getLeftExpression().toString(), "$regex", likeExpression.getRightExpression().toString());
            }

        } else if (expression instanceof InExpression) {
            InExpression in = (InExpression) expression;
            List itens;
            if (in.getItemsList() instanceof SubSelect) {
                itens = retornaValorSubSelect(((SubSelect) in.getItemsList()).getSelectBody().toString());
            } else {
                itens = ((ExpressionList) in.getItemsList()).getExpressions();

            }
            for (int i = 0; i < itens.size(); i++) {
                if (itens.get(i).toString().matches("^[0-9]*$")) {

                    this.addInElementLong(Long.parseLong(itens.get(i).toString()));
                } else {
                    this.addInElement(itens.get(i).toString().replace("'", ""));
                }
            }
            this.setInColumn(in.getLeftExpression().toString());
        }

        if (criterios.size() > 0 && criterios.get(0).getOperador().equals(":")) {
            //Se for Integer
            if (criterios.get(0).getRightExpression().matches("^[0-9]*$")) {
                whereQuery.put(criterios.get(0).getLeftExpression(), Long.parseLong(criterios.get(0).getRightExpression().trim()));

            } else {
                //Se for String remove as aspas simples
                String valor_direita = criterios.get(0).getRightExpression().substring(1, criterios.get(0).getRightExpression().length() - 1);
                whereQuery.put(criterios.get(0).getLeftExpression(), valor_direita);
            }
        } else if (criterios.size() > 0 && criterios.get(0).getOperador().equals("$regex")) {              //Verifica o like
            String valor_direita = criterios.get(0).getRightExpression().substring(1, criterios.get(0).getRightExpression().length() - 1).replaceAll("%", ".*");
            BasicDBObject operador_comp = new BasicDBObject(criterios.get(0).getOperador(), valor_direita);
            whereQuery.put(criterios.get(0).getLeftExpression(), operador_comp);
        } else if (criterios.size() > 0) {
            //Se for Integer
            if (criterios.get(0).getRightExpression().matches("^[0-9]*$")) {
                BasicDBObject operador_comp = new BasicDBObject(criterios.get(0).getOperador(), Integer.parseInt(criterios.get(0).getRightExpression().trim()));
                whereQuery.put(criterios.get(0).getLeftExpression(), operador_comp);

            } else {
                    //Se for String remove as aspas simples
                String valor_direita = criterios.get(0).getRightExpression().substring(1, criterios.get(0).getRightExpression().length() - 1);
                BasicDBObject operador_comp = new BasicDBObject(criterios.get(0).getOperador(), valor_direita);
                whereQuery.put(criterios.get(0).getLeftExpression(), operador_comp);

            }
        }
        //Veritica IN expression
        if (inColumn != null) {
            whereQuery.put(inColumn, new BasicDBObject("$in", inList));
        }
    }

    public void addCriteria(String leftExpression, String operador, String rightExpression) {
        Criteria criterio = new Criteria();
        criterio.setLeftExpression(leftExpression);
        criterio.setOperador(operador);
        criterio.setRightExpression(rightExpression);
        this.criterios.add(criterio);
    }

    public ArrayList<String> retornaValorSubSelect(String query) throws JSQLParserException {

        SelectClause selectTranslate = new SelectToObject(query).selectClause;
        DBCursor resultSimpleQuery;
        AggregationOutput resultAggregation;
        String result_set, header;
        ArrayList<String> retorno = new ArrayList<>();
        if (selectTranslate.getFuncoes().isEmpty()) {
            SelectSimpleObjectTranslate selectMongo = new SelectSimpleObjectTranslate();
            resultSimpleQuery = selectMongo.executeMongoSelect(selectTranslate);
            header = SimpleSelect.retornaCabecalho(selectTranslate);
            while (resultSimpleQuery.hasNext()) {
                retorno.add(resultSimpleQuery.next().get(header.replace(",", "")).toString());
            }
            return retorno;
        } else {
            SelectAggObjectTranslate selectMongo = new SelectAggObjectTranslate();
            resultAggregation = selectMongo.executeMongoSelect(selectTranslate);

            String json_str = resultAggregation.results().toString();

            int tamanho_resultado = json_str.length();

            JSONObject my_obj = new JSONObject(json_str.substring(1, tamanho_resultado - 1));

            JSONArray array = new JSONArray(json_str);
            JSONObject my_obj_result_set;
            for (int i = 0; i < array.length(); i++) {
                //instancia um novo JSONObject passando a string como entrada
                my_obj_result_set = array.getJSONObject(i);
                my_obj_result_set.remove("_id");

                //Retorna as keys de cada documento, pois nem todos tem todos os attr
                Iterator it = my_obj_result_set.keys();
                while (it.hasNext()) {
                    String next = it.next().toString();
                    if (my_obj_result_set.get(next) != null) {
                        //Se não contém letras é tratado como número

                        if (my_obj_result_set.get(next).toString().matches("^[a-zA-ZÁÂÃÀÇÉÊÍÓÔÕÚÜáâãàçéêíóôõúü]*$")) {
                            retorno.add("'" + my_obj_result_set.get(next) + "'");
                        } else if (my_obj_result_set.get(next).toString().matches("^[0-9]*$")) {
                            retorno.add("" + my_obj_result_set.getInt(next) + "");
                        } else {
                            retorno.add("" + my_obj_result_set.getString(next) + "");
                        }
                    }
                }
            }
            return retorno;
        }

    }

    public void setaValoresInSubselect(ArrayList<String> select_result) {
        for (int i = 0; i < select_result.size(); i++) {
            if (select_result.get(i).toString().matches("^[0-9]{1,5}$")) {
                this.addInElementLong(Long.parseLong(select_result.get(i).toString()));
            } else {
                this.addInElement(select_result.get(i).toString().replace("'", ""));
            }
        }
    }

    public void addConectivoLogico(String conectivo) {
        this.conectivoLogico.add(conectivo);
    }

    public void addInElement(String element) {
        this.inList.add(element);
    }

    public void addInElementLong(Long element) {
        this.inList.add(element);
    }

    public String getInColumn() {
        return inColumn;
    }

    public void setInColumn(String inColumn) {
        this.inColumn = inColumn;
    }

    public List<Criteria> getCriterios() {
        return criterios;
    }

    public void setCriterios(List<Criteria> criterios) {
        this.criterios = criterios;
    }

    public List<String> getConectivoLogico() {
        return conectivoLogico;
    }

    public void setConectivoLogico(List<String> conectivoLogico) {
        this.conectivoLogico = conectivoLogico;
    }

    public List<String> getInList() {
        return inList;
    }

    public void setInList(List<String> inList) {
        this.inList = inList;
    }

    public boolean ehInteiro(String s) {

        // cria um array de char  
        char[] c = s.toCharArray();
        boolean d = true;

        for (int i = 0; i < c.length; i++) // verifica se o char não é um dígito  
        {
            if (!Character.isDigit(c[ i])) {
                d = false;
                break;
            }
        }
        return d;
    }
}
