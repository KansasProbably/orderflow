package io.github.kansasprobably.orderflow.customer.exception;

public class CustomerEmailAlreadyExistsException extends RuntimeException {
    public CustomerEmailAlreadyExistsException(String email) {
      super("Customer with provided email already exists");
    }
}
