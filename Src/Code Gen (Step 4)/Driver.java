import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.lang.reflect.Array;
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
        while (input.hasNextLine()) {
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
            //listener.prettyPrint();
            //listener.treePrint();
            listener.generateIR();
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

    //Temp variables
    private String tName = null;
    private String tType = null;
    private String tValue = null;

    //Create and initialize data structure
    private ArrayList<ArrayList<Node>> outer = new ArrayList<>();
    private ArrayList<Node> inner = new ArrayList<>();

    //Create AST structure
    private ArrayList<ASTNode> nodes = new ArrayList<>();
    private int currentAST = 0;
    private ASTNode currentNode;

    //AST booleans
    private boolean a = false;          //assign
    private boolean idAssigned = false;
    private boolean r = false;          //read
    private boolean w = false;          //write

    //CodeObject
    private ArrayList<ArrayList<IRNode>> allCode = new ArrayList<>();
    private ArrayList<IRNode> block = new ArrayList<>();

    //tiny variables
    private ArrayList<Node> variables= new ArrayList<>();

    //test variables
    private int unknowncount = 0;
    private String order = "";

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

        order += "enterFunc_declarations\n";
    }

    @Override
    public void enterFunc_decl(GramParser.Func_declContext ctx) {
        fundec = true;

        order += "enterFunc_decl\n";
    }

    //Statement methods
    @Override
    public void enterBase_stmt(GramParser.Base_stmtContext ctx) {
        //System.out.println("entering base statement");
        //System.out.println("\ttext: " + ctx.getText());

        order += "enterBase_stmt\n";
    }

    @Override
    public void exitBase_stmt(GramParser.Base_stmtContext ctx) {
        //System.out.println("exiting base statement");
        //System.out.println("\ttext: " + ctx.getText());
        currentAST++;

        order += "exitBase_stmt\n";
    }

    @Override
    public void enterAssign_stmt(GramParser.Assign_stmtContext ctx) {
        //System.out.println("assign statement");
        currentNode = new ASTNode("head", "assign");
        nodes.add(currentNode);
        a = true;
        idAssigned = false;

        order += "enterAssign_stmt\n";
    }

    @Override
    public void exitAssign_stmt(GramParser.Assign_stmtContext ctx) {
        a = false;
        idAssigned = false;

        order += "exitAssign_stmt\n";
    }

    @Override
    public void enterRead_stmt(GramParser.Read_stmtContext ctx) {
        //System.out.println("read statement");
        currentNode = new ASTNode("head", "read");
        nodes.add(currentNode);
        r = true;

        order += "enterRead_stmt\n";
    }

    @Override
    public void exitRead_stmt(GramParser.Read_stmtContext ctx) {
        r = false;

        order += "exitRead_stmt\n";
    }

    @Override
    public void enterWrite_stmt(GramParser.Write_stmtContext ctx) {
        //System.out.println("write statement");
        currentNode = new ASTNode("head", "write");
        nodes.add(currentNode);
        w = true;

        order += "enterWrite_stmt\n";
    }

    @Override
    public void exitWrite_stmt(GramParser.Write_stmtContext ctx) {
        w = false;

        order += "exitWrite_stmt\n";
    }

    //Expression methods
    @Override
    public void enterExpr(GramParser.ExprContext ctx) {
        //System.out.println("entering expression");
        //System.out.println("text: " + ctx.getText());
        if (a) {
            //System.out.println("\t\tassign expression");
        }

        order += "enterExpr " + ctx.getText() + "\n";
    }

    @Override
    public void exitExpr(GramParser.ExprContext ctx) {
        //System.out.println("exiting expression");
        //System.out.println("text: " + ctx.getText());

        order += "exitExpr " + ctx.getText() + "\n";
    }

    @Override
    public void enterExpr_prefix(GramParser.Expr_prefixContext ctx) {
        //System.out.println("expr_prefix: <" + ctx.getText() + ">");
        if (!ctx.getText().equals("")) {
            //System.out.println("---create new node");
            currentNode = currentNode.add("op", ("count"));
            unknowncount++;
        }

        order += "enterExpr_prefix " + ctx.getText() + "\n";
    }

    @Override
    public void exitExpr_prefix(GramParser.Expr_prefixContext ctx) {
        if (!ctx.getText().equals("")) {
            if (currentNode.parent != null) {
                if (currentNode.hasLeft() && currentNode.hasRight()) {
                    while (!currentNode.value.equals("count") && currentNode.parent != null) {
                        currentNode = currentNode.parent;
                        //System.out.println("CLIMBING UP");
                    }
                }
            }
        }

        order += "exitExpr_prefix " + ctx.getText() + "\n";
    }

    @Override
    public void enterFactor_prefix(GramParser.Factor_prefixContext ctx) {
        //System.out.println("factor_prefix: <" + ctx.getText() + ">");
        if (!ctx.getText().equals("")) {
            //System.out.println("---create new node");
            currentNode = currentNode.add("op", ("count"));
            unknowncount++;
        }

        order += "enterFactor_prefix " + ctx.getText() + "\n";

    }

    @Override
    public void exitFactor_prefix(GramParser.Factor_prefixContext ctx) {


        order += "exitFactor_prefix " + ctx.getText() + "\n";
    }

    @Override
    public void enterFactor(GramParser.FactorContext ctx) {
        /*//System.out.println("factor_prefix: <" + ctx.getText() + ">");
        if (!ctx.getText().equals("")) {
            //System.out.println("---create new node");
            currentNode = currentNode.add("op", ("count" + Integer.toString(unknowncount)));
            unknowncount++;
        }*/

        order += "enterFactor " + ctx.getText() + "\n";
    }

    @Override
    public void exitFactor(GramParser.FactorContext ctx) {
        if (!ctx.getText().equals(currentNode.left.value) && !ctx.getText().equals(currentNode.right.value)) {
            if (currentNode.parent != null) {
                if (currentNode.hasLeft() && currentNode.hasRight()) {
                    while (!currentNode.value.equals("count") && currentNode.parent != null) {
                        currentNode = currentNode.parent;
                        //System.out.println("CLIMBING UP");
                    }
                }
            }
        }

        order += "exitFactor " + ctx.getText() + "\n";
    }

    @Override
    public void enterPrimary(GramParser.PrimaryContext ctx) {
        try {
            int i = Integer.parseInt(ctx.getText());
            //System.out.println("******** found an int");
            //System.out.println(i + " + " + i + " = " + (i*2));
            currentNode = currentNode.add("lit", Integer.toString(i));
        } catch (Exception e) {
            try {
                float f = Float.parseFloat(ctx.getText());
                //System.out.println("******** found a float");
                //System.out.println(f + " + " + f + " = " + (f*2));
                currentNode = currentNode.add("lit", Float.toString(f));
            } catch (Exception ee) {
                //System.out.println("NEITHER INT NOR FLOAT");
            }
        }

        order += "enterPrimary\n";
    }

    //Op methods
    @Override
    public void enterAddop(GramParser.AddopContext ctx) {
        //System.out.println("\t\tadd flag: " + ctx.getText());
        while (!currentNode.value.equals("count") && currentNode.parent != null) {
            currentNode = currentNode.parent;
            //System.out.println("TEST CLIMBING UP");
        }
        //System.out.println("---setting " + currentNode.value + " to " + ctx.getText());
        currentNode.value = ctx.getText();

        order += "enterAddop\n";
    }

    @Override
    public void exitAddop(GramParser.AddopContext ctx) {
        //currentNode.value = ctx.getText();
        /*
        if (currentNode.parent != null) {
            currentNode = currentNode.parent;
            System.out.println("CLIMBING UP");
        }
        */

        order += "exitAddop\n";
    }


    @Override
    public void enterMulop(GramParser.MulopContext ctx) {
        //System.out.println("\t\tmul flag: " + ctx.getText());
        while (!currentNode.value.equals("count") && currentNode.parent != null) {
            currentNode = currentNode.parent;
            //System.out.println("TEST CLIMBING UP");
        }
        //System.out.println("---setting " + currentNode.value + " to " + ctx.getText());
        currentNode.value = ctx.getText();

        order += "enterMulop\n";
    }

    @Override
    public void exitMulop(GramParser.MulopContext ctx) {
        //currentNode.value = ctx.getText();
        /*
        if (currentNode.parent != null) {
            currentNode = currentNode.parent;
            System.out.println("CLIMBING UP");
        }
        */

        order += "exitMulop\n";
    }

    //Write methods
    @Override
    public void enterId_tail(GramParser.Id_tailContext ctx) {
        if (w) {
            if (!ctx.getText().equals("")) {
                currentNode = currentNode.add("op", "write");
            }
        } else if (r) {
            if (!ctx.getText().equals("")) {
                currentNode = currentNode.add("op", "read");
            }
        }
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

        //System.out.println("\t\tid flag: " + ctx.getText());

        if (str) {
            tName = ctx.getText();
        } else if (var) {
            tName = ctx.getText();
            inner.add(new Node(tName, tType));
        } else if (fundec) {
            inner.add(new Node(ctx.getText()));
            fundec = false;
        } else if (a || w || r) {
            //System.out.println("assign id: " + ctx.getText());
            /*
            if (!idAssigned) {
                currentNode.add("id", ctx.getText());
                idAssigned = true;
            }
            */
            currentNode.add("id", ctx.getText());
        }

        order += "enterId\n";
    }

    public void treePrint() {
        for (int i = 0; i < currentAST; i++) {
            System.out.println("new set");
            nodes.get(i).recPrint();
            System.out.println("-----------------------");
        }

        //System.out.println("ORDER ORDER ORDER");
        //System.out.println(order);
    }

    //fill IRNodes and generate tiny code for each IR line
    public void generateIR() {
        for (int i = 0; i < outer.size(); i++) {
            for (int j = 0; j < outer.get(i).size(); j++) {
                outer.get(i).get(j).print();
                if (outer.get(i).get(j).title == null) {
                    variables.add(outer.get(i).get(j));
                }
            }
        }
        for (int i = 0; i < currentAST; i++) {
            nodes.get(i).getVarType(variables);
            block = nodes.get(i).generate();
            for (int j = 0; j < block.size(); j++) {
                block.get(j).print();
                generateTiny(block.get(j));
            }
            System.out.println();
            allCode.add(block);
        }
    }

    //generate tiny code from single IRNode
    public void generateTiny(IRNode input) {
        //input.print();
        switch (input.opcode) {
            case "STORE": {
                System.out.println("move " + input.first + " " + input.result);
                break;
            }
            case "ADD": {
                if (input.type == 2) {
                    System.out.println("move " + input.first + " " + input.result);
                    System.out.println("addi " + input.second + " " + input.result);
                } else if (input.type == 3) {
                    System.out.println("move " + input.first + " " + input.result);
                    System.out.println("addr " + input.second + " " + input.result);
                } else {
                    System.out.println("~~~~~~~~~~ERROR~~~~~~~~~~");
                }
                break;
            }
            case "SUB": {
                if (input.type == 2) {
                    System.out.println("move " + input.first + " " + input.result);
                    System.out.println("subi " + input.second + " " + input.result);
                } else if (input.type == 3) {
                    System.out.println("move " + input.first + " " + input.result);
                    System.out.println("subr " + input.second + " " + input.result);
                } else {
                    System.out.println("~~~~~~~~~~ERROR~~~~~~~~~~");
                }
                break;
            }
            case "MUL": {
                if (input.type == 2) {
                    System.out.println("move " + input.first + " " + input.result);
                    System.out.println("muli " + input.second + " " + input.result);
                } else if (input.type == 3) {
                    System.out.println("move " + input.first + " " + input.result);
                    System.out.println("mulr " + input.second + " " + input.result);
                } else {
                    System.out.println("~~~~~~~~~~ERROR~~~~~~~~~~");
                }
                break;
            }
            case "DIV": {
                if (input.type == 2) {
                    System.out.println("move " + input.first + " " + input.result);
                    System.out.println("divi " + input.second + " " + input.result);
                } else if (input.type == 3) {
                    System.out.println("move " + input.first + " " + input.result);
                    System.out.println("divr " + input.second + " " + input.result);
                } else {
                    System.out.println("~~~~~~~~~~ERROR~~~~~~~~~~");
                }
                break;
            }
            case "WRITE": {
                if (input.type == 1) {
                    System.out.println("sys writes " + input.result);
                } else if (input.type == 2) {
                    System.out.println("sys writei " + input.result);
                } else if (input.type == 3) {
                    System.out.println("sys writer " + input.result);
                } else {
                    System.out.println("~~~~~~~~~~ERROR~~~~~~~~~~");
                }
                break;
            }
            case "READ": {
                if (input.type == 2) {
                    System.out.println("sys readi " + input.result);
                } else if (input.type == 3) {
                    System.out.println("sys readr " + input.result);
                } else {
                    System.out.println("~~~~~~~~~~ERROR~~~~~~~~~~");
                }
                break;
            }
            default: {
                System.out.println("ERROR");
                break;
            }
        }
    }

    //Print each symbol table
    public void prettyPrint() {
        System.out.println();
        String result = dupe();
        if (result == null) {
            for (int i = 0; i < outer.size(); i++) {
                for (int j = 0; j < outer.get(i).size(); j++) {
                    outer.get(i).get(j).print();
                }
                System.out.println();
            }
        } else {
            System.out.println("DECLARATION ERROR " + result);
        }
        System.out.println("number of nodes: " + currentAST);
    }

    //Checks for duplicates within each scope
    public String dupe() {
        String curType = null;
        String curName = null;
        for (int i = 0; i < outer.size(); i++) {
            for (int j = 0; j < outer.get(i).size(); j++) {
                curType = outer.get(i).get(j).type;
                curName = outer.get(i).get(j).name;
                for (int k = j + 1; k < outer.get(i).size(); k++) {
                    if (curName != null && curType != null) {
                        if (curName.equals(outer.get(i).get(k).name)) {
                            if (curType.equals(outer.get(i).get(k).type)) {
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

class IRNode {
    public String opcode;
    public String first;
    public String second;
    public String result;
    public int type;        // 1 = str | 2 = int | 3 = float

    public IRNode(String inOp, String inF, String inS, String inRes, String inVar) {
        opcode = inOp;
        first = inF;
        second = inS;
        result = inRes;
        //print();
        switch (inVar) {
            case "STRING": {
                type = 1;
                break;
            }
            case "INT": {
                type = 2;
                break;
            }
            case "FLOAT": {
                type = 3;
                break;
            }
            default: {
                type = 0;
                System.out.println("ERROR ERROR ERROR ERROR ERROR ERROR");
                break;
            }
        }
    }

    public void print() {
        System.out.print("\t;" + opcode + " ");
        if (first != null) {
            System.out.print(first + " ");
        }
        if (second != null) {
            System.out.print(second + " ");
        }
        if (result != null) {
            System.out.print(result + " ");
        }
        System.out.println();
    }
}

class ASTNode {
    //AST variables
    public String type;
    public String value;
    public ASTNode parent;
    public ASTNode left;
    public ASTNode right;

    //IR variables
    public static int tempCount = 0;
    public int coTemp;                  // current temp
    public int coType;                  // 0 = root | 1 = L-Val | 2 = R-Val
    public ArrayList<IRNode> coBlock;   // code block
    public String varType;

    public ASTNode(String inType, String inValue) {
        type = inType;
        value = inValue;
        if (type.equals("head")) {
            parent = null;
            coTemp = -1;
            //coTemp = tempCount;
            //tempCount++;
            coType = 0;
        } else if (type.equals("id") || type.equals("lit")) {
            coTemp = -1;
            coType = 1;
        } else if (type.equals("op")) {
            coTemp = -1;
            //coTemp = tempCount;
            //tempCount++;
            coType = 2;
        } else {
            coTemp = -1;
            coType = -1;
            System.out.println("ERROR ERROR ERROR - coType");
        }
        //System.out.println("IR -> | te = " + coTemp + " | ty = " + coType);
    }

    public String getVarType(ArrayList<Node> v) {
        //System.out.println("my current value is " + value);
        String leftVar = "";
        String rightVar = "";
        String myVar = "";
        if (hasLeft()) {
            leftVar = left.getVarType(v);
        } else {
            leftVar = "";
        }
        if (hasRight()) {
            rightVar = right.getVarType(v);
        } else {
            rightVar = "";
        }
        if (!hasLeft()) {
            try {
                int i = Integer.parseInt(value);
                myVar = "INT";
            } catch (Exception e) {
                try {
                    float f = Float.parseFloat(value);
                    myVar = "FLOAT";
                } catch (Exception ee) {
                    for (int i = 0; i < v.size(); i++) {
                        //v.get(i).print();
                        if (v.get(i).name.equals(value)) {
                            myVar = v.get(i).type;
                        }
                    }
                }
            }
        } else {
            myVar = leftVar;
        }
        //System.out.println(type + value + " = " + myVar);
        varType = myVar;
        return myVar;
    }

    public ArrayList<IRNode> generate() {
        ArrayList<IRNode> myChunk = new ArrayList<>();
        ArrayList<IRNode> leftChunk;
        ArrayList<IRNode> rightChunk;

        //recursively get post-order lists and add to current code block
        if (hasLeft()) {
            leftChunk = left.generate();
            for (int i = 0; i < leftChunk.size(); i++) {
                myChunk.add(leftChunk.get(i));
            }
        }
        if (hasRight()) {
            rightChunk = right.generate();
            for (int i = 0; i < rightChunk.size(); i++) {
                myChunk.add(rightChunk.get(i));
            }
        }

        //create current code block
        if (type.equals("head")) {
            coType = 0;
            if (value.equals("assign")) {
                if (right.coType == 1) {
                    right.coTemp = getNewTemp();
                    myChunk.add(new IRNode("STORE", right.value, null, ("r" + right.coTemp), varType));
                }
                myChunk.add(new IRNode("STORE", ("r" + right.coTemp), null,  left.value, varType));
            } else if (value.equals("write")) {
                myChunk.add(0, new IRNode("WRITE", null, null, left.value, varType));
            } else if (value.equals("read")) {
                myChunk.add(0, new IRNode("READ", null, null, left.value, varType));
            }
        } else if (type.equals("op")) {
            coTemp = getNewTemp();
            coType = 2;
            if (value.equals("+")) {
                if (left.coType == 1) {
                    left.coTemp = getNewTemp();
                    myChunk.add(new IRNode("STORE", left.value, null, ("r" + left.coTemp), varType));
                }
                if (right.coType == 1) {
                    right.coTemp = getNewTemp();
                    myChunk.add(new IRNode("STORE", right.value, null, ("r" + right.coTemp), varType));
                }
                myChunk.add(new IRNode("ADD", ("r" + left.coTemp), ("r" + right.coTemp), ("r" + coTemp), varType));
            } else if (value.equals("-")) {
                if (left.coType == 1) {
                    left.coTemp = getNewTemp();
                    myChunk.add(new IRNode("STORE", left.value, null, ("r" + left.coTemp), varType));
                }
                if (right.coType == 1) {
                    right.coTemp = getNewTemp();
                    myChunk.add(new IRNode("STORE", right.value, null, ("r" + right.coTemp), varType));
                }
                myChunk.add(new IRNode("SUB", ("r" + left.coTemp), ("r" + right.coTemp), ("r" + coTemp), varType));
            } else if (value.equals("*")) {
                if (left.coType == 1) {
                    left.coTemp = getNewTemp();
                    myChunk.add(new IRNode("STORE", left.value, null, ("r" + left.coTemp), varType));
                }
                if (right.coType == 1) {
                    right.coTemp = getNewTemp();
                    myChunk.add(new IRNode("STORE", right.value, null, ("r" + right.coTemp), varType));
                }
                myChunk.add(new IRNode("MUL", ("r" + left.coTemp), ("r" + right.coTemp), ("r" + coTemp), varType));
            } else if (value.equals("/")) {
                if (left.coType == 1) {
                    left.coTemp = getNewTemp();
                    myChunk.add(new IRNode("STORE", left.value, null, ("r" + left.coTemp), varType));
                }
                if (right.coType == 1) {
                    right.coTemp = getNewTemp();
                    myChunk.add(new IRNode("STORE", right.value, null, ("r" + right.coTemp), varType));
                }
                myChunk.add(new IRNode("DIV", ("r" + left.coTemp), ("r" + right.coTemp), ("r" + coTemp), varType));
            } else if (value.equals("write")) {
                myChunk.add(0, new IRNode("WRITE", null, null, left.value, varType));
            } else if (value.equals("read")) {
                myChunk.add(0, new IRNode("READ", null, null, left.value, varType));
            } else {
                System.out.println("ERROR ERROR ERROR - op");
            }

        } else if (type.equals("id") || type.equals("lit")) {
            //do nothing
        } else {
            //error
        }
        return myChunk;
    }

    public int getNewTemp() {
        int temp = tempCount;
        tempCount++;
        return temp;
    }

    public ASTNode add(String t, String v) {
        if (!hasLeft()) {
            return addLeft(t, v);
        } /*else if (!hasRight()) {
            return addRight(t, v);
        }*/ else {
            return addRight(t, v);
        }
        //return upCheck();
    }

    public ASTNode addLeft(String t, String v) {
        left = new ASTNode(t, v);
        left.parent = this;
        if (t.equals("op")) {
            //System.out.println("\t\taddleft," + t + v + "//returnleft");
            //System.out.println("CLIMBING DOWN LEFT");
            return left;

        } else {
            //System.out.println("\t\taddleft," + t + v + "//returnTHIS");
            return this;
        }
        //return left;
    }

    public ASTNode addRight(String t, String v) {
        right = new ASTNode(t, v);
        right.parent = this;
        if (t.equals("op")) {
            //System.out.println("\t\taddright," + t + v + "//returnright");
            //System.out.println("CLIMBING DOWN RIGHT");
            return right;
        } else {
            //System.out.println("\t\taddright," + t + v + "//returnTHIS");
            return this;
        }
        //return right;
    }

    public boolean hasLeft() {
        if (left == null) {
            return false;
        } else {
            return true;
        }
    }

    public boolean hasRight() {
        if (right == null) {
            return false;
        } else {
            return true;
        }
    }

    public void setType(String inType) {
        type = inType;
    }

    public void setValue(String inValue) {
        value = inValue;
    }

    public void print() {
        System.out.println(type + " : " + value);
        /*
        if (hasLeft()) {
            System.out.println("l = " + left.type + " : " + left.value);
        } else {
            System.out.println("l = null");
        }
        if (hasRight()) {
            System.out.println("r = " + right.type + " : " + right.value);
        } else {
            System.out.println("r = null");
        }
        */
        //System.out.println();
    }

    public void recPrint() {
        if (left != null) {
            left.recPrint();
        }
        if (right != null) {
            right.recPrint();
        }
        print();
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
        if (title != null) {
            //System.out.println("Symbol table " + title);
        } else {
            if (value != null) {
                //System.out.println("name " + name + " type " + type + " value " + value);
                System.out.println("str " + name + " " + value);
            } else {
                //System.out.println("name " + name + " type " + type);
                System.out.println("var " + name);
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