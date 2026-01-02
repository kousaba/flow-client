grammar FlowScript;

@header {
    package net.flowclient.script.parser;
}

script
    : functionDecl* EOF
    ;

functionDecl
    : FN ID '(' paramList? ')' block
    ;

paramList
    : ID (',' ID)*
    ;

block
    : '{' statement* '}'
    ;

statement
    : varDecl
    | assignment
    | ifStmt
    | whileStmt
    | forStmt
    | returnStmt
    | expressionStmt // 式文(関数呼び出しなど)
    | block
    ;

varDecl
    : LET ID ('=' expression)? ';'
    ;

assignment
    : ID op=('=' | '+=' | '-=' | '*=' | '/=' | '%=') expression ';'
    ;

ifStmt
    : IF expression statement (ELSE statement)?
    ;

whileStmt
    : WHILE expression statement
    ;

forStmt
    : FOR forInit? ';' expression? ';' forUpdate? statement
    ;

forInit
    : LET ID '=' expression
    | ID '=' expression
    ;

forUpdate
    : ID op=('=' | '+=' | '-=' | '*=' | '/=') expression
    ;

returnStmt
    : RETURN expression? ';'
    ;

expressionStmt
    : expression ';'
    ;

expression
    : '(' expression ')' # ParenExpr
    | expression '[' expression ']' # IndexExpr
    | <assoc=right> expression '^' expression # PowerExpr
    | op=('-' | '!') expression # UnaryExpr
    | expression op=('*' | '/' | '%') expression # MultiplicativeExpr
    | expression op=('+' | '-') expression # AdditiveExpr
    | expression op=('==' | '!=' | '<' | '>' | '<=' | '>=') expression # RelationalExpr
    | expression '&&' expression # LogicAndExpr
    | expression '||' expression # LogicOrExpr
    | ID '(' exprList? ')' # FunctionCallExpr
    | '[' exprList? ']' # ListExpr
    | atom # AtomExpr
    ;

exprList
    : expression (',' expression)*
    ;

atom
    : ID
    | NUMBER
    | STRING
    | COLOR_LITERAL
    | TRUE
    | FALSE
    | NULL
    ;

FN: 'fn';
LET: 'let';
IF: 'if';
ELSE: 'else';
WHILE: 'while';
FOR: 'for';
RETURN: 'return';
TRUE: 'true';
FALSE: 'false';
NULL: 'null';

COLOR_LITERAL
    : '#' HEX HEX HEX HEX HEX HEX (HEX HEX)?
    ;
fragment HEX: [0-9a-fA-F];
NUMBER: [0-9]+('.' [0-9]+)?;
STRING: '"' .*? '"';
ID: [a-zA-Z_][a-zA-Z0-9_]*;

COMMENT
    : '//' ~[\r\n]* -> skip
    ;
BLOCK_COMMENT
    : '/*' .*? '*/' -> skip
    ;
WS
    : [ \t\r\n] -> skip
    ;