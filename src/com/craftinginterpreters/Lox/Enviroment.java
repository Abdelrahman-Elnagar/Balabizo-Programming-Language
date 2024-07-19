package src.com.craftinginterpreters.Balabizo;

import java.util.HashMap;
import java.util.Map;

class Environment {
  final Environment enclosing;
  private final Map<String, Object> values = new HashMap<>();
  Environment() {
    enclosing = null;
  }

  Environment(Environment enclosing) {
    this.enclosing = enclosing;
  }
  Object get(Token name) {
    if (values.containsKey(name.lexeme)) {
      return values.get(name.lexeme);
    }
    if (enclosing != null) return enclosing.get(name); // recursive

    throw new RuntimeError(name,
        "Balabizo, Undefined variable '" + name.lexeme + "'.");
  }
  void define(String name, Object value) { //we don’t check to see if it’s already present.
    values.put(name, value);
  }
  void assign(Token name, Object value) {
    if (values.containsKey(name.lexeme)) {
      values.put(name.lexeme, value);
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