package com.swd392.onlineshop.service;
public class FormException extends RuntimeException {
    private final String field;
    public FormException(String field, String message) { super(message); this.field = field; }
    public String getField() { return field; }
}