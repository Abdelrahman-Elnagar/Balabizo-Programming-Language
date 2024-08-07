package src.com.craftinginterpreters.Balabizo;

class BreakError extends RuntimeException {
    BreakError() {
        super(null, null, false, false);
    }
}