package lexer;

public class Token {

    private TokenType type;
    private String value;
    private double val;

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    public Token(TokenType type, double value) {
        this.type = type;
        this.val = value;
    }

    public Token(TokenType type) {
        this.type = type;
        this.val = Double.NaN;
    }

    public void setValue(String value) {
        switch (type) {
            case CHAR:
                if (value.length() == 1) {
                    this.value = value;
                } else {
                    throw new IllegalArgumentException("(Character value is invalid = " + value + ")");
                }
                break;
            case FLOAT:
                try {
                    Float.parseFloat(value);
                    this.value = value;
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("(Float value is invalid = " + value + ")");
                }
                break;
            case BOOL:
                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                    this.value = value;
                } else {
                    throw new IllegalArgumentException("(Boolean value is invalid = " + value + ")");
                }
                break;
            case INT:
                try {
                    Integer.parseInt(value);
                    this.value = value;
                } catch (NumberFormatException e) {
                    try {
                        Float.parseFloat(value);
                        this.value = value;
                    } catch (NumberFormatException ex) {
                        throw new NumberFormatException("(Integer value is invalid = " + value + ")");
                    }
                }
                break;
            default:
                throw new UnsupportedOperationException("(Data Type does not exist = " + value + ")");
        }
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public double getVal() {
        return val;
    }

    public Object getDataType() {
        switch (type) {
            case CHAR:
                if (value.length() == 1) {
                    return value.charAt(0);
                } else {
                    throw new IllegalArgumentException("(Character value is invalid = " + value + ")");
                }
            case FLOAT:
                return Float.parseFloat(value);
            case BOOL:
                if (value != null) {
                    return Boolean.parseBoolean(value);
                } else {
                    return null;
                }
            case INT:
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("(Integer value is invalid = " + value + ")");
                }
            case DELIMITER:
                if (value.equals("(") || value.equals(")")) {
                    return null; // Or another appropriate indicator
                } else {
                    throw new UnsupportedOperationException("Unsupported delimiter: " + value);
                }
            default:
                throw new UnsupportedOperationException(toString());
        }
    }

    @Override
    public String toString() {
        return "(" + type + " = \"" + value + "\")";
    }
}
