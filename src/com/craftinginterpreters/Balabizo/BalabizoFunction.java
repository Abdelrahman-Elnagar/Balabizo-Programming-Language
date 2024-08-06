package src.com.craftinginterpreters.Balabizo;

import java.util.List;

class BalabizoFunction implements LoxCallable {
  private final Environment closure;
  private final Stmt.Function declaration;
  BalabizoFunction(Stmt.Function declaration, Environment closure) {
    this.closure = closure;
    this.declaration = declaration;
  }
  @Override
  public Object call(Interpreter interpreter,
                     List<Object> arguments) {
    Environment environment = new Environment(closure);
    // each function gets its own environment as the function enncapsulate its parameters that is why its in the call not in the declaration
    for (int i = 0; i < declaration.params.size(); i++) {
      environment.define(declaration.params.get(i).lexeme,
          arguments.get(i));
    }

    try {
        interpreter.executeBlock(declaration.body, environment); // discards that function-local environment and restores the previous one that was active back at the callsite
      } catch (ErrorReturn returnValue) {
        return returnValue.value;
      } 
    return null; // return values will be added later 
  }
  @Override
  public int arity() {
    return declaration.params.size();
  }
  @Override
  public String toString() {
    return "<THis is Function \"" + declaration.name.lexeme + "\", Let's Explore it>";
  }
}