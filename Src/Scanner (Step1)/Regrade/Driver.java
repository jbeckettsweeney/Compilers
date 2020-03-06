import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

import java.util.List;
import java.util.Scanner;

public class Driver {
    public static void main(String[] args){
        String entire = "";
        Scanner input = new Scanner(System.in);

        //input.useDelimiter(System.getProperty("line.separator"));

        while(input.hasNextLine()) {
            entire += input.nextLine() + "\n";
        }

        gLexer lexer = new gLexer(CharStreams.fromString(entire));

        List l = lexer.getAllTokens();
        int a = l.size();
        Token tok;
        int type;

        // 1 - 18   is KEYWORD
        // 19 - 33  is OPERATOR
        // 34       is IDENTIFIER
        // 35       is INTLITERAL
        // 36       is FLOATLITERAL
        // 37       is STRINGLITERAL
        // 38       is COMMENT

        for(int i = 0; i < a; i++){
            tok = (Token) l.get(i);
            type = tok.getType();

            if (type != 38) {   //skip comments
                System.out.println("Token Type: " + stringType(type));
                System.out.println("Value: " + tok.getText());
            }
        }

    }

    private static String stringType(int in) {
        if (in > 0 && in <= 18) {
            return "KEYWORD";
        } else if (in > 18 && in <= 33) {
            return "OPERATOR";
        } else if (in == 34) {
            return "IDENTIFIER";
        } else if (in == 35) {
            return "INTLITERAL";
        } else if (in == 36) {
            return "FLOATLITERAL";
        } else if (in == 37) {
            return "STRINGLITERAL";
        } else if (in == 38){
            return "COMMENT";
        } else {
            return "ERROR ERROR ERROR";
        }
    }
}
