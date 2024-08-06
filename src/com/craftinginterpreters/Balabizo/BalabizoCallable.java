//The Java representation of any Balabizo object that can be called like a function will implement this interface. 
package src.com.craftinginterpreters.Balabizo;

import java.util.List;

interface BalabizoCallable {
    int arity();

  Object call(Interpreter interpreter, List<Object> arguments);
}