package src.com.craftinginterpreters.Lox;

class ErrorReturn extends RuntimeException {
  final Object value;

  ErrorReturn(Object value) {
    super(null, null, false, false);
    this.value = value;
  }
}
