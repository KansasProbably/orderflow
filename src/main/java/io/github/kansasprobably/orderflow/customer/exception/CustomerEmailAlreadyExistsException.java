package io.github.kansasprobably.orderflow.customer.exception;

public class CustomerEmailAlreadyExistsException extends RuntimeException {
    public CustomerEmailAlreadyExistsException(String email) {
      super("Customer with email " + email + " already exists");
    }
}
