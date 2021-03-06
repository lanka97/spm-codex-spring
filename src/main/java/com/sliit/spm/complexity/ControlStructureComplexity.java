package com.sliit.spm.complexity;
import com.mongodb.util.JSON;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class ControlStructureComplexity {
    private static final String regex_for = "^for\\s\\(.+\\)|^for\\(.+\\)";
    private static final String regex_if = "^if\\s\\(.+\\)|^if\\(.+\\)";
    private static final String regex_while = "^while\\s\\(.+\\)|^while\\(.+\\)";
    private static final String regex_do_while = "while\\s\\(.+\\);|^while\\(.+\\);";
    private static final String regex_do = "^do\\s|^do";
    private static final String regex_switch = "^switch\\s\\(.+\\)|^switch\\(.+\\)";
    private static final String regex_case = "^case\\s.+\\s:|^case\\s.+:";

    private static boolean isControlStructure;
    private static boolean isNestedControlStructure;
    private static boolean isSwitch;

    private int ctc;
    private int totalCtc;
    private JSONArray tokens;

    public ControlStructureComplexity() {
        this.ctc = 0;
        this.totalCtc = 0;
        tokens = new JSONArray();
        isNestedControlStructure = false;
        isControlStructure = false;
        isSwitch =false;
    }

    private void detectConditionalControlStructure(String split){
        if(Pattern.matches(regex_if,split)){
            ctc++;
            tokens.put("if");
            isControlStructure = true;
            isNestedControlStructure = false;
        }
    }
    private void detectIterativeControlStructure(String split){
        if(Pattern.matches(regex_for,split)){
            ctc += 2;
            tokens.put("for");
            isNestedControlStructure = true;
            isControlStructure = false;

        }
        if(Pattern.matches(regex_do,split)){
            ctc+=2;
            tokens.put("do-while");
            isNestedControlStructure=true;
            isControlStructure = false;
        }
        if(!Pattern.matches(regex_do_while,split)) {
            if (Pattern.matches(regex_while, split)) {
                ctc += 2;
                tokens.put("while");
                isNestedControlStructure = true;
                isControlStructure = false;
            }
        }
    }

    private void detectLogicalOperator(String split){
        int increment = 0;
        if(isControlStructure) {
            increment = 1;
        }
        if(isNestedControlStructure) {
            increment = 2;
        }
        if(isNestedControlStructure | isControlStructure){
            char [] ch = split.toCharArray();
            int prev = 0;
            //38 and
            //124 or
            for(int c: ch){
                if(c==38){
                    if(prev!=38){
                        ctc += increment;
                        tokens.put("AND");
                    }
                }
                if(c==124){
                    if(prev!=124){
                        ctc += increment;
                        tokens.put("OR");
                    }
                }
                prev = c;
            }
        }
    }

    private void detectSwitch(String line){
        if(Pattern.matches(regex_switch,line)){
            isSwitch = true;
        }
    }
    private void detectCaseStatement(String line){
        if(isSwitch){
            if(Pattern.matches(regex_case,line)){
                ctc++;
                tokens.put("case");
            }
        }
    }

    public void measureCtc(String currentLine){
        String[] split = currentLine.split("\\{");
        ctc = 0;
        tokens = new JSONArray();
        for (String s : split) {
            s = s.trim();
            detectConditionalControlStructure(s);
            detectIterativeControlStructure(s);
            detectLogicalOperator(s);
            detectSwitch(s);
            detectCaseStatement(s);

            if(s.contains("}") | currentLine.contains("{")){
                isNestedControlStructure = false;
                isControlStructure = false;
                isSwitch = false;
            }
        }
        totalCtc += ctc;
    }

    int getCtc() {
        return ctc;
    }

    int getTotalCtc() {
        return totalCtc;
    }

    JSONArray getTokens(){
        return tokens;
    }
}
