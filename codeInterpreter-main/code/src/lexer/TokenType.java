package lexer;

public enum TokenType {
    //Program Structure
    IDENTIFIER,
    NEWLINE,
    ASSIGNMENT,
    CONCAT,
    KEYWORD,
    OPERATOR,
    DELIMITER,
    INVALID,
    EOF,

    //Data Type
    INT,
    CHAR,
    BOOL,
    FLOAT,
    STRING,

    //Arithmetic Operators
    PAREN_OPEN, PAREN_CLOSE,
    MULTIPLY, DIVIDE, MODULO,
    PLUS, MINUS,
    GREATER_THAN, LESS_THAN,
    GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL,
    EQUAL, NOT_EQUAL,

    //Logical operators
    AND,
    OR,
    NOT,
}