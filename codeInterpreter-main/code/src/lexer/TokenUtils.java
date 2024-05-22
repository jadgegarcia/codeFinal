package lexer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TokenUtils {

    private static final Set<String> keywordsAndSpecialTokens = new HashSet<>();
    private static final Set<String> booleanLiterals = new HashSet<>();
    private static final Set<Character> delimiters = new HashSet<>();
    private static final Map<Character, TokenType> operationChars = new HashMap<>();
    private static final Map<String, TokenType> logicalOperationChars = new HashMap<>();

    static {
        // Keywords and Special Tokens
        keywordsAndSpecialTokens.add("IF");
        keywordsAndSpecialTokens.add("ELSE");
        keywordsAndSpecialTokens.add("WHILE");
        keywordsAndSpecialTokens.add("FOR");
        keywordsAndSpecialTokens.add("CHAR");
        keywordsAndSpecialTokens.add("INT");
        keywordsAndSpecialTokens.add("FLOAT");
        keywordsAndSpecialTokens.add("DOUBLE");
        keywordsAndSpecialTokens.add("BOOL");
        keywordsAndSpecialTokens.add("RETURN");
        keywordsAndSpecialTokens.add("BEGIN");
        keywordsAndSpecialTokens.add("END");
        keywordsAndSpecialTokens.add("CODE");
        keywordsAndSpecialTokens.add("SCAN");
        keywordsAndSpecialTokens.add("DISPLAY");
        keywordsAndSpecialTokens.add("AND");
        keywordsAndSpecialTokens.add("OR");
        keywordsAndSpecialTokens.add("NOT");

        // Boolean
        booleanLiterals.add("TRUE");
        booleanLiterals.add("FALSE");

        // Delimiters
        delimiters.add('(');
        delimiters.add(')');
        delimiters.add('{');
        delimiters.add('}');
        delimiters.add(',');
        delimiters.add(';');
        delimiters.add(':');

        // Mathematical Operators
        operationChars.put('+', TokenType.PLUS);
        operationChars.put('-', TokenType.MINUS);
        operationChars.put('*', TokenType.MULTIPLY);
        operationChars.put('/', TokenType.DIVIDE);
        operationChars.put('%', TokenType.MODULO);
        operationChars.put('(', TokenType.PAREN_OPEN);
        operationChars.put(')', TokenType.PAREN_CLOSE);

        // Logical Operators
        logicalOperationChars.put("==", TokenType.EQUAL);
        logicalOperationChars.put("<", TokenType.LESS_THAN);
        logicalOperationChars.put(">", TokenType.GREATER_THAN);
        logicalOperationChars.put("<=", TokenType.LESS_THAN_OR_EQUAL);
        logicalOperationChars.put(">=", TokenType.GREATER_THAN_OR_EQUAL);
        logicalOperationChars.put("&&", TokenType.AND);
        logicalOperationChars.put("||", TokenType.OR);
        logicalOperationChars.put("!", TokenType.NOT);
        logicalOperationChars.put("!=", TokenType.NOT_EQUAL);
    }

    public static boolean isKeywordOrSpecialToken(String identifier) {
        return keywordsAndSpecialTokens.contains(identifier);
    }

    public static boolean isBooleanLiteral(String identifier) {
        return booleanLiterals.contains(identifier);
    }

    public static boolean isDelimiter(char c) {
        return delimiters.contains(c);
    }

    public static boolean isValidIdentifier(String identifier) {
        return identifier.matches("[a-zA-Z_][a-zA-Z0-9_]*");
    }

    public static boolean isOperator(char c) {
        return "+-*/()%=".indexOf(c) != -1;
    }

    public static TokenType getOperationToken(char c) {
        return operationChars.getOrDefault(c, null);
    }

    public static TokenType getLogicalOperationToken(String s) {
        return logicalOperationChars.getOrDefault(s, null);
    }

    public static void error(String message) {
        System.out.println("Lexer Error: " + message);
        System.exit(0);
    }

}