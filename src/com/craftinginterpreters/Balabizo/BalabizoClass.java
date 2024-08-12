package src.com.craftinginterpreters.Balabizo;

import java.util.List;
import java.util.Map;

class BalabizoClass implements BalabizoCallable{
  final String name;

  private final Map<String, BalabizoFunction> methods;

  BalabizoClass(String name, Map<String, BalabizoFunction> methods) {
    this.name = name;
    this.methods = methods;
  }

  @Override
  public String toString() {
    return name;
  }
  @Override
  public Object call(Interpreter interpreter,
                     List<Object> arguments) {
    BalabizoInstance instance = new BalabizoInstance(this);
    BalabizoFunction initializer = findMethod("create");
    if (initializer != null) {
      initializer.bind(instance).call(interpreter, arguments);
    }

    return instance;
  }

  @Override
  public int arity() {
    BalabizoFunction initializer = findMethod("create");
    if (initializer == null) return 0;
    return initializer.arity();
  }
  BalabizoFunction findMethod(String name) {
    if (methods.containsKey(name)) {
      return methods.get(name);
    }

    return null;
  }
}