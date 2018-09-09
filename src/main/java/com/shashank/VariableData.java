package com.shashank;

import java.util.List;


public class VariableData {

    public VariableData(VariableType variableType, List<Double> numbersList, List<String> stringsList, Double number, String string) {
        this.variableType = variableType;
        this.numbersList = numbersList;
        this.stringsList = stringsList;
        this.number = number;
        this.string = string;
    }

    public VariableType getVariableType() {
        return variableType;
    }

    public List<Double> getNumbersList() {
        return numbersList;
    }

    public List<String> getStringsList() {
        return stringsList;
    }

    public Double getNumber() {
        return number;
    }

    public String getString() {
        return string;
    }

    public enum VariableType {
        DOUBLE("DOUBLE"),
        LIST_DOUBLE("LIST_DOUBLE"),
        STRING("STRING"),
        LIST_STRING("LIST_STRING");

        String variableType;
        VariableType(String variableType) {
            this.variableType = variableType;
        }
    }
    private final VariableType variableType;
    private final List<Double> numbersList;
    private final List<String> stringsList;
    private final Double number;
    private final String string;

}
