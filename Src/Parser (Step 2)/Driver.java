import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.Scanner;

/*
References:
https://stackoverflow.com/questions/18132078/handling-errors-in-antlr4
https://www.programcreek.com/java-api-examples/?api=org.antlr.v4.runtime.CommonTokenStream
 */

public class Driver {
    public static void main(String[] args) throws Exception {

        //Prepare for input
        String entire = "";
        Scanner input = new Scanner(System.in);

        //Add newline between each line for proper formatting
        input.useDelimiter(System.getProperty("line.separator"));

        //Read from input (input is redirected from shell)
        while(input.hasNext()) {
            entire += input.next();
        }

        //Create lexer to get tokens, then parser full of those tokens
        GramLexer lexer = new GramLexer(CharStreams.fromString(entire));
        CommonTokenStream stream = new CommonTokenStream(lexer);
        GramParser parser = new GramParser(stream);

        //Add error handling
        parser.removeErrorListeners();
        parser.addErrorListener(ThrowingErrorListener.INSTANCE);

        //Accept or do not accept input
        try {
            parser.program();
            System.out.println("Accepted");
        } catch (Exception e) {
            System.out.println("Not accepted");
        }

    }
}

/*
Exception handling reference: User Mouagip
https://stackoverflow.com/questions/18132078/handling-errors-in-antlr4

ANTLR is weird about errors and it doesn't throw them in the form of actual exceptions.
This class turns ANTLR errors into exceptions so we can use a simple try-catch.
 */
class ThrowingErrorListener extends BaseErrorListener {

    public static final ThrowingErrorListener INSTANCE = new ThrowingErrorListener();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e)
            throws ParseCancellationException {
        throw new ParseCancellationException("line " + line + ":" + charPositionInLine + " " + msg);
    }
}
