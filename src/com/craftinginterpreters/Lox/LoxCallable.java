//The Java representation of any Lox object that can be called like a function will implement this interface. 
package src.com.craftinginterpreters.Lox;

import java.util.List;

interface LoxCallable {
    int arity();

  Object call(Interpreter interpreter, List<Object> arguments);
}