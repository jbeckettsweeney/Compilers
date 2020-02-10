import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.atn.ATN;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
//reference: https://howtodoinjava.com/java/io/java-read-file-to-string-examples/

public class Driver {
    public static void main(String[] args){
        String filePath = "C:/Users/Beckett/IdeaProjects/Compiler/src/fibonacci.micro";

        //System.out.println( readAllBytesJava7( filePath ) );

        String file = readAllBytesJava7(filePath);


        System.out.println("hello");
        gLexer lexer = new gLexer(CharStreams.fromString(file));
        TokenFactory name = lexer.getTokenFactory();

        ATN atn = lexer.getATN();

        List l = lexer.getAllTokens();
        int a = l.size();

        Token tok;

        for(int i = 0; i < a; i++){
            tok = (Token) l.get(i);
            System.out.println(tok.getText());
            System.out.println(tok.getType());
        }

        /*
        tok = lexer.nextToken();
        System.out.println(tok.getText());
        System.out.println(tok.getType());

        tok = lexer.nextToken();
        System.out.println(tok.getText());
        System.out.println(tok.getType());

        tok = lexer.nextToken();
        System.out.println(tok.getText());
        System.out.println(tok.getType());
        */
    }
    private static String readAllBytesJava7(String filePath)
    {
        String content = "";

        try
        {
            content = new String ( Files.readAllBytes( Paths.get(filePath) ) );
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return content;
    }
}
