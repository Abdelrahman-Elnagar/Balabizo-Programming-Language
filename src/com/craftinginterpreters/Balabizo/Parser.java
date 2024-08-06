// Each grammar rule becomes a method inside this new class

package src.com.craftinginterpreters.Balabizo;
import java.util.Arrays;

import static src.com.craftinginterpreters.Balabizo.TokenType.*;

import java.util.ArrayList;
import java.util.List;

class Parser {
  // prof Slim said to delay the error to the end to the last place to handle it so whst are we doiing handling here ?
  // because we want to let the calling method inside the parser decide whether to unwind or not  
  private static class ParseError extends RuntimeException {}
  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  // to start the whole thing 
  /*Expr parse() {
    try {
      return expression();
    } catch (ParseError error) {
      return null;
    }
  }*/
  List<Stmt> parse() {
    List<Stmt> statements = new ArrayList<>();
    while (!isAtEnd()) {
      statements.add(declaration());
    }

    return statements; 
  }

  private Expr expression() {
    return assignment();
  }
  private Expr assignment() {
    Expr expr = or();

    if (match(EQUAL)) {
      Token equals = previous();
      Expr value = assignment();

      if (expr instanceof Expr.Variable) {
        Token name = ((Expr.Variable)expr).name;
        return new Expr.Assign(name, value);
      }

      error(equals, "Balabizo , Invalid assignment target."); 
    }

    return expr;
  }
  private Expr or() {
    Expr expr = and();

    while (match(OR)) {
      Token operator = previous();
      Expr right = and();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }
  private Expr and() {
    Expr expr = equality();

    while (match(AND)) {
      Token operator = previous();
      Expr right = equality();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }
  private Stmt declaration() { //when parsing a series of statements in a block or a script , right place to synchronize when the parser goes into panic mode.
    try {
      if (match(FUN)) return function("function");
      if (match(VAR)) return varDeclaration();

      return statement();
    } catch (ParseError error) {
      synchronize();
      return null;
    }
  }
  private Stmt statement() {
    if (match(FOR)) return forStatement();
    if (match(IF)) return ifStatement();
    if (match(PRINT)) return printStatement();
    if (match(RETURN)) return returnStatement();
    if (match(WHILE)) return whileStatement();
    if (match(LEFT_BRACE)) return new Stmt.Block(block());
    return expressionStatement();
  }
  private Stmt forStatement() {
    consume(LEFT_PAREN, "Balabizo, Expect '(' after 'for'.");
    Stmt initializer;
    if (match(SEMICOLON)) {
      initializer = null;
    } else if (match(VAR)) {
      initializer = varDeclaration();
    } else {
      initializer = expressionStatement();
    }
    
    Expr condition = null;
    if (!check(SEMICOLON)) {
      condition = expression();
    }
    consume(SEMICOLON, "Balabizo, Expect ';' after loop condition.");
    Expr increment = null;
    if (!check(RIGHT_PAREN)) {
      increment = expression();
    }
    consume(RIGHT_PAREN, "Balabizo, Expect ')' after for clauses.");
    Stmt body = statement();
    if (increment != null) {
      body = new Stmt.Block(
          Arrays.asList(
              body,
              new Stmt.Expression(increment)));
    }
    if (condition == null) condition = new Expr.Literal(true);
    body = new Stmt.While(condition, body);
    if (initializer != null) {
      body = new Stmt.Block(Arrays.asList(initializer, body));
    }

    return body;
  }
  private Stmt whileStatement() {
    consume(LEFT_PAREN, "Balabizo, Expect '(' after 'while'.");
    Expr condition = expression();
    consume(RIGHT_PAREN, "Balabizo, Expect ')' after condition.");
    Stmt body = statement();

    return new Stmt.While(condition, body);
  }
  private Stmt ifStatement() {
    consume(LEFT_PAREN, "Balabizo, Expect '(' after 'if'.");
    Expr condition = expression();
    consume(RIGHT_PAREN, "Balabizo, Expect ')' after if condition."); 

    Stmt thenBranch = statement();
    Stmt elseBranch = null;
    if (match(ELSE)) {
      elseBranch = statement();
    }

    return new Stmt.If(condition, thenBranch, elseBranch);
  }
  private List<Stmt> block() {
    List<Stmt> statements = new ArrayList<>();

    while (!check(RIGHT_BRACE) && !isAtEnd()) {
      statements.add(declaration());
    }

    consume(RIGHT_BRACE, "Balabizo , Expect '}' after block.");
    return statements;
  }
  private Stmt printStatement() {
    Expr value = expression();
    consume(SEMICOLON, "Balabizo, Expect ';' after value.");
    return new Stmt.Print(value);
  }
  private Stmt returnStatement() {
    Token keyword = previous();
    Expr value = null;
    if (!check(SEMICOLON)) {
      value = expression();
    }

    consume(SEMICOLON, "Balabizo, Expect ';' after return value.");
    return new Stmt.Return(keyword, value);
  }
  private Stmt varDeclaration() {
    Token name = consume(IDENTIFIER, "Balabizo, Expect variable name.");

    Expr initializer = null;
    if (match(EQUAL)) {
      initializer = expression();
    }
    //to be modified 
    else{
      consume(IDENTIFIER, "Balabizo, Must be intialized");
    }

    consume(SEMICOLON, "Balabizo, Expect ';' after variable declaration.");
    return new Stmt.Var(name, initializer);
  }
  private Stmt expressionStatement() {
    Expr expr = expression();
    consume(SEMICOLON, "Expect ';' after expression.");
    return new Stmt.Expression(expr);
  }

  private Stmt.Function function(String kind) {
    Token name = consume(IDENTIFIER, "Balabizo, Expect " + kind + " name."); //kind of declaration being parsed.
    consume(LEFT_PAREN, "Balabizo, Expect '(' after " + kind + " name.");
    List<Token> parameters = new ArrayList<>();
    if (!check(RIGHT_PAREN)) { //handles the zero parameter case
      do {
        if (parameters.size() >= 255) {
          error(peek(), "Balabizo Number, Can't have more than 255 parameters. But Wow if you reached here");
        }

        parameters.add(
            consume(IDENTIFIER, " Balabizo, Expect parameter name."));
      } while (match(COMMA));
    }
    consume(RIGHT_PAREN, "Balabizo, Expect ')' after parameters.");
    consume(LEFT_BRACE, "Balabizo, Expect '{' before " + kind + " body.");
    List<Stmt> body = block(); //block() assumes the brace token has already been matched.
    return new Stmt.Function(name, parameters, body);
  }

  //creates a left-associative nested tree of binary operator nodes.
  private Expr equality() { 
    Expr expr = comparison(); // first comparison nonterminal in the body translates to the first call to comparison() in the method. We take that result and store it in a local variable.

    while (match(BANG_EQUAL, EQUAL_EQUAL)) {
      Token operator = previous();
      Expr right = comparison();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private boolean match(TokenType... types) { // checks to see if the current token has any of the given types. If so, it consumes the token and returns true
    for (TokenType type : types) {
      if (check(type)) {
        advance();
        return true;
      }
    }

    return false;
  }

  // looks for the closing )
  private Token consume(TokenType type, String message) {
    if (check(type)) return advance();

    throw error(peek(), message);
  }
  
  //The check() method returns true if the current token is of the given type. Unlike match(), it never consumes the token, it only looks at it.
  private boolean check(TokenType type) {
    if (isAtEnd()) return false;
    return peek().type == type;
  }

  // advance() method consumes the current token and returns it
  private Token advance() {
    if (!isAtEnd()) current++;
    return previous();
  }

  // helpers :
  private boolean isAtEnd() {
    return peek().type == EOF;
  }

  private Token peek() {
    return tokens.get(current);
  }

  private Token previous() {
    return tokens.get(current - 1);
  }

  private ParseError error(Token token, String message) {
    Balabizo.error(token, message);
    return new ParseError();
  }

  
  /*
   * We want to discard tokens until we’re right at the beginning of the next statement. 
   * That boundary is pretty easy to spot—it’s one of the main reasons we picked it. 
   * After a semicolon, we’re probably finished with a statement. Most statements start with a keyword—for, if, return, var, etc.
   *  When the next token is any of those, we’re probably about to start a statement.
   */

   private void synchronize() {
    advance();

    while (!isAtEnd()) {
      if (previous().type == SEMICOLON) return;

      switch (peek().type) {
        case CLASS:
        case FUN:
        case VAR:
        case FOR:
        case IF:
        case WHILE:
        case PRINT:
        case RETURN:
          return;
      }

      advance();
    }
  }

  private Expr comparison() {
    Expr expr = term();

    while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
      Token operator = previous();
      Expr right = term();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr term() {
    Expr expr = factor();

    while (match(MINUS, PLUS)) {
      Token operator = previous();
      Expr right = factor();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr factor() {
    Expr expr = unary();

    while (match(SLASH, STAR)) {
      Token operator = previous();
      Expr right = unary();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr unary() {
    if (match(BANG, MINUS)) {
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    }

    return call();
  }
  private Expr call() {
    Expr expr = primary();

    while (true) { 
      if (match(LEFT_PAREN)) {
        expr = finishCall(expr);
      } else {
        break;
      }
    }

    return expr;
  }
  private Expr finishCall(Expr callee) {
    List<Expr> arguments = new ArrayList<>();
    if (!check(RIGHT_PAREN)) {
      do {
        if (arguments.size() >= 255) {
          error(peek(), "Can't have more than 255 arguments.");
        }
        arguments.add(expression());
      } while (match(COMMA));
    }

    Token paren = consume(RIGHT_PAREN,
                          "Balabizo, Expect ')' after arguments.");

    return new Expr.Call(callee, paren, arguments);
  }

  //Most of the cases for the rule are single terminals, so parsing is straightforward.
  private Expr primary() {
    if (match(FALSE)) return new Expr.Literal(false);
    if (match(TRUE)) return new Expr.Literal(true);
    if (match(NIL)) return new Expr.Literal(null);

    if (match(NUMBER, STRING)) {
      return new Expr.Literal(previous().literal);
    }
    if (match(IDENTIFIER)) {
      return new Expr.Variable(previous());
    }
    if (match(LEFT_PAREN)) {
      Expr expr = expression();
      consume(RIGHT_PAREN, "Expect ')' after expression. Blabizo code ?? ");
      return new Expr.Grouping(expr);
    }
    throw error(peek(), "Expect expression. but you gave a Balabizo");
  }


}
/*
  Grammar notation	Code representation
  Terminal	        Code to match and consume a token
  Nonterminal	      Call to that rule’s function
  |	                if or switch statement
  * or +          	while or for loop
  ?	                if statement
 */