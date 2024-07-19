package src.com.craftinginterpreters.Lox;

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
}