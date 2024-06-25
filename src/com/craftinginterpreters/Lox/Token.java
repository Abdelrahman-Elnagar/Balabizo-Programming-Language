package src.com.craftinginterpreters.Lox;
class Token {
  final TokenType type;
  final String lexeme;
  final Object literal;
  final int line; 

  Token(TokenType type, String lexeme, Object literal, int line) {
    this.type = type;
    this.lexeme = lexeme;
    this.literal = literal;
    this.line = line;
  }

  public String toString() {
    return type + " " + lexeme + " " + literal;
  }
}

/*
 Most tokens never appear in an error message. For those, the less time you spend calculating position information ahead of time, the better.
 */