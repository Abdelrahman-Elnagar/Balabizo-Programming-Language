// Each grammar rule becomes a method inside this new class

package src.com.craftinginterpreters.Lox;

import java.util.ArrayList;
import java.util.List;

import static src.com.craftinginterpreters.Lox.TokenType.*;

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
      statements.add(statement());
    }

    return statements; 
  }

  private Expr expression() {
    return equality();
  }
  private Stmt statement() {
    if (match(PRINT)) return printStatement();

    return expressionStatement();
  }
  private Stmt printStatement() {
    Expr value = expression();
    consume(SEMICOLON, "Balabizo, Expect ';' after value.");
    return new Stmt.Print(value);
  }
  private Stmt expressionStatement() {
    Expr expr = expression();
    consume(SEMICOLON, "Expect ';' after expression.");
    return new Stmt.Expression(expr);
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

    return primary();
  }

  //Most of the cases for the rule are single terminals, so parsing is straightforward.
  private Expr primary() {
    if (match(FALSE)) return new Expr.Literal(false);
    if (match(TRUE)) return new Expr.Literal(true);
    if (match(NIL)) return new Expr.Literal(null);

    if (match(NUMBER, STRING)) {
      return new Expr.Literal(previous().literal);
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