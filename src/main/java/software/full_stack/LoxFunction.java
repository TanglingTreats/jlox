package software.full_stack;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;
    // Local scope stack
    private final Environment closure;

    LoxFunction(Stmt.Function declaration, Environment closure) {
        this.closure = closure;
        this.declaration = declaration;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for(int i = 0; i < declaration.params.size(); i++) {
            // Create variable stack with param names and argument values
            // Provides encapsulation
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }


        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }
        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
}
