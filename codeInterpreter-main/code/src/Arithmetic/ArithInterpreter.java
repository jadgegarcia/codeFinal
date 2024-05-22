package Arithmetic;

import java.util.ArrayList;
import java.util.ListIterator;

public class ArithInterpreter {
      /**
   * Calls the Tokenizer and Parser, and returns the evaluated result of the parsed token tree
   */
  public static double getResult(String inputString) throws Exception {
    ArithTokenNode expressionTree = ArithParser.parseTokens(MathTokenizer.generateTokens(inputString));
    return evaluateExpressionTree(expressionTree);
}

public static boolean getLogicalResult(String inputString) throws Exception {
    ArithTokenNode expressionTree = ArithParser.parseTokens(MathTokenizer.generateTokens(inputString));
    return evaluateLogicalExpressionTree(expressionTree);
}

/**
 * Evaluates the result of the mathematical expression by recursively going through all the TokenNodes in the Parsed
 * Token Tree and evaluate the results at each node
 *
 * @return the result of the mathematical expression
 */
private static double evaluateExpressionTree(ArithTokenNode node) {
    switch (node.type) {
        case NUMBER: {
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

private static boolean evaluateLogicalExpressionTree(ArithTokenNode node){
    switch (node.type){
        case EQUAL: {
            return evaluateLogicalExpressionTree(node.operand1) == evaluateLogicalExpressionTree(node.operand2);       
        }
        default: {
            throw new InternalError("Wrong Method");
        } 
    }
}
private class ArithParser {
        // to iterate over all tokens in the token list passed from the lexer
        private static ListIterator<MathsToken> tokenIter;

        /**
         * Parse the list of tokens passed in and build a tree to evaluate results
         *
         * @param tokenList ArrayList of all the valid tokens to be parsed
         */
        public static ArithTokenNode parseTokens(ArrayList<MathsToken> tokenList) throws Exception {
            tokenIter = tokenList.listIterator();
            ArithTokenNode result = expression(tokenIter.next());
    
            if (tokenIter.hasNext()) // unreachable / unexpected tokens found
                throw new Exception();
    
            return result;
        }
    
        /**
         * Create an expression from the generated tokens of the input
         */
        static ArithTokenNode expression(MathsToken current) throws Exception {
            ArithTokenNode currentExpr = term(current);
    
            while (tokenIter.hasNext()) {
                current = tokenIter.next();
                // new terms encountered
                if (current.type == MathsToken.MatTokenType.PLUS) {
                    currentExpr = new ArithTokenNode(MathsToken.MatTokenType.PLUS, currentExpr, term(tokenIter.next()));
                } else if (current.type == MathsToken.MatTokenType.MINUS) {
                    currentExpr = new ArithTokenNode(MathsToken.MatTokenType.MINUS, currentExpr, term(tokenIter.next()));
                } 


                else if(current.type == MathsToken.MatTokenType.EQUAL){
                    currentExpr = new ArithTokenNode(MathsToken.MatTokenType.EQUAL, currentExpr, term(tokenIter.next()));
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
        static ArithTokenNode term(MathsToken current) throws Exception {
            ArithTokenNode currentTerm = factor(current);
    
            while (tokenIter.hasNext()) {
                current = tokenIter.next();
                // new factors / expressions encountered
                if (current.type == MathsToken.MatTokenType.MULTIPLY) {
                    currentTerm = new ArithTokenNode(MathsToken.MatTokenType.MULTIPLY, currentTerm, factor(tokenIter.next()));
                } else if (current.type == MathsToken.MatTokenType.DIVIDE) {
                    currentTerm = new ArithTokenNode(MathsToken.MatTokenType.DIVIDE, currentTerm, factor(tokenIter.next()));
                } else if (current.type == MathsToken.MatTokenType.PAREN_OPEN) {
                    currentTerm = new ArithTokenNode(MathsToken.MatTokenType.MULTIPLY, currentTerm, expression(current));
                } else if (current.type == MathsToken.MatTokenType.MODULO) {
                    currentTerm = new ArithTokenNode(MathsToken.MatTokenType.MODULO, currentTerm, factor(tokenIter.next()));
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
        static ArithTokenNode factor(MathsToken current) throws Exception {
            switch (current.type) {
                // an bracket enclosed expression found.
                case PAREN_OPEN: {
                    ArithTokenNode expr = expression(tokenIter.next());
    
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
                    ArithTokenNode next = factor(tokenIter.next());
                    next.negateNode();
                    return next;
                }
                // a number found
                case NUMBER: {
                    return new ArithTokenNode(current);
                }

                // unexpected token at start
                default: {
                    throw new Exception();
                }
            }
        }
}

}
