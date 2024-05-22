import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lexer.Lexer;
import lexer.Token;
import parser.SyntaxAnalyzer;

public class Main {
    // Main method
    public static void main(String[] args) {
        // Define the filename containing the CODE program
        String filename = "D:\\CODE\\FINAL\\interpreter\\codeInterpreter-main\\code\\src\\sourceCode.txt";
        
        try {
            // Create a FileReader to read the file
            FileReader fileReader = new FileReader(filename);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            // Create a StringBuilder to store the CODE program
            StringBuilder codeBuilder = new StringBuilder();
            String line;
            // Read each line from the file and append it to the StringBuilder
            while ((line = bufferedReader.readLine()) != null) {
                codeBuilder.append(line).append("@\n");
            }

            bufferedReader.close();
            // Create a Lexer instance
            Lexer lexer = new Lexer(codeBuilder.toString());

            List<Token> tokenList = new ArrayList<>();
            // Token token;

            // do {
            //     token = lexer.getNextToken();
            //     tokens.add(token);
            //     System.out.println(token);
            // } while (token.getType() != TokenType.EOF);

            tokenList = Lexer.Tokenize(lexer);

            SyntaxAnalyzer syntaxAnalyzer = new SyntaxAnalyzer(tokenList);
            syntaxAnalyzer.parse();

        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + filename);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        
    }

}
