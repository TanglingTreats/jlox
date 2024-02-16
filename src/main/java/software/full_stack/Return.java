package software.full_stack;

// Wrapper for return values to unwind back to call()
public class Return extends RuntimeException {
    final Object value;

    Return(Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}
