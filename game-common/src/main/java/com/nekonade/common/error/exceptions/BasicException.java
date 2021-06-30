package com.nekonade.common.error.exceptions;

import com.nekonade.common.error.IServerError;
import lombok.Getter;
import org.slf4j.helpers.MessageFormatter;

@Getter
public class BasicException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    private final IServerError error;

    private final Object data;

    public BasicException(IServerError error, Object data) {
        this.error = error;
        this.data = data;
    }

    public BasicException(String message, IServerError error, Object data) {
        super(message);
        this.error = error;
        this.data = data;
    }

    public BasicException(String message, Throwable cause, IServerError error, Object data) {
        super(message, cause);
        this.error = error;
        this.data = data;
    }

    public BasicException(Throwable cause, IServerError error, Object data) {
        super(cause);
        this.error = error;
        this.data = data;
    }

    public static Builder newBuilder(IServerError error) {
        return new Builder(error);
    }

    public BasicException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, IServerError error, Object data) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.error = error;
        this.data = data;
    }

    public BasicException(IServerError error, String message, Object data) {
        super(message);
        this.error = error;
        this.data = data;
    }

    public static class Builder<T extends BasicException> {

        private final IServerError error;

        private String message;

        private Throwable exp;

        private Object data;

        public Builder(IServerError error) {
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

        public BasicException build() {
            String msg = this.error.toString();
            StringBuilder str = new StringBuilder(msg);

            if (this.message != null) {
                str.append("   ").append(this.message);
            }
            if (this.data != null) {

            }
            if (this.exp == null) {
                return new BasicException(this.error, str.toString(), data);
            } else {
                return new BasicException(this.error, str.toString(), this.exp);
            }

        }

    }
}
