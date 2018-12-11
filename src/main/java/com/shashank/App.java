package com.shashank;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class App {

    public static void main(String[] args) throws Exception {


        DecisionMaker decisionMaker = new DecisionMaker();
        Map<String,VariableData> context = new HashMap<>();

        List<String> strings = new ArrayList<>();
        strings.add("sha1");

        VariableData variableData = new VariableData(VariableData.VariableType.LIST_STRING,null,strings,null,null);

        context.put("ans-_a_1",variableData);

        String condition = "(($ans % [#sha,#ra,#lol, #gg]) || ($ans-_a_1 % [#sha,#ra,#lol, #gg])) ";

        System.out.println(decisionMaker.getVars(condition));
        System.out.println("Conditional evaluates to " + decisionMaker.parse(condition , context));




    }

    public void oldCode() throws IOException {
        DecisionMaker decisionMaker = new DecisionMaker();

        Map<String,Double> context = new HashMap<>();

        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter exitVarNames to exit variables input");
        while(true){
            System.out.println("Enter the variable name or exitVarNames to exit");
            String varName = scanner.next();
            if(varName.equals("exitVarNames")){
                break;
            }
            System.out.println("Please enter var value");
            Double varVal = scanner.nextDouble();
            context.put(varName,varVal);
        }



        System.out.println("Enter the condition to evaluate . Use variables you already mentiond with $ appended . Please see the example below");
        System.out.println("($answer % [1,2,3,4]) & ($orderval % [100 - *])");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String condition = reader.readLine();
        System.out.println();
        //System.out.println("Conditional evaluates to" + decisionMaker.parse(condition , context));

    }

}
