package com.nekonade.mq.system.exception;

public class GameMQSendFailedException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public GameMQSendFailedException(String msg) {
        super(msg);
    }

}
