package src.com.craftinginterpreters.Balabizo;
// Testing class to see ann intermideate procress
/*
package src.com.craftinginterpreters.Lox;

class AstPrinter implements Expr.Visitor<String> {
  String print(Expr expr) {
    return expr.accept(this);
  }


  @Override
  public String visitBinaryExpr(Expr.Binary expr) {
    return parenthesize(expr.operator.lexeme,
                        expr.left, expr.right);
  }

  @Override
  public String visitGroupingExpr(Expr.Grouping expr) {
    return parenthesize("group", expr.expression);
  }

  @Override
  public String visitLiteralExpr(Expr.Literal expr) {
    if (expr.value == null) return "nil";
    return expr.value.toString();
  }

  @Override
  public String visitUnaryExpr(Expr.Unary expr) {
    return parenthesize(expr.operator.lexeme, expr.right);
  }

  private String parenthesize(String name, Expr... exprs) {
    StringBuilder builder = new StringBuilder();

    builder.append("(").append(name);
    for (Expr expr : exprs) {
      builder.append(" ");
      builder.append(expr.accept(this)); //call accept() on each subexpression and passes in itself. This is the recursive step that lets us print an entire tree.
    }
    builder.append(")");

    return builder.toString();
  }

  // test function only : 
  public static void main(String[] args) {
    Expr[] expressions = new Expr[] {
      new Expr.Binary(
          new Expr.Unary(
              new Token(TokenType.MINUS, "-", null, 1),
              new Expr.Literal(123)),
          new Token(TokenType.STAR, "*", null, 1),
          new Expr.Grouping(
              new Expr.Literal(45.67))),
      
      new Expr.Binary(
          new Expr.Literal(10),
          new Token(TokenType.PLUS, "+", null, 1),
          new Expr.Binary(
              new Expr.Literal(5),
              new Token(TokenType.STAR, "*", null, 1),
              new Expr.Literal(2)
          )
      ),
      
      new Expr.Binary(
          new Expr.Literal(20),
          new Token(TokenType.MINUS, "-", null, 1),
          new Expr.Binary(
              new Expr.Literal(10),
              new Token(TokenType.SLASH, "/", null, 1),
              new Expr.Literal(2)
          )
      ),
      
      new Expr.Binary(
          new Expr.Grouping(
              new Expr.Binary(
                  new Expr.Literal(5),
                  new Token(TokenType.PLUS, "+", null, 1),
                  new Expr.Literal(3)
              )
          ),
          new Token(TokenType.STAR, "*", null, 1),
          new Expr.Grouping(
              new Expr.Binary(
                  new Expr.Literal(6),
                  new Token(TokenType.MINUS, "-", null, 1),
                  new Expr.Literal(2)
              )
          )
      )
  };
  AstPrinter printer = new AstPrinter();
  
  for (Expr expression : expressions) {
      System.out.println(printer.print(expression));
  }

  }
}*/