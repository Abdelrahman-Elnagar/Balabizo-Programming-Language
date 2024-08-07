package src.com.craftinginterpreters.Balabizo;

import java.util.HashMap;
import java.util.Map;

class Environment {
  final Environment enclosing;
  private final Map<String, Object> values = new HashMap<>();
  private final Map<String, Boolean> initialized = new HashMap<>(); //to be modified

  Environment() {
    enclosing = null;
  }

  Environment(Environment enclosing) {
    this.enclosing = enclosing;
  }
  Object get(Token name) {
    if (values.containsKey(name.lexeme)) {
      if (!initialized.get(name.lexeme)) {//to be modified
        throw new RuntimeError(name, "Variable '" + name.lexeme + "' is not initialized, Balabizo");
    }
      return values.get(name.lexeme);
    }
    if (enclosing != null) return enclosing.get(name); // recursive

    throw new RuntimeError(name,
        "Balabizo, Undefined variable '" + name.lexeme + "'.");
  }
  void define(String name, Object value) { //we don’t check to see if it’s already present.
    values.put(name, value);
    initialized.put(name, value != null);
  }
  void assign(Token name, Object value) {
    if (values.containsKey(name.lexeme)) {
      values.put(name.lexeme, value);
      initialized.put(name.lexeme, true);
      return;
    }
    if (enclosing != null) {
      enclosing.assign(name, value);
      return;
    }
    throw new RuntimeError(name,
        "Balabizo, Undefined variable '" + name.lexeme + "'.");
  }
  Object getAt(int distance, String name) {
    return ancestor(distance).values.get(name);
  }
  void assignAt(int distance, Token name, Object value) {
    ancestor(distance).values.put(name.lexeme, value);
  }
  Environment ancestor(int distance) {
    Environment environment = this;
    for (int i = 0; i < distance; i++) {
      environment = environment.enclosing; 
    }
    //doesn’t even have to check to see if the variable is
    // there—we know it will be because the resolver already found it
    return environment;
  }
}