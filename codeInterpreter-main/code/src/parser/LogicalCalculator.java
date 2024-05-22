package parser;
import java.util.*;

import lexer.Token;
import lexer.TokenType;

public class LogicalCalculator {
    private Stack<Token> operandStack;
    private Stack<Token> operatorStack;

    public LogicalCalculator() {
        operandStack = new Stack<>();
        operatorStack = new Stack<>();
    }

    public boolean evaluate(List<Token> tokens) {
        for (Token token : tokens) {
            // Visualize token being processed
//            System.out.println("Processing token: " + token);

            if (token.getType() == TokenType.BOOL || token.getType() == TokenType.INT || token.getType() == TokenType.FLOAT) {
                operandStack.push(token);
            } else if (token.getType() == TokenType.OPERATOR || token.getType() == TokenType.KEYWORD) {
                while (!operatorStack.isEmpty() && hasPrecedence(operatorStack.peek(), token)) {
                    performOperation();
                }
                operatorStack.push(token);
            } else if (token.getType() == TokenType.DELIMITER && token.getValue().equals("(")) {
                operatorStack.push(token);
            } else if (token.getType() == TokenType.DELIMITER && token.getValue().equals(")")) {
                while (!operatorStack.isEmpty() && operatorStack.peek().getType() != TokenType.DELIMITER) {
                    performOperation();
                }
                if (!operatorStack.isEmpty() && operatorStack.peek().getType() == TokenType.DELIMITER) {
                    operatorStack.pop(); // Remove the "("
                } else {
                    throw new IllegalArgumentException("Mismatched parentheses");
                }
            }

            // Visualize operand stack and operator stack after processing current token
//            System.out.println("Operand Stack: " + operandStack);
//            System.out.println("Operator Stack: " + operatorStack);
//            System.out.println("--------------------");
        }

        while (!operatorStack.isEmpty()) {
            performOperation();
        }

        if (operandStack.size() != 1 || operatorStack.size() != 0) {
            throw new IllegalArgumentException("Invalid expression");
        }

        Token resultToken = operandStack.pop();
        return Boolean.parseBoolean(resultToken.getValue());
    }

    private boolean hasPrecedence(Token op1, Token op2) {
        if ((op2.getType() == TokenType.KEYWORD && op2.getValue().equalsIgnoreCase("NOT")) ||
                (op1.getType() == TokenType.KEYWORD && (op1.getValue().equalsIgnoreCase("AND") || op1.getValue().equalsIgnoreCase("OR")))) {
            return false;
        }
        if ((op1.getType() == TokenType.KEYWORD && op1.getValue().equalsIgnoreCase("NOT")) ||
                (op1.getType() == TokenType.OPERATOR && (op1.getValue().equalsIgnoreCase("AND") || op1.getValue().equalsIgnoreCase("OR")))) {
            return true;
        }
        return false;
    }

    private void performOperation() {
        Token operator = operatorStack.pop();
//        System.out.println("Operator: " + operator);

        if (operator.getType() == TokenType.KEYWORD && operator.getValue().equals("NOT")) {
            Token operand = operandStack.pop();
            boolean operandValue = Boolean.parseBoolean(operand.getValue());
            operandStack.push(new Token(TokenType.BOOL, String.valueOf(!operandValue)));
//            System.out.println("NOT operand: " + operand.getValue());
        } else {
            Token operand2 = operandStack.pop();
            Token operand1 = operandStack.pop();
            boolean result = false;

            // Evaluate expression based on operand types
            if (operand1.getType() == TokenType.BOOL || operand2.getType() == TokenType.BOOL) {
                boolean operand1Value = Boolean.parseBoolean(operand1.getValue());
                boolean operand2Value = Boolean.parseBoolean(operand2.getValue());

                // Apply logical operator
                switch (operator.getValue()) {
                    case "AND":
                        result = operand1Value && operand2Value;
                        break;
                    case "OR":
                        result = operand1Value || operand2Value;
                        break;
                    case "<>":
                        result = operand1Value != operand2Value;
                        break;
                }
            } else {
                double operand1Num = Double.parseDouble(operand1.getValue());
                double operand2Num = Double.parseDouble(operand2.getValue());

                // Apply Compare operator
                switch (operator.getValue()) {
                    case ">":
                        result = operand1Num > operand2Num;
                        break;
                    case "<":
                        result = operand1Num < operand2Num;
                        break;
                    case ">=":
                        result = operand1Num >= operand2Num;
                        break;
                    case "<=":
                        result = operand1Num <= operand2Num;
                        break;
                    case "==":
                        result = operand1Num == operand2Num;
                        break;
                    case "<>":
                        result = operand1Num != operand2Num;
                        break;
                    default:
//                        System.out.println("Invalid operator: " + operator.getValue());
                }
            }

            // Push result onto operand stack
            operandStack.push(new Token(TokenType.BOOL, String.valueOf(result)));
//            System.out.println("Result pushed onto operand stack: " + result);
        }

//        System.out.println("Operand Stack: " + operandStack);
//        System.out.println("Operator Stack: " + operatorStack);
//        System.out.println("--------------------");
    }
}
