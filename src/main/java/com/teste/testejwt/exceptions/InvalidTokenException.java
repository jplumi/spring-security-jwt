package com.teste.testejwt.exceptions;

public class InvalidTokenException extends RuntimeException{

    public InvalidTokenException(String message) {
        super("Trying to authenticate with invalid token: " + message);
    }
}
