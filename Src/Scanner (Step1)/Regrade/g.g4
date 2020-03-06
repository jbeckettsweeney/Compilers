grammar g;

//Keywords
PROGRAM:        'PROGRAM';
BEGIN:          'BEGIN';
END:            'END';
FUNCTION:       'FUNCTION';
READ:           'READ';
WRITE:          'WRITE';
IF:             'IF';
ELSE:           'ELSE';
ENDIF:          'ENDIF';
WHILE:          'WHILE';
ENDWHILE:       'ENDWHILE';
CONTINUE:       'CONTINUE';
BREAK:          'BREAK';
RETURN:         'RETURN';
INT:            'INT';
VOID:           'VOID';
STRING:         'STRING';
FLOAT:          'FLOAT';

//Operators
ASSIGN:         ':=';
ADD:            '+';
SUB:            '-';
MULT:           '*';
DIV:            '/';
EQUAL:          '=';
NOTEQUAL:       '!=';
LT:             '<';
GT:             '>';
LPAREN:         '(';
RPAREN:         ')';
SEMI:           ';';
COMMA:          ',';
LE:             '<=';
GE:             '>=';

//Tokens
IDENTIFIER:         Letter LetterOrDigit*;
INTLITERAL:         Digit+;
FLOATLITERAL:       Digit+ '.' Digit+
            |       '.' Digit+
            ;
STRINGLITERAL:      '"' .+? '"';     //uncertain
COMMENT:            '--' ~( '\r' | '\n' )* -> skip;   //uncertain

WS : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines

//Fragments
fragment Letter:            [a-zA-Z];
fragment Digit:             [0-9];
fragment LetterOrDigit:     Letter | Digit;


//Program

program:        PROGRAM id BEGIN pgm_body END;
id:             IDENTIFIER;
pgm_body:       decl func_declarations;
decl:           string_decl decl | var_decl decl | empty;

//Global String Declaration
string_decl:    STRING id ASSIGN str SEMI;
str:            STRINGLITERAL;

//Variable Declaration
var_decl:       var_type id_list SEMI;
var_type:       FLOAT | INT;
any_type:       var_type | VOID;
id_list:        id id_tail;
id_tail:        COMMA id id_tail | empty;

//Function Parameter List
param_decl_list:    param_decl param_decl_tail | empty;
param_decl:         var_type id;
param_decl_tail:    COMMA param_decl param_decl_tail | empty;

//Function Declarations
func_declarations:  func_decl func_declarations | empty;
func_decl:          FUNCTION any_type id LPAREN param_decl_list RPAREN BEGIN func_body END; //uncertain
func_body:          decl stmt_list;

//Statement List
stmt_list:      stmt stmt_list | empty;
stmt:           base_stmt | if_stmt | while_stmt;
base_stmt:      assign_stmt | read_stmt | write_stmt | return_stmt;

// Basic Statements
assign_stmt:    assign_expr SEMI;
assign_expr:    id ASSIGN expr;
read_stmt:      READ LPAREN id_list RPAREN SEMI;
write_stmt:     WRITE LPAREN id_list RPAREN SEMI;
return_stmt:    RETURN expr SEMI;
comment_stmt:   COMMENT;

// Expressions
expr:               expr_prefix factor;
expr_prefix:        expr_prefix factor addop | empty;
factor:             factor_prefix postfix_expr;
factor_prefix:      factor_prefix postfix_expr mulop | empty;
postfix_expr:       primary | call_expr;
call_expr:          id LPAREN expr_list RPAREN;
expr_list:          expr expr_list_tail | empty;
expr_list_tail:     COMMA expr expr_list_tail | empty;
primary:            LPAREN expr RPAREN | id | INTLITERAL | FLOATLITERAL;
addop:              ADD | SUB;
mulop:              MULT | DIV;

// Complex Statements and Condition
if_stmt:        IF LPAREN cond RPAREN decl stmt_list else_part ENDIF;
else_part:      ELSE decl stmt_list | empty;
cond:           expr compop expr;
compop:         LT | GT | EQUAL | NOTEQUAL | LE | GE;

// While Statements
while_stmt:     WHILE LPAREN cond RPAREN decl stmt_list ENDWHILE;

//Empty
empty:      /*empty*/;
