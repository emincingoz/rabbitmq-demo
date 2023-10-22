package com.demo.rabbitmq.exception;

public class MessageQueueException extends RuntimeException {
    public MessageQueueException() {
        super();
    }

    public MessageQueueException(String message) {
        super(message);
    }

    public MessageQueueException(String message, Throwable cause) {
        super(message, cause);
    }
}
