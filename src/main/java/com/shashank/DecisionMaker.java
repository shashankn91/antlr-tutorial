package com.shashank;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class DecisionMaker {

    public List<String>  getVars(String code){
        CharStream charStream = new ANTLRInputStream(code);
        DecisionLexer lexer = new DecisionLexer(charStream);
        TokenStream tokens = new CommonTokenStream(lexer);
        ((CommonTokenStream) tokens).fill();
        List<Token> tokensList  = ((CommonTokenStream) tokens).get(0,tokens.size() -1);

        List<String> varsList = tokensList.stream().filter(t->{
            if(lexer.getVocabulary().getSymbolicName(t.getType()) == null){
                return false;
            }
            return lexer.getVocabulary().getSymbolicName(t.getType()).equals("VARNAME");
        }).map(t-> t.getText().trim().substring(1)).collect(Collectors.toList());

        return varsList;
    }
    public boolean parse(String code, Map<String, VariableData> context) {
        CharStream charStream = new ANTLRInputStream(code);
        DecisionLexer lexer = new DecisionLexer(charStream);
        TokenStream tokens = new CommonTokenStream(lexer);
        DecisionParser parser = new DecisionParser(tokens);
        ExpressionVisitor expressionVisitor = new ExpressionVisitor(context);
        return expressionVisitor.visit(parser.expression());

    }


    private class ExpressionVisitor extends DecisionBaseVisitor<Boolean> {
        private final Map<String, VariableData> externalContext;


        private ExpressionVisitor(Map<String, VariableData> context) {
            this.externalContext = context;
        }

        @Override
        public Boolean visitParenthesisExp(@NotNull DecisionParser.ParenthesisExpContext ctx) {
            return visit(ctx.expression());
        }

        @Override
        public Boolean visitAndOrExp(@NotNull DecisionParser.AndOrExpContext ctx) {
            Boolean left = visit(ctx.expression(0));
            Boolean right = visit(ctx.expression(1));
            Boolean result = null;
            if (ctx.AND() != null) {
                result = left && right;
            }
            if (ctx.OR() != null) {
                result = left || right;
            }
            return result;
        }


        @Override
        public Boolean visitNumberRangeExp(@NotNull DecisionParser.NumberRangeExpContext ctx){

            Boolean result = null;

            final Double number  = Double.parseDouble(ctx.NUMBER().getSymbol().getText());
            RangeVisitor rangeVisitor = new RangeVisitor();
            RangeData rangeData= rangeVisitor.visit(ctx.range());
            if(rangeData.getStartRange() == null && rangeData.getEndRange() != null){
                Double maxLimit = rangeData.getEndRange();
                return (maxLimit >= number) ;
            }

            if(rangeData.getEndRange() == null && rangeData.getStartRange() != null){
                Double minLimit = rangeData.getStartRange();
                return (minLimit <= number) ;
            }

            if(rangeData.getStartRange() != null && rangeData.getEndRange() != null){
                Double maxLimit = rangeData.getEndRange();
                Double minLimit = rangeData.getStartRange();

                return (maxLimit >= number) && (minLimit <= number) ;
            }

            return result;
        }

        @Override
        public Boolean visitNumberRangeListExp(@NotNull DecisionParser.NumberRangeListExpContext ctx){
            final Double number  = Double.parseDouble(ctx.NUMBER().getSymbol().getText());
            NumberListArrVisitor numberListArrVisitor = new NumberListArrVisitor();
            List<Double> numbers = numberListArrVisitor.visit(ctx.rangeNumberlists());
            return numbers.stream().filter(it -> it.equals(number)).count() > 0;

        }

        @Override
        public Boolean visitVarNameRangeExp(@NotNull DecisionParser.VarNameRangeExpContext ctx){

            Boolean result = null;
            String varName = ctx.VARNAME().getSymbol().getText();
            varName = varName.startsWith("$") ? varName.substring(1) : varName;

            VariableData variableData = externalContext.get(varName);
            if(variableData.getVariableType() != VariableData.VariableType.DOUBLE){
                throw new RuntimeException("Wrong Variable Type");
            }
            Double number  = variableData.getNumber();
            RangeVisitor rangeVisitor = new RangeVisitor();
            RangeData rangeData= rangeVisitor.visit(ctx.range());
            if(rangeData.getStartRange() == null && rangeData.getEndRange() != null){
                Double maxLimit = rangeData.getEndRange();
                return (maxLimit >= number) ;
            }

            if(rangeData.getEndRange() == null && rangeData.getStartRange() != null){
                Double minLimit = rangeData.getStartRange();
                return (minLimit <= number) ;
            }

            if(rangeData.getStartRange() != null && rangeData.getEndRange() != null){
                Double maxLimit = rangeData.getEndRange();
                Double minLimit = rangeData.getStartRange();

                return (maxLimit >= number) && (minLimit <= number) ;
            }
            return result;
        }


        @Override
        public Boolean visitVarNameRangeListExp(@NotNull DecisionParser.VarNameRangeListExpContext ctx){
            Boolean result = null;
            String varName = ctx.VARNAME().getSymbol().getText();
            varName = varName.startsWith("$") ? varName.substring(1) : varName;

            VariableData variableData = externalContext.get(varName);
            if(variableData.getVariableType() != VariableData.VariableType.DOUBLE){
                throw new RuntimeException("Wrong Variable Type");
            }
            Double number  = variableData.getNumber();

            NumberListArrVisitor numberListArrVisitor = new NumberListArrVisitor();
            List<Double> numbers = numberListArrVisitor.visit(ctx.rangeNumberlists());
            return numbers.stream().filter(it -> it.equals(number)).count() > 0;
        }

        @Override
        public Boolean visitNumberMatchExp(@NotNull DecisionParser.NumberMatchExpContext ctx){
            NumberListArrVisitor numberListArrVisitor = new NumberListArrVisitor();
            List<Double> leftNumbers = numberListArrVisitor.visit(ctx.rangeNumberlists(0));
            List<Double> rightNumbers = numberListArrVisitor.visit(ctx.rangeNumberlists(1));

            Set<Double> leftNumbersSet = new HashSet<>(leftNumbers);
            Set<Double> rightNumbersSet = new HashSet<>(rightNumbers);
            if(leftNumbersSet.size() != rightNumbersSet.size()){
                return false;
            }
            return leftNumbersSet.stream().allMatch(element -> rightNumbersSet.contains(element));
        }

        @Override
        public Boolean visitVarNameMatchNumberExp(@NotNull DecisionParser.VarNameMatchNumberExpContext ctx){

            String varName = ctx.VARNAME().getSymbol().getText();
            varName = varName.startsWith("$") ? varName.substring(1) : varName;
            VariableData variableData = externalContext.get(varName);
            if(variableData.getVariableType() != VariableData.VariableType.LIST_DOUBLE){
                throw new RuntimeException("Wrong Variable Type");
            }

            List<Double> leftNumbers  = variableData.getNumbersList();

            NumberListArrVisitor numberListArrVisitor = new NumberListArrVisitor();
            List<Double> rightNumbers = numberListArrVisitor.visit(ctx.rangeNumberlists());

            Set<Double> leftNumbersSet = new HashSet<>(leftNumbers);
            Set<Double> rightNumbersSet = new HashSet<>(rightNumbers);
            if(leftNumbersSet.size() != rightNumbersSet.size()){
                return false;
            }
            return leftNumbersSet.stream().allMatch(element -> rightNumbersSet.contains(element));

        }

        @Override
        public Boolean visitVarNameMatchStringExp(@NotNull DecisionParser.VarNameMatchStringExpContext ctx){

            String varName = ctx.VARNAME().getSymbol().getText();
            varName = varName.startsWith("$") ? varName.substring(1) : varName;
            VariableData variableData = externalContext.get(varName);
            if(variableData.getVariableType() != VariableData.VariableType.LIST_STRING){
                throw new RuntimeException("Wrong Variable Type");
            }

            List<String> leftStrings  = variableData.getStringsList();

            StringListArrVisitor stringListArrVisitor = new StringListArrVisitor();
            List<String> rightStrings = stringListArrVisitor.visit(ctx.rangeStringlists());

            Set<String> leftStringsSet = new HashSet<>(leftStrings);
            Set<String> rightStringsSet = new HashSet<>(rightStrings);
            if(leftStringsSet.size() != rightStringsSet.size()){
                return false;
            }
            return rightStringsSet.stream().allMatch(element -> leftStringsSet.contains(element.startsWith("#") ? element.substring(1) : element));

        }

        @Override
        public Boolean visitVarNameBelongsStringExp(@NotNull DecisionParser.VarNameBelongsStringExpContext ctx){

            String varName = ctx.VARNAME().getSymbol().getText();
            varName = varName.startsWith("$") ? varName.substring(1) : varName;
            VariableData variableData = externalContext.get(varName);
            if(variableData.getVariableType() != VariableData.VariableType.LIST_STRING){
                throw new RuntimeException("Wrong Variable Type");
            }

            List<String> leftStrings  = variableData.getStringsList();

            StringListArrVisitor stringListArrVisitor = new StringListArrVisitor();
            List<String> rightStrings = stringListArrVisitor.visit(ctx.rangeStringlists());

            Set<String> leftStringsSet = new HashSet<>(leftStrings);
            Set<String> rightStringsSet = new HashSet<>(rightStrings);
            return rightStringsSet.stream().anyMatch(element -> leftStringsSet.contains(element.startsWith("#") ? element.substring(1) : element));

        }


        @Override
        public Boolean visitStringMatchExp(@NotNull DecisionParser.StringMatchExpContext ctx){
            StringListArrVisitor stringListArrVisitor = new StringListArrVisitor();
            List<String> leftStrings = stringListArrVisitor.visit(ctx.rangeStringlists(0));
            List<String> rightStrings = stringListArrVisitor.visit(ctx.rangeStringlists(1));

            Set<String> leftStringsSet = new HashSet<>(leftStrings);
            Set<String> rightStringsSet = new HashSet<>(rightStrings);
            if(leftStringsSet.size() != rightStringsSet.size()){
                return false;
            }
            return leftStrings.stream().allMatch(element -> rightStrings.contains(element));

        }

    }


    private class RangeVisitor extends DecisionBaseVisitor<RangeData> {

        @Override
        public RangeData visitRangeNumberToNumber(@NotNull DecisionParser.RangeNumberToNumberContext ctx){
            Double start = null,end = null;
            start = Double.parseDouble(ctx.NUMBER(0).getSymbol().getText());
            end = Double.parseDouble(ctx.NUMBER(1).getSymbol().getText());
            RangeData result = new RangeData(start,end);
            return result;
        }

        @Override
        public RangeData visitRangeWildcardToNumber(@NotNull DecisionParser.RangeWildcardToNumberContext ctx){

            Double start = null,end = null;
            if(ctx.WILDCARD() != null && ctx.NUMBER() != null){
                end = Double.parseDouble(ctx.NUMBER().getSymbol().getText());
            }
            RangeData result = new RangeData(start,end);
            return result;
        }

        @Override
        public RangeData visitRangeNumberToWildCard(@NotNull DecisionParser.RangeNumberToWildCardContext ctx){

            Double start = null,end = null;

            if(ctx.WILDCARD() != null && ctx.NUMBER() != null){
                start = Double.parseDouble(ctx.NUMBER().getSymbol().getText());
            }
            RangeData result = new RangeData(start,end);
            return result;
        }

    }



    private class NumberListArrVisitor extends DecisionBaseVisitor<List<Double>> {
        @Override
        public List<Double> visitNumbersListArr(@NotNull DecisionParser.NumbersListArrContext ctx){

            List<Double> numbersList = new ArrayList<>();
            numbersList.add(Double.parseDouble(ctx.NUMBER().getSymbol().getText()));
            if(ctx.numbers() != null ){
                NumbersListVisitor numbersListVisitor = new NumbersListVisitor();
                numbersList.addAll(numbersListVisitor.visit(ctx.numbers()));
            }
            return numbersList;
        }
    }

    private class StringListArrVisitor extends DecisionBaseVisitor<List<String>> {
        @Override
        public List<String> visitStringListArr(@NotNull DecisionParser.StringListArrContext ctx){

            List<String> stringsList = new ArrayList<>();
            stringsList.add(ctx.STRING().getSymbol().getText());
            if(ctx.strings() != null ){
                StringListVisitor stringListVisitor = new StringListVisitor();
                stringsList.addAll(stringListVisitor.visit(ctx.strings()));
            }
            return stringsList;
        }
    }


    private class NumbersListVisitor extends DecisionBaseVisitor<List<Double>> {

        private List<Double> numbersList = new ArrayList<>();
        @Override
        public List<Double> visitNumbersList(@NotNull DecisionParser.NumbersListContext ctx){
            if (ctx.NUMBER() != null) {
                numbersList.add(Double.parseDouble(ctx.NUMBER().getSymbol().getText()));
            }
            if(ctx.numbers(0) != null) {
                return visit(ctx.numbers(0));
            }
            return numbersList;
        }
    }

    private class StringListVisitor extends DecisionBaseVisitor<List<String>> {

        private List<String> stringList = new ArrayList<>();
        @Override
        public List<String> visitStringsList(DecisionParser.StringsListContext ctx){

            if (ctx.STRING() != null) {
                stringList.add(ctx.STRING().getSymbol().getText());
            }
            if(ctx.strings(0) != null) {
                return visit(ctx.strings(0));
            }
            return stringList;
        }
    }
}
