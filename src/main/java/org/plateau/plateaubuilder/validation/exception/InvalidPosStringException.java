package org.plateau.plateaubuilder.validation.exception;

public class InvalidPosStringException extends RuntimeException {

  public InvalidPosStringException(Throwable cause) {
    super(cause);
  }

  public InvalidPosStringException(String message) {
    super(message);
  }
}
