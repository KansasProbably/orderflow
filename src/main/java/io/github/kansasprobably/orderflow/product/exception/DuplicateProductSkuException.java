package io.github.kansasprobably.orderflow.product.exception;

public class DuplicateProductSkuException extends RuntimeException {
    public DuplicateProductSkuException(String sku) {
        super("Product with sku " + sku + " already exists");
    }
}
