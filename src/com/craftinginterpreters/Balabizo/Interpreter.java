package src.com.craftinginterpreters.Balabizo;
import java.util.List;

import src.com.craftinginterpreters.Balabizo.Stmt.Return;

import java.util.ArrayList;

class Interpreter implements Expr.Visitor<Object>,
                             Stmt.Visitor<Void> { // for statements that retun no value (Void)

  final Environment globals = new Environment();
  private Environment environment = globals;
  //The Interpreter’s public API is simply one method.
  Interpreter() {
    globals.define("clock", new BalabizoCallable() {
      @Override
      public int arity() { return 0; }

      @Override
      public Object call(Interpreter interpreter, //calls the corresponding Java function / Native Function
                         List<Object> arguments) {
        return (double)System.currentTimeMillis() / 1000.0;
      }

      @Override
      public String toString() { return "<native fn>"; }
    });
  }

   void interpret(List<Stmt> statements) {
    try {
      for (Stmt statement : statements) {
        execute(statement);
      }
    } catch (RuntimeError error) {
      Balabizo.runtimeError(error);
    }
  }
  private void execute(Stmt stmt) {
    stmt.accept(this);
  }
  @Override
  public Void visitBlockStmt(Stmt.Block stmt) {
    executeBlock(stmt.statements, new Environment(environment));
    return null;
  }
  void executeBlock(List<Stmt> statements,
                    Environment environment) {
    Environment previous = this.environment;
    try {
      this.environment = environment;

      for (Stmt statement : statements) {
        execute(statement);
      }
    } finally {
      this.environment = previous;
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
  public Object visitLogicalExpr(Expr.Logical expr) { // differ than the VisitBinary in which its short-circuit eval left first and judge
    Object left = evaluate(expr.left);

    if (expr.operator.type == TokenType.OR) {
      if (isTruthy(left)) return left;
    } else {
      if (!isTruthy(left)) return left;
    }

    return evaluate(expr.right);
  }
  @Override
  public Void visitWhileStmt(Stmt.While stmt) {
    while (isTruthy(evaluate(stmt.condition))) {
      execute(stmt.body);
    }
    return null;
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
  public Void visitExpressionStmt(Stmt.Expression stmt) {
    evaluate(stmt.expression);
    return null;
  }
  @Override
  public Void visitFunctionStmt(Stmt.Function stmt) {
    BalabizoFunction function = new BalabizoFunction(stmt, environment);
    environment.define(stmt.name.lexeme, function);
    return null;
  }
  @Override
  public Void visitIfStmt(Stmt.If stmt) {
    if (isTruthy(evaluate(stmt.condition))) {
      execute(stmt.thenBranch);
    } else if (stmt.elseBranch != null) {
      execute(stmt.elseBranch);
    }
    return null;
  }
  @Override
  public Void visitPrintStmt(Stmt.Print stmt) {
    Object value = evaluate(stmt.expression);
    System.out.println(stringify(value));
    return null;
  }
  public Void visitReturnStmt(Stmt.Return stmt) {
    Object value = null;
    if (stmt.value != null) value = evaluate(stmt.value);
    throw new ErrorReturn(value);
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
  public Object visitCallExpr(Expr.Call expr) {
    Object callee = evaluate(expr.callee);

    List<Object> arguments = new ArrayList<>();
    for (Expr argument : expr.arguments) { 
      arguments.add(evaluate(argument));
    }
    if (!(callee instanceof BalabizoCallable)) {
      throw new RuntimeError(expr.paren,
          "Balabizo, Can only call functions and classes.");
    }
    BalabizoCallable function = (BalabizoCallable)callee;
    if (arguments.size() != function.arity()) {
      throw new RuntimeError(expr.paren, "Expected " +
          function.arity() + " arguments but got " +
          arguments.size() + ".");
    }
    return function.call(this, arguments);
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
  @Override
  public Void visitVarStmt(Stmt.Var stmt) {
    Object value = null;
    if (stmt.initializer != null) {
      value = evaluate(stmt.initializer);
    }

    environment.define(stmt.name.lexeme, value);
    return null;
  }
  @Override
  public Object visitVariableExpr(Expr.Variable expr) {
    return environment.get(expr.name);
  }
  @Override
  public Object visitAssignExpr(Expr.Assign expr) {
    Object value = evaluate(expr.value);
    environment.assign(expr.name, value);
    return value;
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
