package software.full_stack;

import java.util.List;

import static software.full_stack.TokenType.*;

public class Parser {

    // Sentinel class to unwind the parser
    // Synchronise tokens on statement boundaries
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;

    private int current = 0;

    // Consume flat sequence of tokens
    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // Kick off parser
    // TODO: change when adding statements
    Expr parse () {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }

    // Expand an expression to an equality
    // equality -> comparison( ("!=" | "==") comparison)* ;
    private Expr expression() {
        return equality();
    }

    // The entry point
    private Expr equality() {
        // First comparison
        Expr expr = comparison();

        // Multi-occurrence in while loop
        // Find either != or == token
        while(match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // comparison -> term ( ( ">" | ">=" | "<" | "<=" ) term )*;
    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // Addition and Subtraction
    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // Multiplication and division
    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // Looking ahead of upcoming tokens -> Predictive parser
    // Grab token and recursively call unary to parse operand
    // unary -> ( "!" | "-" ) unary | primary ;
    private Expr unary() {
        if(match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    // primary -> NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;
    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        // Find a matching left parentheses
        // Otherwise, throw error
        if(match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        // Handle other cases
        throw error(peek(), "Expect expression.");
    }

    private boolean match(TokenType... types) {
        for (TokenType type: types) {
            // Does current token has any given types?
            if(check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        // Throw error if something unexpected happens
        throw error(peek(), message);
    }

    // Only return true if current token is of given type. Never consumes
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    // Consumes current token and returns it
    private Token advance() {
        if(!isAtEnd()) current++;
        return previous();
    }

    // --- Primitive operations ---
    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    // Discard tokens until boundary right at the beginning of the next statement
    // AKA after a semicolon
    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }
}
