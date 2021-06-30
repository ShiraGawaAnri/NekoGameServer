package com.nekonade.common.error.exceptions;

import com.nekonade.common.error.IServerError;
import lombok.Getter;
import org.slf4j.helpers.MessageFormatter;

@Getter
public class GameNotifyException extends BasicException {

    public GameNotifyException(IServerError error, Object data) {
        super(error, data);
    }

    public GameNotifyException(String message, IServerError error, Object data) {
        super(message, error, data);
    }

    public GameNotifyException(String message, Throwable cause, IServerError error, Object data) {
        super(message, cause, error, data);
    }

    public GameNotifyException(Throwable cause, IServerError error, Object data) {
        super(cause, error, data);
    }

    public GameNotifyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, IServerError error, Object data) {
        super(message, cause, enableSuppression, writableStackTrace, error, data);
    }

    public GameNotifyException(IServerError error, String message, Object data) {
        super(error, message, data);
    }

    public static Builder newBuilder(IServerError error) {
        return new Builder(error);
    }

    public static class Builder extends BasicException.Builder<GameNotifyException>{

        private final IServerError error;

        private String message;

        private Throwable exp;

        private Object data;

        public Builder(IServerError error) {
            super(error);
            this.error = error;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder data(Object data) {
            this.data = data;
            return this;
        }

        public Builder message(String format, Object... args) {
            this.message = MessageFormatter.arrayFormat(format, args).getMessage();
            return this;
        }

        public Builder causeBy(Throwable exp) {
            this.exp = exp;
            return this;
        }

        public GameNotifyException build() {
            String msg = this.error.toString();
            StringBuilder str = new StringBuilder(msg);

            if (this.message != null) {
                str.append("   ").append(this.message);
            }
            if (this.data != null) {

            }
            if (this.exp == null) {
                return new GameNotifyException(this.error, str.toString(), data);
            } else {
                return new GameNotifyException(this.error, str.toString(), this.exp);
            }

        }

    }

}
