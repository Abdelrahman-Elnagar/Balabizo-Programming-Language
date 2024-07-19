# the Grammer :
expression    → literal
               | unary
               | binary
               | grouping ;

literal       → NUMBER | STRING | "true" | "false" | "nil" ;
grouping      → "(" expression ")" ;
unary         → ( "-" | "!" ) expression ;
binary        → expression operator expression ;
operator      → "==" | "!=" | "<" | "<="
               | ">" | ">="
               | "+"  | "-"  | "*" | "/" ;

equality       → comparison ( ( "!=" | "==" ) comparison )* ;
comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term           → factor ( ( "-" | "+" ) factor )* ;
factor         → unary ( ( "/" | "*" ) unary )* ;
unary          → ( "!" | "-" ) unary
               | primary ;
primary        → "true" | "false" | "nil"
               | NUMBER | STRING
               | "(" expression ")"
               | IDENTIFIER ;

program        → declaration* EOF ;
declaration    → varDecl
               | statement ;
varDecl        → "var" IDENTIFIER ( "=" expression )? ";" ;
statement      → exprStmt
               | printStmt
               | block ;

block          → "{" declaration* "}" ;
exprStmt       → expression ";" ;
printStmt      → "print" expression ";" ;
expression     → assignment ;
assignment     → IDENTIFIER "=" assignment
               | equality ;
