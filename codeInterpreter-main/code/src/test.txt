package parser;

import java.util.*;

import lexer.Token;
import lexer.TokenType;

public class SyntaxAnalyzer {
    private List<Token> tokens;
    private int tokenIndex;
    private boolean begin_code;
    private Map<String, Token> variables;
    Scanner scanner;

    public SyntaxAnalyzer(List<Token> tokens) {
        this.tokens = tokens;
        this.tokenIndex = 0;
        this.begin_code = false;
        this.variables = new HashMap<>();
        this.scanner = new Scanner(System.in);
        ;
    }

    private Token currentToken() {
        if (tokenIndex < tokens.size()) {
            return tokens.get(tokenIndex);
        } else {
            return null; // No more tokens left
        }
    }

    private TokenType currentTokenType() {
        return currentToken().getType();
    }

    private String currentTokenValue() {
        return currentToken().getValue();
    }

    private Token nextToken() {
        if (tokenIndex < tokens.size()) {
            return tokens.get(tokenIndex + 1);
        } else {
            return null; // No more tokens left
        }
    }

    private TokenType nextTokenType() {
        return nextToken().getType();
    }

    private String nextTokenValue() {
        return nextToken().getValue();
    }

    private void eat() {
        tokenIndex++;
    }

    private void matchToken(TokenType type) {
        if (tokenIndex < tokens.size()) {
            Token currentToken = tokens.get(tokenIndex);
            if (currentToken.getType() == type) {
                eat();
            } else {
                error("Mismatch Token: Expected = " + type + " || Current = " + currentToken.getType());
            }
        }
    }

    private void matchToken(TokenType type, String value) {
        if (tokenIndex < tokens.size()) {
            Token currentToken = tokens.get(tokenIndex);
            if (currentToken.getType() == type && currentToken.getValue().equals(value)) {
                eat();
            } else {
                error("Mismatch Token: Expected Type = " + type + " Expected Value = " + value + " || Current type = "
                        + currentToken.getType() + " Current value = " + currentToken.getValue());
            }
        }
    }

    public void parse() {
        while (tokenIndex < tokens.size()) {
            statement();
        }
        if (tokenIndex < tokens.size()) {
            error("Exceeded expected tokens: Max amount = " + tokens.size() + " || Current Amount = " + tokenIndex);
        }
    }

    public int countNewline(int from, int to) {
        int count = 0;
        for (int i = from; i < to + 1; i++) {
            if (tokens.get(i).getType() == TokenType.NEWLINE)
                count++;
        }
        return count;
    }

    private void statement() {
        if (!begin_code && !tokens.get(tokenIndex).getValue().equals("BEGIN")
                && !tokens.get(tokenIndex).getValue().equals("CODE")) {
            eat();
            return;
        }

        if (tokenIndex < tokens.size()) {
            Token currentToken = tokens.get(tokenIndex);
            if (currentToken.getType() == TokenType.IDENTIFIER) {
                assignmentStatement();
            } else if (currentToken.getType() == TokenType.NEWLINE) {
                eat();
            } else if (currentToken.getType() == TokenType.KEYWORD) {
                switch (currentToken.getValue()) {
                    case "BEGIN":
                        eat();
                        beginStatement();
                        break;
                    case "END":
                        eat();
                        endStatement();
                        break;
                    case "INT":
                    case "CHAR":
                    case "BOOL":
                    case "FLOAT":
                        eat();
                        declareStatement(currentToken.getValue());
                        break;
                    case "DISPLAY":
                        eat();
                        matchToken(TokenType.DELIMITER, ":");
                        displayStatement();
                        break;
                    case "SCAN":
                        eat();
                        matchToken(TokenType.DELIMITER, ":");
                        scanStatement();
                        break;
                    case "IF":
                        eat();
                        ifStatement();
                        break;
                    case "WHILE":
                        eat();
                        whileStatement();
                        break;
                }
            } else {
                error("Invalid statement:" + currentToken);
            }
        }
    }

    private void assignmentStatement() {
        ArrayList<Token> identifiers = new ArrayList<>();

        identifiers.add(currentToken());
        matchToken(TokenType.IDENTIFIER);
        matchToken(TokenType.ASSIGNMENT, "=");

        List<Token> tokens = new LinkedList<>();

        while (currentTokenType() != TokenType.NEWLINE) {
            if (currentTokenType() == TokenType.ASSIGNMENT) {
                eat(); // eat the ASSIGNMENT token
            } else if (currentTokenType() == TokenType.IDENTIFIER
                    && !isNextType(TokenType.OPERATOR, TokenType.NEWLINE, TokenType.DELIMITER)) {
                identifiers.add(currentToken());
                eat(); // eat the IDENTIFIER token
            } else if (isCurrentType(TokenType.INT, TokenType.FLOAT, TokenType.DELIMITER, TokenType.BOOL,
                    TokenType.CHAR, TokenType.IDENTIFIER)) {
                while (currentTokenType() != TokenType.NEWLINE) {
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

        for (Token var : identifiers) {
            if (variables.containsKey(var.getValue())) {
                StringBuilder tokenValuesBuilder = new StringBuilder();
                for (Token token : tokens) {
                    if (token.getType() == TokenType.IDENTIFIER)
                        tokenValuesBuilder.append(variables.get(token.getValue()));
                    else
                        tokenValuesBuilder.append(token.getValue());
                }
                if (isLogicalStatement(tokens)) {
                    LogicalCalculator logicalCalculator = new LogicalCalculator();
                    variables.get(var.getValue()).setValue(Boolean.toString(logicalCalculator.evaluate(tokens)));
                } else if (containsFloat(tokens)) {
                    try {
                        variables.get(var.getValue())
                                .setValue(Double.toString(Calculator.getResult(tokenValuesBuilder.toString())));
                    } catch (Exception ignored) {
                        System.out.println("Invalid Input");
                    }
                } else if (containsChar(tokens)) {
                    // Handle CHAR datatype assignment
                    String charValue = tokens.stream()
                            .filter(t -> t.getType() == TokenType.CHAR)
                            .findFirst()
                            .map(Token::getValue)
                            .orElseThrow(() -> new IllegalArgumentException("CHAR value missing"));
                    variables.get(var.getValue()).setValue(charValue);
                } else {
                    try {
                        variables.get(var.getValue())
                                .setValue(Integer.toString((int) Calculator.getResult(tokenValuesBuilder.toString())));
                    } catch (Exception ignored) {
                        System.out.println("Invalid Input");
                    }
                }
            } else
                error("Variable: " + var + " must be declared first");
        }
        matchToken(TokenType.NEWLINE);
    }

    // Helper method to check if tokens contain CHAR type
    private boolean containsChar(List<Token> tokens) {
        return tokens.stream().anyMatch(t -> t.getType() == TokenType.CHAR);
    }

    private void beginStatement() {
        if (tokens.get(tokenIndex).getValue().equals("CODE")) {
            eat();
            if (begin_code) {
                error("Code has already started");
            }
            begin_code = true;
            if (currentTokenType() == TokenType.NEWLINE) {
                eat();
            } else {
                error("Expected a NEWLINE");
            }
        }
    }

    private void endStatement() {
        if (tokens.get(tokenIndex).getValue().equals("CODE")) {
            eat();
            begin_code = false;
            if (currentTokenType() == TokenType.NEWLINE) {
                eat();
            } else {
                error("Expected a NEWLINE");
            }

            if (currentTokenType() == TokenType.EOF) {
                eat();
            } else {
                error("Expected a EOF");
            }
            System.out.println("\nNo Error");
        }
    }

    private void declareStatement(String value) {
        TokenType type = getVariableType(value);

        while (currentTokenType() != TokenType.NEWLINE) {
            if (currentTokenValue().equals(",")) {
                matchToken(TokenType.DELIMITER);
            }

            String varname = currentTokenValue();
            matchToken(TokenType.IDENTIFIER);

            if (currentTokenValue() == "=") {
                eat();
                checkVariableDeclaration(varname, type);

                if (nextTokenType() != TokenType.OPERATOR && currentTokenType() != TokenType.IDENTIFIER
                        && currentTokenType() != TokenType.DELIMITER && currentTokenType() != TokenType.BOOL) {
                    if (type == TokenType.INT) {
                        currentToken().setValue(Integer.toString((int) currentToken().getDataType()));
                    }
                    variables.put(varname, currentToken());
                    eat();
                } else {
                    StringBuilder tokenValuesBuilder = new StringBuilder();
                    List<Token> logicalTokens = new LinkedList<>();
                    while (currentTokenType() != TokenType.NEWLINE && !currentTokenValue().equals(",")) {
                        logicalTokens.add(currentToken());
                        if (currentTokenType() == TokenType.IDENTIFIER) {
                            tokenValuesBuilder.append(variables.get(currentTokenValue()).getDataType());
                        } else
                            tokenValuesBuilder.append(currentTokenValue());
                        eat();
                    }
                    try {
                        if (type == TokenType.INT) {
                            variables.put(varname, new Token(type,
                                    Integer.toString((int) Calculator.getResult(tokenValuesBuilder.toString()))));
                        } else if (type == TokenType.FLOAT)
                            variables.put(varname, new Token(type,
                                    Double.toString(Calculator.getResult(tokenValuesBuilder.toString()))));
                        else if (type == TokenType.BOOL) {
                            Object res = expression(logicalTokens);
                            variables.put(varname, new Token(type, res.toString()));
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            } else if (currentTokenValue().equals(",")) {
                eat(); // eat DELIMITER token
                variables.put(varname, new Token(type, null));
            } else if (currentTokenType() == TokenType.NEWLINE) {
                variables.put(varname, new Token(type, null));
            } else
                error("Unexpected token type: " + currentToken());
        }
        matchToken(TokenType.NEWLINE);
    }

    // ex. DISPLAY: x
    // Token: DISPLAY (KEYWORD)
    // Token: x (IDENTIFIER)
    private void displayStatement() {
        while (currentTokenType() != TokenType.NEWLINE) {
            List<Token> tokens = new LinkedList<>();
            while (currentTokenType() != TokenType.CONCAT) {
                tokens.add(currentToken());
                eat();
                if (currentTokenType() == TokenType.NEWLINE)
                    break;
            }

            if (currentTokenType() != TokenType.NEWLINE) {
                matchToken(TokenType.CONCAT);
            }

            // for (Token token : tokens) {
            // System.out.println(token);
            // }

            if (tokens.size() == 1) {
                if (tokens.get(0).getType() == TokenType.IDENTIFIER) {
                    
                    try {

                        if (variables.get(tokens.get(0).getValue()).getDataType() instanceof Boolean) {

                            System.out.print(
                                    variables.get(tokens.get(0).getValue()).getDataType().toString().toUpperCase());
                        } else {
                            System.out.print(variables.get(tokens.get(0).getValue()).getDataType());
                        }
                    } catch (NullPointerException e) {
                        if (variables.containsKey(tokens.get(0).getValue()))
                            System.out.print("null");
                        else
                            throw new RuntimeException("Variable: " + tokens.get(0) + " is not yet declared");
                    }
                } else if (tokens.get(0).getType() == TokenType.STRING) {
                    if (tokens.get(0).getValue().equals("$"))
                        System.out.println("");
                    else
                        System.out.print(tokens.get(0).getValue());
                }
            } else {
                System.out.print(expression(tokens));
            }
            tokens.clear();
        }
        matchToken(TokenType.NEWLINE);
    }

    // [Collect Condition Tokens] -> [Match Tokens] -> [Find End of While Block] ->
    // [Execute While Loop] -> [Restore Token Index]
    private void whileStatement() {
        List<Token> expressionTokens = new LinkedList<>();
        while (currentTokenType() != TokenType.NEWLINE) {
            expressionTokens.add(currentToken());
            eat();
        }

        matchToken(TokenType.NEWLINE);
        matchToken(TokenType.KEYWORD, "BEGIN");
        matchToken(TokenType.KEYWORD, "WHILE");
        matchToken(TokenType.NEWLINE);

        int startwhileIndex = tokenIndex;
        int nestedCount = 0;

        while (true) {
            if (currentTokenValue().equals("BEGIN") && nextTokenValue().equals("WHILE"))
                nestedCount++;
            if (currentTokenValue().equals("END") && nextTokenValue().equals("WHILE"))
                nestedCount--;
            if (nestedCount == -1) {
                break;
            }
            eat();
        }

        int endwhileIndex = tokenIndex;

        while (Boolean.parseBoolean(expression(expressionTokens).toString())) {
            tokenIndex = startwhileIndex;
            while (tokenIndex < tokens.size() && tokenIndex <= endwhileIndex) {
                statement();
            }
        }

        tokenIndex = endwhileIndex;
    }

    private void scanStatement() {
        while (currentTokenType() == TokenType.IDENTIFIER || currentTokenType() == TokenType.DELIMITER) {
            if (currentTokenType() == TokenType.IDENTIFIER) {
                try {
                    if (variables.containsKey(currentTokenValue())) {
                        String newValue = scanner.nextLine();
                        variables.get(currentTokenValue()).setValue(newValue);
                    } else
                        throw new RuntimeException("Variable: " + currentTokenValue() + " is not yet declared");
                } catch (NullPointerException e) {
                }
                eat();
            } else if (currentTokenType() == TokenType.DELIMITER) {
                eat();
            } else
                error("Unexpected Token in Scan:" + currentToken());
        }
        matchToken(TokenType.NEWLINE);
    }

    private void ifStatement() {
        matchToken(TokenType.DELIMITER, "(");
        Boolean parseStatement = ifExpression();
        matchToken(TokenType.DELIMITER, ")");

        while (currentTokenType().equals(TokenType.NEWLINE)) {
            eat();
        }
        matchToken(TokenType.KEYWORD, "BEGIN");
        matchToken(TokenType.KEYWORD, "IF");
        eat();
        if (parseStatement) {
            while (currentTokenType() != TokenType.KEYWORD || !currentTokenValue().equals("END")) {
                statement();
            }
        } else {
            int nestedCount = 0;
            while (true) {
                // System.out.println("Concomed: " + currentToken() + " Count: " + nestedCount);
                if (currentTokenValue().equals("BEGIN") && nextTokenValue().equals("IF"))
                    nestedCount++;
                if (currentTokenValue().equals("END") && nextTokenValue().equals("IF"))
                    nestedCount--;
                if (nestedCount == -1) {
                    break;
                }
                eat();
            }
        }
        matchToken(TokenType.KEYWORD, "END");
        matchToken(TokenType.KEYWORD, "IF"); // first if statement finished
        eat();
        List<Boolean> ifelseResult = new LinkedList<>();
        ifelseResult.add(parseStatement);
        // check for multiple alternatives

        while (true) {
            if (currentTokenType() == TokenType.KEYWORD && currentTokenValue().equals("ELSE")) { // if else keyword
                // encountered
                eat();
                if (currentTokenType() == TokenType.KEYWORD && currentTokenValue().equals("IF")) { // should keep
                                                                                                   // checking for
                    // else ifs
                    eat();
                    matchToken(TokenType.DELIMITER, "(");
                    Boolean parseStatement2 = ifExpression();
                    matchToken(TokenType.DELIMITER, ")");
                    eat();
                    matchToken(TokenType.KEYWORD, "BEGIN");
                    matchToken(TokenType.KEYWORD, "IF");
                    eat();
                    if (parseStatement2 && !ifelseResult.contains(true)) {
                        while (currentTokenType() != TokenType.KEYWORD || !currentTokenValue().equals("END")) {
                            statement();
                        }
                    } else {
                        int nestedCount = 0;
                        while (true) {
                            if (currentTokenValue().equals("BEGIN") && nextTokenValue().equals("IF"))
                                nestedCount++;
                            if (currentTokenValue().equals("END") && nextTokenValue().equals("IF"))
                                nestedCount--;
                            if (nestedCount == -1) {
                                break;
                            }
                            eat();
                        }
                    }
                    ifelseResult.add(parseStatement2);
                    matchToken(TokenType.KEYWORD, "END");
                    matchToken(TokenType.KEYWORD, "IF");
                    eat();
                } else {
                    eat();
                    matchToken(TokenType.KEYWORD, "BEGIN");
                    matchToken(TokenType.KEYWORD, "IF");
                    eat();

                    if (ifelseResult.contains(true)) {
                        int nestedCount = 0;
                        while (true) {
                            if (currentTokenValue().equals("BEGIN") && nextTokenValue().equals("IF"))
                                nestedCount++;
                            if (currentTokenValue().equals("END") && nextTokenValue().equals("IF"))
                                nestedCount--;
                            if (nestedCount == -1) {
                                break;
                            }
                            eat();
                        }
                    } else {
                        while (currentTokenType() != TokenType.KEYWORD || !currentTokenValue().equals("END")) {
                            statement();
                        }
                    }
                    matchToken(TokenType.KEYWORD, "END");
                    matchToken(TokenType.KEYWORD, "IF");
                    eat();
                    break;
                }
            } else {
                break;
            }
        }

    }

    private Object expression(List<Token> expressionTokens) {
        List<Token> tokens = new LinkedList<>();
        for (Token token : expressionTokens) {
            if (token.getType() == TokenType.IDENTIFIER) {
                Token tok = variables.get(token.getValue());
                if (tok != null)
                    tokens.add(variables.get(token.getValue()));
                else
                    error(token + " is not declared");
            } else
                tokens.add(token);
        }

        StringBuilder tokenValuesBuilder = new StringBuilder();
        for (Token token : tokens) {
            tokenValuesBuilder.append(token.getValue());
        }

        Object result = null;
        if (isLogicalStatement(tokens)) {
            LogicalCalculator logicalCalculator = new LogicalCalculator();
            List<Token> logicalTokens = new LinkedList<>();
            for (int i = 0; i < tokens.size(); i++) {
                if (i < tokens.size() - 1) {
                    if (isNumberorFloat(tokens.get(i)) && isArithOperator(tokens.get(i + 1))) {
                        StringBuilder arithmeticBuilder = new StringBuilder();
                        arithmeticBuilder.append(tokens.get(i).getValue());
                        arithmeticBuilder.append(tokens.get(i + 1).getValue());
                        arithmeticBuilder.append(tokens.get(i + 2).getValue());
                        // System.out.println(arithmeticBuilder.toString() + "result");
                        double res = 0;
                        try {
                            res = Calculator.getResult(arithmeticBuilder.toString());
                        } catch (Exception e) {
                            // throw new RuntimeException("ERRORRRRRRRRRRRRRRRRRRRRRRRRRRR");
                            error("Invalid operation: " + arithmeticBuilder.toString());
                        }
                        Token newToken = new Token(TokenType.FLOAT, Double.toString(res));
                        logicalTokens.add(newToken);
                        i += 2;
                    } else {
                        logicalTokens.add(tokens.get(i));
                    }
                }
                if (i == tokens.size() - 1)
                    logicalTokens.add(tokens.get(i));
            }

            result = Boolean.toString(logicalCalculator.evaluate(logicalTokens));
        } else if (containsFloat(tokens)) {
            try {
                result = Double.toString(Calculator.getResult(tokenValuesBuilder.toString()));
            } catch (Exception e) {
                System.err.println("Invalid operation: " + tokenValuesBuilder.toString());
            }
        } else {
            try {
                result = Integer.toString((int) Calculator.getResult(tokenValuesBuilder.toString()));
            } catch (Exception e) {
                System.err.println("Invalid operation: " + tokenValuesBuilder.toString());
            }
        }
        return result;
    }

    // ----------------------------- Helper Functions -----------------------------
    // //

    // Assingment Statment
    private boolean isCurrentType(TokenType... types) {
        for (TokenType type : types) {
            if (currentTokenType() == type) {
                return true;
            }
        }
        return false;
    }

    private boolean isNextType(TokenType... types) {
        for (TokenType type : types) {
            if (currentTokenType() == type) {
                return true;
            }
        }
        return false;
    }

    private Token getVariableToken() {
        if (currentTokenType() == TokenType.IDENTIFIER) {
            return variables.get(currentTokenValue());
        }
        return currentToken();
    }

    // Declaration Statement
    private TokenType getVariableType(String value) {
        switch (value) {
            case "INT":
                return TokenType.INT;
            case "CHAR":
                return TokenType.CHAR;
            case "BOOL":
                return TokenType.BOOL;
            case "FLOAT":
                return TokenType.FLOAT;
            default:
                return null;
        }
    }

    private void checkVariableDeclaration(String varname, TokenType type) {
        if (variables.containsKey(varname)) {
            error("Variable name: " + varname + " is already declared");
        }

        if (currentTokenType() != type) {
            validateTokenType(type, varname);
        }
    }

    private void validateTokenType(TokenType type, String varname) {
        if (currentTokenType() == TokenType.IDENTIFIER) {
            TokenType varDataType = variables.get(varname).getType();
            boolean result;
            switch (type) {
                case CHAR:
                    result = type == TokenType.CHAR;
                case INT:
                case FLOAT:
                    result = type != TokenType.CHAR && type != TokenType.BOOL;
                case BOOL:
                    result = type == TokenType.BOOL;
                default:
                    result = false;
            }
            if (!result) {
                error("Unmatched datatype Expected datatype: " + type + " Defined datatype: " + varDataType + " "
                        + currentToken());
            }
        } else if (isMismatchedType(type)) {
            throw new IllegalArgumentException(
                    "Unmatched datatype Expected datatype: " + type + " Defined datatype: " + currentTokenType());
        }
    }

    private boolean isMismatchedType(TokenType type) {
        return (type == TokenType.CHAR || type == TokenType.BOOL)
                && (currentTokenType() == TokenType.INT || currentTokenType() == TokenType.FLOAT)
                || (currentTokenType() == TokenType.CHAR || currentTokenType() == TokenType.BOOL)
                        && (type == TokenType.INT || type == TokenType.FLOAT);
    }

    private boolean ifExpression() {
        List<Token> tokensForIf = new LinkedList<>();
        while (nextTokenType() != TokenType.NEWLINE && currentTokenValue() != ")") {
            tokensForIf.add(currentToken());
            eat();
        }
        return Boolean.parseBoolean(expression(tokensForIf).toString());
    }

    public static boolean containsFloat(List<Token> tokens) {
        for (Token token : tokens) {
            if (token.getType() == TokenType.FLOAT) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNumberorFloat(Token token) {
        if (token.getType() == TokenType.INT || token.getType() == TokenType.FLOAT)
            return true;
        return false;
    }

    public static boolean isArithOperator(Token token) {
        if (token.getValue().equals("+") || token.getValue().equals("-") || token.getValue().equals("/")
                || token.getValue().equals("*") || token.getValue().equals("%"))
            return true;
        return false;
    }

    private boolean isLogicalOperator(String value) {
        return value.equals(">") || value.equals("<") ||
                value.equals("<>") || value.equals("==") ||
                value.equals(">=") || value.equals("<=") ||
                value.equals("AND") || value.equals("OR");
    }

    private boolean isLogicalStatement(List<Token> tokens) {
        for (Token token : tokens) {
            if (token.getValue().equals("==") || token.getValue().equals("<>") || token.getValue().equals(">=")
                    || token.getValue().equals("<=") || token.getValue().equals(">") || token.getValue().equals("<")
                    || token.getValue().equals("AND") || token.getValue().equals("OR") || token.getValue().equals("NOT")
                    || token.getType() == TokenType.BOOL) {
                return true;
            }
        }
        return false;
    }

    private void error(String message) {
        System.out.println("\nSyntax Analyzer Error: " + message);
        System.exit(0);
    }

}