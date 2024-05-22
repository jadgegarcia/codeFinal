package parser;

import lexer.Token;
import lexer.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Parser {
    private List<Token> tokens;
    private int tokenIndex;
    private Map<String, Token> variables;

    private boolean begin_code;
    private boolean begin_if;
    private boolean begin_while;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.tokenIndex = 0;
        this.variables = new HashMap<>();

        this.begin_code = false;
        this.begin_if = false;
        this.begin_while = false;
    }

    private void eat() {
        tokenIndex++;
    }

    private Token currentToken() {
        return tokens.get(tokenIndex);
    }

    private TokenType currentTokenType() {
        return currentToken().getType();
    }

    private String currentTokenValue() {
        return currentToken().getValue();
    }

    private Token nextToken() {
        return tokens.get(tokenIndex);
    }

    private TokenType nextTokenType() {
        return nextToken().getType();
    }

    private String nextTokenValue() {
        return nextToken().getValue();
    }

    private boolean matchToken(TokenType type) {
        Token currentToken = tokens.get(tokenIndex);
        if (currentToken.getType() == type) {
            eat();
            return true;
        }
        isError("Expected token type " + type + " but found " + currentTokenType());
        return false;
    }

    private boolean matchToken(TokenType type, String value) {
        Token currentToken = tokens.get(tokenIndex);
        if (currentToken.getType() == type && currentTokenValue() == value) {
            eat();
            return true;
        }
        isError("Expected token type " + type + " but found " + currentTokenType());
        return false;
    }

    public void parse() {
        while (tokenIndex < tokens.size()) {
            statement();
        }
        if (tokenIndex < tokens.size()) {
            isError("Invalid parsed tokens = " + tokenIndex + ":" + tokens.size());
        }
    }

    private void statement() {
        Token currentToken = tokens.get(tokenIndex);

        if (currentToken.getType() == TokenType.IDENTIFIER) {
            assignmentStatement();
        }
        if (currentToken.getType() == TokenType.NEWLINE) {
            eat();
        }
        if (currentToken.getType() == TokenType.KEYWORD) {
            switch (currentTokenValue()) {
                case "BEGIN":
                    beginStatement();
                    break;
                case "END":
                    endStatement();
                    break;
                case "INT":
                case "FLOAT":
                case "CHAR":
                case "BOOL":
                    operatorStatement();
                    break;
                case "DISPLAY":
                    displayStatement();
                    break;
                case "SCAN":
                    scanStatement();
                    break;
                case "IF":
                    ifStatement();
                    break;
                case "WHILE":
                    whileStatement();
                    break;
                default:
                    isError("Invalid statement: " + currentToken);
            }
        }
    }

    // ---------------------------------------- Statement Fucntions Section
    // ----------------------------------------//

    private void assignmentStatement() {
        List<Token> identifiers = new ArrayList<>();
        identifiers.add(currentToken());
        matchToken(TokenType.IDENTIFIER);
        matchToken(TokenType.ASSIGNMENT, "=");

        List<Token> tokens = new LinkedList<>();

        while (currentTokenType() != TokenType.NEWLINE) {
            if (currentTokenType() == TokenType.ASSIGNMENT) {
                eat();
            } else if (isIdentifierAssignment()) {
                identifiers.add(currentToken());
                eat();
            } else if (isExpressionToken()) {
                while (currentToken().getType() != TokenType.NEWLINE) {
                    if (isLogicalOperator(nextTokenValue())) {
                        tokens.add(new Token(TokenType.DELIMITER, "("));
                        tokens.add(getVariableToken());
                        eat();
                        tokens.add(currentToken());
                        eat();
                        tokens.add(getVariableToken());
                        eat();
                        tokens.add(new Token(TokenType.DELIMITER, ")"));
                    } else {
                        tokens.add(getVariableToken());
                        eat();
                    }
                }
            }
        }

        assignValues(identifiers, tokens);
        matchToken(TokenType.NEWLINE);
    }

    private void beginStatement() {
        eat();
        switch (currentTokenValue()) {
            case "CODE":
                eat();
                begin_code = true;
                if (!matchToken(TokenType.NEWLINE)) {
                    isError("NEWLINE after BEGIN CODE");
                }
                break;
            case "IF":
                eat();
                begin_if = true;
                if (!matchToken(TokenType.NEWLINE)) {
                    isError("NEWLINE after BEGIN IF");
                }
                break;
            case "WHILE":
                eat();
                begin_while = true;
                if (!matchToken(TokenType.NEWLINE)) {
                    isError("NEWLINE after BEGIN WHILE");
                }
                break;
        }
    }

    private void endStatement() {
        eat();
        switch (currentTokenValue()) {
            case "CODE":
                eat();
                begin_code = false;
                if (!matchToken(TokenType.NEWLINE)) {
                    isError("NEWLINE after END CODE");
                }
                if (!matchToken(TokenType.EOF)) {
                    isError("Expected EOF");
                }
                break;
            case "IF":
                eat();
                begin_if = false;
                if (!matchToken(TokenType.NEWLINE)) {
                    isError("NEWLINE after END IF");
                }
                break;
            case "WHILE":
                eat();
                begin_while = false;
                if (!matchToken(TokenType.NEWLINE)) {
                    isError("NEWLINE after END WHILE");
                }
                break;
        }
    }

    private void operatorStatement() {
        String value = currentTokenValue();
        TokenType type = getExpectedType(value);

        while (currentTokenType() != TokenType.NEWLINE) {
            if (currentTokenValue().equals(",")) {
                matchToken(TokenType.DELIMITER);
            }

            String varname = currentTokenValue();
            matchToken(TokenType.IDENTIFIER);

            if (currentTokenValue().equals("=")) {
                eat();

                if (nextTokenType() != TokenType.OPERATOR && currentTokenType() != TokenType.IDENTIFIER) {
                    if (type == TokenType.INT) {
                        if (currentTokenType() == TokenType.DELIMITER) {
                            processVariableEquation(value, type, varname);
                        } else {
                            variables.put(varname, currentToken());
                            eat();
                        }
                    } else {
                        processVariableEquation(value, type, varname);
                    }
                } else if (currentToken().getValue().equals(",")) {
                    eat();
                    variables.put(varname, new Token(type, null));
                } else if (currentTokenType() == TokenType.NEWLINE) {
                    variables.put(varname, new Token(type, null));
                } else {
                    isError("Unexpected token type: " + currentToken());
                }
            }
        }
        matchToken(TokenType.NEWLINE);
    }

    private void displayStatement() {

    }

    private void scanStatement() {

    }

    private void ifStatement() {

    }

    private void whileStatement() {

    }

    // ---------------------------------------- Helper Fucntions Section
    // ----------------------------------------//

    // Assignment Statement
    private boolean isIdentifierAssignment() {
        TokenType nextType = nextTokenType();
        return currentTokenType() == TokenType.IDENTIFIER &&
                nextType != TokenType.OPERATOR &&
                nextType != TokenType.NEWLINE &&
                nextType != TokenType.DELIMITER;
    }

    private boolean isExpressionToken() {
        TokenType type = currentTokenType();
        return type == TokenType.INT || type == TokenType.FLOAT ||
                type == TokenType.DELIMITER || type == TokenType.BOOL ||
                type == TokenType.IDENTIFIER;
    }

    private boolean isLogicalStatement(List<Token> tokens) {
        for (Token token : tokens) {
            if (isLogicalOperator(token.getValue())) {
                return true;
            }
        }
        return false;
    }

    private boolean isLogicalOperator(String value) {
        return value.equals(">") || value.equals("<") ||
                value.equals("<>") || value.equals("==") ||
                value.equals(">=") || value.equals("<=") ||
                value.equals("AND") || value.equals("OR");
    }

    private Token getVariableToken() {
        if (currentTokenType() == TokenType.IDENTIFIER) {
            return variables.get(currentTokenValue());
        }
        return currentToken();
    }

    private void assignValues(List<Token> identifiers, List<Token> tokens) {
        for (Token var : identifiers) {
            if (variables.containsKey(var.getValue())) {
                String result = evaluateExpression(tokens);
                variables.get(var.getValue()).setValue(result);
            } else {
                isError("Variable: " + var + " must be declared first");
            }
        }
    }

    private String evaluateExpression(List<Token> tokens) {
        StringBuilder tokenValuesBuilder = new StringBuilder();
        for (Token token : tokens) {
            if (token.getType() == TokenType.IDENTIFIER) {
                tokenValuesBuilder.append(variables.get(token.getValue()));
            } else {
                tokenValuesBuilder.append(token.getValue());
            }
        }

        if (isLogicalStatement(tokens)) {
            LogicalCalculator logicalCalculator = new LogicalCalculator();
            return Boolean.toString(logicalCalculator.evaluate(tokens));
        } else if (containsFloat(tokens)) {
            try {
                return Double.toString(Calculator.getResult(tokenValuesBuilder.toString()));
            } catch (Exception e) {
                System.out.println("Invalid Input");
                return "";
            }
        } else {
            try {
                return Integer.toString((int) Calculator.getResult(tokenValuesBuilder.toString()));
            } catch (Exception e) {
                System.out.println("Invalid Input");
                return "";
            }
        }
    }

    private boolean containsFloat(List<Token> tokens) {
        for (Token token : tokens) {
            if (token.getType() == TokenType.FLOAT) {
                return true;
            }
        }
        return false;
    }

    // Operator Statement
    private TokenType getExpectedType(String value) {
        eat();
        switch (value) {
            case "INT":
                return TokenType.INT;
            case "FLOAT":
                return TokenType.FLOAT;
            case "BOOL":
                return TokenType.BOOL;
            case "CHAR":
                return TokenType.CHAR;
            default:
                return null;
        }
    }

    private void processVariableEquation(String value, TokenType type, String varname) {
        StringBuilder tokenValuesBuilder = new StringBuilder();
        while (currentTokenType() != TokenType.NEWLINE && !currentTokenType().equals(",")) {
            if (currentTokenType() == TokenType.IDENTIFIER) {
                tokenValuesBuilder.append(variables.get(currentTokenValue()).getDataType());
            } else {
                tokenValuesBuilder.append(currentTokenValue());
            }
            eat();
        }

        try {
            if (type == TokenType.INT) {
                variables.put(varname,
                        new Token(type, Integer.toString((int) Calculator.getResult(tokenValuesBuilder.toString()))));
            } else {
                variables.put(varname,
                        new Token(type, Double.toString(Calculator.getResult(tokenValuesBuilder.toString()))));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //

    public static String isError(String message) {
        return "Parser Error: " + message;
    }

}
