package util;

public class ExceptionResolver {

    private final MessageDisplayer messageDisplayer;

    public ExceptionResolver(MessageDisplayer messageDisplayer) {
        this.messageDisplayer = messageDisplayer;
    }

    public void resolve(String message, Exception e) {
        messageDisplayer.displayError(e.getMessage(), message);
        throw new RuntimeException(e);
    }
}
