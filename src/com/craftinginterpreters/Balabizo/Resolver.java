package src.com.craftinginterpreters.Balabizo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
  private final Interpreter interpreter;
  private final Stack<Map<String, Boolean>> scopes = new Stack<>();
  private FunctionType currentFunction = FunctionType.NONE;
  private LoopType currentLoop = LoopType.NONE;

  Resolver(Interpreter interpreter) {
    this.interpreter = interpreter;
  }
  private enum FunctionType {
    NONE,
    FUNCTION,
    INITIALIZER,
    METHOD
  }
  private enum LoopType {
    NONE,
    LOOP
  }
  private enum ClassType {
    NONE,
    CLASS
  }

  private ClassType currentClass = ClassType.NONE;
  @Override
  //Block statements; introduces a new scope for the statements it contains.
  public Void visitBlockStmt(Stmt.Block stmt) {
    beginScope();
    resolve(stmt.statements);
    endScope();
    return null;
  }
  @Override
  public Void visitVarStmt(Stmt.Var stmt) {
    declare(stmt.name); // put it in the innermost scope but with false / not ready yet
    if (stmt.initializer != null) {
      resolve(stmt.initializer); // if its intialized then let it use the Visitor pattern
    }
    define(stmt.name); //make it true to be used
    return null;
  }
  @Override
  public Void visitAssignExpr(Expr.Assign expr) {
    resolve(expr.value);
    resolveLocal(expr, expr.name);
    return null;
  }
  @Override
  public Void visitFunctionStmt(Stmt.Function stmt) {
    declare(stmt.name);
    define(stmt.name); // defined before resolve to allow recursion !
    resolveFunction(stmt, FunctionType.FUNCTION);
    return null;
  }
  private void resolveFunction(
    Stmt.Function function, FunctionType type) {
    FunctionType enclosingFunction = currentFunction; // ot just that we’re in a function, but how many we’re in.
    currentFunction = type;
    beginScope();
    for (Token param : function.params) {
      declare(param);
      define(param);
    }
    resolve(function.body);
    endScope();
    currentFunction = enclosingFunction;
    // instead of using a stack we’ll piggyback on the JVM
  }
  void resolve(List<Stmt> statements) {
    for (Stmt statement : statements) {
      resolve(statement);
    }
  }
  private void resolve(Stmt stmt) {
    stmt.accept(this);
  }
  private void resolve(Expr expr) {
    expr.accept(this);
  }
  private void beginScope() {
    scopes.push(new HashMap<String, Boolean>());
  }
  // to exit a scope; ez
  private void endScope() {
    scopes.pop();
}
private void declare(Token name) {
    if (scopes.isEmpty()) return;

    Map<String, Boolean> scope = scopes.peek();
    if (scope.containsKey(name.lexeme)) {
        Balabizo.error(name,
            "Balabizo, Already a variable with this name in this scope.");
      }
    scope.put(name.lexeme, false);
  }
  private void define(Token name) {
    if (scopes.isEmpty()) return;
    scopes.peek().put(name.lexeme, true);
  }
  @Override
  public Void visitVariableExpr(Expr.Variable expr) {
    if (!scopes.isEmpty() &&
        scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) { // false :declared but not yet defined.
      Balabizo.error(expr.name,
          "Balabizo, Can't read local variable in its own initializer.");
    }
    resolveLocal(expr, expr.name);
    return null;
  }
  private void resolveLocal(Expr expr, Token name) {
    for (int i = scopes.size() - 1; i >= 0; i--) { // start at the innermost scope and work outwards
      if (scopes.get(i).containsKey(name.lexeme)) {
        interpreter.resolve(expr, scopes.size() - 1 - i);
        return;
      }
    }
  }
  
  // not needed but must 
  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt) {
    resolve(stmt.expression);
    return null;
  }
  @Override
  public Void visitIfStmt(Stmt.If stmt) {
    resolve(stmt.condition);
    resolve(stmt.thenBranch);
    if (stmt.elseBranch != null) resolve(stmt.elseBranch);
    return null;
  }
  @Override
  public Void visitPrintStmt(Stmt.Print stmt) {
    resolve(stmt.expression);
    return null;
  }
  @Override
  public Void visitReturnStmt(Stmt.Return stmt) {
    if (currentFunction == FunctionType.NONE) {
        Balabizo.error(stmt.keyword, "Balabizo, Can't return from top-level code. must be in a function");
      }
    if (stmt.value != null) {
      if (currentFunction == FunctionType.INITIALIZER) {
        Balabizo.error(stmt.keyword,
            "Balabizo, Can't return a value from an initializer.");
      }
      resolve(stmt.value);
    }

    return null;
  }
  @Override
  public Void visitBreakStmt(Stmt.Break stmt) {
      if (currentLoop == LoopType.NONE) {
          Balabizo.reporteasily("Balabizo, Can't have a \"break\" outside of a loop.");
      }
      return null;
  }
  @Override
  public Void visitWhileStmt(Stmt.While stmt) {
    LoopType enclosingLoop = currentLoop;
    currentLoop = LoopType.LOOP;

    resolve(stmt.condition);
    resolve(stmt.body);

    currentLoop = enclosingLoop;
    return null;
  }
  @Override
  public Void visitBinaryExpr(Expr.Binary expr) {
    resolve(expr.left);
    resolve(expr.right);
    return null;
  }
  @Override
  public Void visitCallExpr(Expr.Call expr) {
    resolve(expr.callee);

    for (Expr argument : expr.arguments) {
      resolve(argument);
    }

    return null;
  }
  @Override
  public Void visitGroupingExpr(Expr.Grouping expr) {
    resolve(expr.expression);
    return null;
  }
  @Override
  public Void visitLiteralExpr(Expr.Literal expr) {
    return null;
  }
  @Override
  public Void visitLogicalExpr(Expr.Logical expr) {
    resolve(expr.left);
    resolve(expr.right);
    return null;
  }
  @Override
  public Void visitUnaryExpr(Expr.Unary expr) {
    resolve(expr.right);
    return null;
  }
  @Override
  public Void visitClassStmt(Stmt.Class stmt) {
    declare(stmt.name);
    define(stmt.name);
    ClassType enclosingClass = currentClass;
    currentClass = ClassType.CLASS;
    beginScope();
    scopes.peek().put("this", true);
    //beginScope();
    //scopes.peek().put("self", true);
    
    for (Stmt.Function method : stmt.methods) {
      FunctionType declaration = FunctionType.METHOD;
      if (method.name.lexeme.equals("create")) {
        declaration = FunctionType.INITIALIZER;
      }
      resolveFunction(method, declaration); 
    }
    //endScope();
    endScope();
    currentClass = enclosingClass;


    return null;
  }
  @Override
  public Void visitGetExpr(Expr.Get expr) {
    resolve(expr.object);
    return null;
  }
  @Override
  public Void visitSetExpr(Expr.Set expr) {
    resolve(expr.value);
    resolve(expr.object);
    return null;
  }
  @Override
  public Void visitselfExpr(Expr.self expr) {
    resolveLocal(expr, expr.keyword);
    return null;
  }
  @Override
  public Void visitThisExpr(Expr.This expr) {
    if (currentClass == ClassType.NONE) {
      Balabizo.error(expr.keyword,
          "Balabizo, Can't use 'this' outside of a class.");
      return null;
    }
    resolveLocal(expr, expr.keyword);
    return null;
  }
}