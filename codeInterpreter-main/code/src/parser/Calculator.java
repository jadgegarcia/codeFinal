package parser;

import java.util.ArrayList;
import java.util.ListIterator;

import lexer.Lexer;
import lexer.Token;
import lexer.TokenNode;
import lexer.TokenType;

public class Calculator {
      /**
   * Calls the Tokenizer and Parser, and returns the evaluated result of the parsed token tree
   */
  public static double getResult(String inputString) throws Exception {
    TokenNode expressionTree = Parser.parseTokens(Lexer.getMathTokens(inputString));
    return evaluateExpressionTree(expressionTree);
}

public static boolean getLogicalResult(String inputString) throws Exception {
    TokenNode expressionTree = Parser.parseTokens(Lexer.getMathTokens(inputString));
    return evaluateLogicalExpressionTree(expressionTree);
}

/**
 * Evaluates the result of the mathematical expression by recursively going through all the TokenNodes in the Parsed
 * Token Tree and evaluate the results at each node
 *
 * @return the result of the mathematical expression
 */
private static double evaluateExpressionTree(TokenNode node) {
    switch (node.type) {
        case INT: {
            return node.isNegative() ? -node.nodeValue : node.nodeValue;
        }
        case MULTIPLY: {
            return evaluateExpressionTree(node.operand1) * evaluateExpressionTree(node.operand2);
        }
        case DIVIDE: {
            return evaluateExpressionTree(node.operand1) / evaluateExpressionTree(node.operand2);
        }
        case PLUS: {
            return evaluateExpressionTree(node.operand1) + evaluateExpressionTree(node.operand2);
        }
        case MINUS: {
            return evaluateExpressionTree(node.operand1) - evaluateExpressionTree(node.operand2);
        }
        case MODULO: {
            return evaluateExpressionTree(node.operand1) % evaluateExpressionTree(node.operand2);
        }
        default: {
            // if somehow an invalid token gets processed by the Parser/Tokenizer
            throw new InternalError("Unknown Error Encountered");
        }
    }
}

private static boolean evaluateLogicalExpressionTree(TokenNode node){
    switch (node.type){
        case EQUAL: {
            return evaluateLogicalExpressionTree(node.operand1) == evaluateLogicalExpressionTree(node.operand2);       
        }
        default: {
            throw new InternalError("Wrong Method");
        } 
    }
}
    private class Parser
     {
        // to iterate over all tokens in the token list passed from the lexer
        private static ListIterator<Token> tokenIter;

        /**
         * Parse the list of tokens passed in and build a tree to evaluate results
         *
         * @param tokenList ArrayList of all the valid tokens to be parsed
         */
        public static TokenNode parseTokens(ArrayList<Token> tokenList) throws Exception {
            tokenIter = tokenList.listIterator();
            TokenNode result = expression(tokenIter.next());
    
            if (tokenIter.hasNext()) // unreachable / unexpected tokens found
                throw new Exception();
    
            return result;
        }
    
        /**
         * Create an expression from the generated tokens of the input
         */
        static TokenNode expression(Token current) throws Exception {
            TokenNode currentExpr = term(current);
    
            while (tokenIter.hasNext()) {
                current = tokenIter.next();
                // new terms encountered
                if (current.getType() == TokenType.PLUS) {
                    currentExpr = new TokenNode(TokenType.PLUS, currentExpr, term(tokenIter.next()));
                } else if (current.getType() == TokenType.MINUS) {
                    currentExpr = new TokenNode(TokenType.MINUS, currentExpr, term(tokenIter.next()));
                } 


                else if(current.getType() == TokenType.EQUAL){
                    currentExpr = new TokenNode(TokenType.EQUAL, currentExpr, term(tokenIter.next()));
                }

                // unexpected tokens found
                else {
                    tokenIter.previous();
                    break;
                }
            }
            return currentExpr;
        }
    
        /**
         * Create terms from the factors in the expression
         */
        static TokenNode term(Token current) throws Exception {
            TokenNode currentTerm = factor(current);
    
            while (tokenIter.hasNext()) {
                current = tokenIter.next();
                // new factors / expressions encountered
                if (current.getType() == TokenType.MULTIPLY) {
                    currentTerm = new TokenNode(TokenType.MULTIPLY, currentTerm, factor(tokenIter.next()));
                } else if (current.getType() == TokenType.DIVIDE) {
                    currentTerm = new TokenNode(TokenType.DIVIDE, currentTerm, factor(tokenIter.next()));
                } else if (current.getType() == TokenType.PAREN_OPEN) {
                    currentTerm = new TokenNode(TokenType.MULTIPLY, currentTerm, expression(current));
                } else if (current.getType() == TokenType.MODULO) {
                    currentTerm = new TokenNode(TokenType.MODULO, currentTerm, factor(tokenIter.next()));
                }
                // unexpected token found
                else {
                    tokenIter.previous();
                    break;
                }
            }
            return currentTerm;
        }
    
        /**
         * Create a tokenNode for the factors in the terms of the expression
         */
        static TokenNode factor(Token current) throws Exception {
            switch (current.getType()) {
                // an bracket enclosed expression found.
                case PAREN_OPEN: {
                    TokenNode expr = expression(tokenIter.next());
    
                    if (tokenIter.hasNext()) // iterate once more as PAREN_CLOSE encountered
                        tokenIter.next();
    
                    return expr;
                }

                // a +ve expression/number found
                case PLUS: {
                    return factor(tokenIter.next());
                }
                // a -ve expression/number found
                case MINUS: {
                    TokenNode next = factor(tokenIter.next());
                    next.negateNode();
                    return next;
                }
                // a number found
                case INT: {
                    return new TokenNode(current);
                }

                // unexpected token at start
                default: {
                    throw new Exception();
                }
            }
        }
}

}
