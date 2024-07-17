package src.com.craftinginterpreters.Lox;

class Interpreter implements Expr.Visitor<Object> {

  //The Interpreter’s public API is simply one method.
  void interpret(Expr expression) { 
    try {
      Object value = evaluate(expression);
      System.out.println(stringify(value));
    } catch (RuntimeError error) {
      Balabizo.runtimeError(error);
    }
  }
  private String stringify(Object object) {
    if (object == null) return "nil";

    if (object instanceof Double) {
      String text = object.toString();
      if (text.endsWith(".0")) {
        text = text.substring(0, text.length() - 2);
      }
      return text;
    }

    return object.toString();
  }
  @Override
  public Object visitLiteralExpr(Expr.Literal expr) {
    return expr.value;
  }
  //to evaluate is grouping—the node you get as a result of using explicit parentheses in an expression.
  public Object visitGroupingExpr(Expr.Grouping expr) {
    return evaluate(expr.expression);
  }

  private Object evaluate(Expr expr) {
    return expr.accept(this);
  }

  @Override
  public Object visitBinaryExpr(Expr.Binary expr) {
    Object left = evaluate(expr.left);
    Object right = evaluate(expr.right); 

    switch (expr.operator.type) {

      case GREATER:
        checkNumberOperands(expr.operator, left, right);      
        return (double)left > (double)right;
      case GREATER_EQUAL:
        checkNumberOperands(expr.operator, left, right);
        return (double)left >= (double)right;
      case LESS:
        checkNumberOperands(expr.operator, left, right);
        return (double)left < (double)right;
      case LESS_EQUAL:
        checkNumberOperands(expr.operator, left, right);
        return (double)left <= (double)right;
      case BANG_EQUAL:
        return !isEqual(left, right);
      case EQUAL_EQUAL:
        return isEqual(left, right);  

      case MINUS:
        checkNumberOperands(expr.operator, left, right);
        return (double)left - (double)right;

      /*case PLUS:
        if (left instanceof Double && right instanceof Double) {
          return (double)left + (double)right;
        } 
        if (left instanceof String && right instanceof String) {
          return (String)left + (String)right;
        */
      //break;
      case PLUS:
      if (left instanceof Double && right instanceof Double) {
          return (double)left + (double)right;
      } 
      if (left instanceof String && right instanceof String) {
          return (String)left + (String)right;
      }
      if (left instanceof String) {
          return (String)left + right.toString();
      }
      if (right instanceof String) {
          return left.toString() + (String)right;
      }  
        throw new RuntimeError(expr.operator,
        "Balabizo code, Operands must be two numbers or two strings.");
      case SLASH:
        checkNumberOperands(expr.operator, left, right);
        if (right.equals(0))
          throw new RuntimeError((Token)right, "Balabizo arthmetic ? Change right Operand to be a non Zero");
        return (double)left / (double)right;
      case STAR:
        checkNumberOperands(expr.operator, left, right);
        return (double)left * (double)right;
    }

    // Unreachable.
    return null;
  }

  @Override
  public Object visitUnaryExpr(Expr.Unary expr) {
    Object right = evaluate(expr.right);

    switch (expr.operator.type) {
        case BANG:
            return !isTruthy(right);
        case MINUS:
            checkNumberOperand(expr.operator, right); //Type error handling 
            return -(double)right;
    }

    // Unreachable.
    return null;
  }
  private void checkNumberOperand(Token operator, Object operand) {
    if (operand instanceof Double) return;
    throw new RuntimeError(operator, "Balabizo Code ? Change Operand to be a number");
  }
  private void checkNumberOperands(Token operator,
                                   Object left, Object right) {
    if (left instanceof Double && right instanceof Double) return;
    
    throw new RuntimeError(operator, "Balabizo Code ? Change Operands to be numbers");
  }

    //First, we evaluate the operand expression. Then we apply the unary operator itself to the result of that. 
    //There are two different unary expressions, identified by the type of the operator token.
    // we identify everything is true except false and nil and 0
    private boolean isTruthy(Object object) {
      if (object == null) return false;
      if (object instanceof Boolean) return (boolean)object;
      //if (object instanceof Number) return ((Number)object).doubleValue() != 0;
      //if (object instanceof String) return !object.equals("0");
      return true;
  }

  private boolean isEqual(Object a, Object b) {
    if (a == null && b == null) return true;
    if (a == null) return false;
    // the same equallity as java 
    return a.equals(b);
  }
  

}











/*
Lox type	Java representation
Any Lox value	Object
nil	null
Boolean	Boolean
number	Double
string	String
 */