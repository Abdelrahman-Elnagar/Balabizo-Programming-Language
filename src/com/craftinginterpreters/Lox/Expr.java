package src.com.craftinginterpreters.Lox;
// we will use nested java classes to put all in a single java file

abstract class Expr { 
  static class Binary extends Expr {
    final Expr left;
    final Token operator;
    final Expr right;

    Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    
  }

  // Other expressions...
}