package src.com.craftinginterpreters.Balabizo;

import java.util.HashMap;
import java.util.Map;

class BalabizoInstance {
  private BalabizoClass klass;
  private final Map<String, Object> fields = new HashMap<>();

  BalabizoInstance(BalabizoClass klass) {
    this.klass = klass;
  }

  @Override
  public String toString() {
    return klass.name + " instance";
  }
  Object get(Token name) {
    if (fields.containsKey(name.lexeme)) {
      return fields.get(name.lexeme);
    }

    BalabizoFunction method = klass.findMethod(name.lexeme);
    if (method != null) return method.bind(this);

    throw new RuntimeError(name, 
        "Dear Balabizo, Undefined property '" + name.lexeme + "'.");
  }
  void set(Token name, Object value) {
    fields.put(name.lexeme, value);
  }

}
