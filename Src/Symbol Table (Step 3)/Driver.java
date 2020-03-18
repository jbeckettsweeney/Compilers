import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.*;
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

        //Read from input (input is redirected from shell)
        while(input.hasNextLine()) {
            entire += input.nextLine() + "\n";
        }

        //Create lexer to get tokens, then parser full of those tokens
        GramLexer lexer = new GramLexer(CharStreams.fromString(entire));
        CommonTokenStream stream = new CommonTokenStream(lexer);
        GramParser parser = new GramParser(stream);

        //Add error handling
        parser.removeErrorListeners();
        parser.addErrorListener(ThrowingErrorListener.INSTANCE);

        //Create listener
        Listener listener = new Listener();

        //Accept or do not accept input
        try {
            new ParseTreeWalker().walk(listener, parser.program());
            listener.prettyPrint();
        } catch (ParseCancellationException e) {
            System.out.println("Not accepted");
        }
    }
}

class Listener extends GramBaseListener {
    //Type booleans
    private boolean str = false;
    private boolean var = false;
    private boolean fundec = false;
    private boolean par = false;

    //Temp variables
    private String tName = null;
    private String tType = null;
    private String tValue = null;

    //Block count variable
    private int count = 1;

    //Create and initialize data structure
    private ArrayList<ArrayList<Node>> outer = new ArrayList<>();
    private ArrayList<Node> inner = new ArrayList<>();
    public Listener() {
        inner.add(new Node("GLOBAL"));
    }

    //Function methods
    @Override
    public void enterFunc_declarations(GramParser.Func_declarationsContext ctx) {
        outer.add(inner);

        inner = new ArrayList<>();

        tName = null;
        tType = null;
        tValue = null;
    }
    @Override
    public void enterFunc_decl(GramParser.Func_declContext ctx) {
        fundec = true;
    }

    //Block methods
    @Override
    public void enterIf_stmt(GramParser.If_stmtContext ctx) {
        outer.add(inner);

        inner = new ArrayList<>();

        tName = null;
        tType = null;
        tValue = null;

        inner.add(new Node("BLOCK " + count));
        count++;
    }
    @Override
    public void enterElse_part(GramParser.Else_partContext ctx) {
        String t = ctx.getText();
        if(t.equals("") == false) {
            outer.add(inner);

            inner = new ArrayList<>();

            tName = null;
            tType = null;
            tValue = null;

            inner.add(new Node("BLOCK " + count));
            count++;
        }
    }
    @Override
    public void enterWhile_stmt(GramParser.While_stmtContext ctx) {
        outer.add(inner);

        inner = new ArrayList<>();

        tName = null;
        tType = null;
        tValue = null;

        inner.add(new Node("BLOCK " + count));
        count++;
    }

    //Parameter methods
    @Override
    public void enterParam_decl(GramParser.Param_declContext ctx) {
        par = true;
    }
    @Override
    public void exitParam_decl(GramParser.Param_declContext ctx) {
        tName = null;
        tType = null;
        tValue = null;
        par = false;
    }

    //String methods
    @Override
    public void enterString_decl(GramParser.String_declContext ctx) {
        str = true;
        tType = ctx.getStart().getText();
    }
    @Override
    public void exitString_decl(GramParser.String_declContext ctx) {
        inner.add(new Node(tName, tType, tValue));
        tName = null;
        tType = null;
        tValue = null;
        str = false;
    }
    @Override
    public void enterStr(GramParser.StrContext ctx) {
        tValue = ctx.getText();
    }

    //Var methods
    @Override
    public void enterVar_decl(GramParser.Var_declContext ctx) {
        var = true;
    }
    @Override
    public void exitVar_decl(GramParser.Var_declContext ctx) {
        tName = null;
        tType = null;
        tValue = null;
        var = false;
    }
    @Override
    public void enterVar_type(GramParser.Var_typeContext ctx) {
        tType = ctx.getText();
    }

    //ID check
    @Override
    public void enterId(GramParser.IdContext ctx) {
        if(str) {
            tName = ctx.getText();
        } else if(var) {
            tName = ctx.getText();
            inner.add(new Node(tName, tType));
        } else if(fundec) {
            inner.add(new Node(ctx.getText()));
            fundec = false;
        } else if(par) {
            tName = ctx.getText();
            inner.add(new Node(tName, tType));
        }
    }

    //Print each symbol table
    public void prettyPrint() {
        String result = dupe();
        if(result == null) {
            for(int i = 0; i < outer.size(); i++) {
                for(int j = 0; j < outer.get(i).size(); j++) {
                    outer.get(i).get(j).print();
                }
                System.out.println();
            }
        } else {
            System.out.println("DECLARATION ERROR " + result);
        }
    }

    //Checks for duplicates within each scope
    public String dupe() {
        String curType = null;
        String curName = null;
        for(int i = 0; i < outer.size(); i++) {
            for(int j = 0; j < outer.get(i).size(); j++) {
                curType = outer.get(i).get(j).type;
                curName = outer.get(i).get(j).name;
                for(int k = j+1; k < outer.get(i).size(); k++) {
                    if(curName != null && curType != null) {
                        if(curName.equals(outer.get(i).get(k).name)) {
                            if(curType.equals(outer.get(i).get(k).type)) {
                                return curName;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}

class Node {
    //Node class to hold necessary data
    public String title;
    public String name;
    public String type;
    public String value;
    public Node(String inName, String inType, String inValue) {
        title = null;
        name = inName;
        type = inType;
        value = inValue;
    }
    public Node(String inName, String inType) {
        title = null;
        name = inName;
        type = inType;
        value = null;
    }
    public Node(String inTitle) {
        title = inTitle;
        name = null;
        type = null;
        value = null;
    }
    public void print() {
        if(title != null){
            System.out.println("Symbol table " + title);
        } else {
            if(value != null) {
                System.out.println("name " + name + " type " + type + " value " + value);
            } else {
                System.out.println("name " + name + " type " + type);
            }
        }
    }
}

class ThrowingErrorListener extends BaseErrorListener {
    /*
    Exception handling reference: User Mouagip
    https://stackoverflow.com/questions/18132078/handling-errors-in-antlr4
    ANTLR is weird about errors and it doesn't throw them in the form of actual exceptions.
    This class turns ANTLR errors into exceptions so we can use a simple try-catch.
     */
    public static final ThrowingErrorListener INSTANCE = new ThrowingErrorListener();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e)
            throws ParseCancellationException {
        throw new ParseCancellationException("line " + line + ":" + charPositionInLine + " " + msg);
    }
}