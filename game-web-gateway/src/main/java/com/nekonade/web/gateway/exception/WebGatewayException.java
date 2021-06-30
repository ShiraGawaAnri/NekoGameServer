package com.nekonade.web.gateway.exception;

import com.nekonade.common.error.IServerError;
import org.slf4j.helpers.MessageFormatter;

public class WebGatewayException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private IServerError error;

    public WebGatewayException() {
    }

    private WebGatewayException(IServerError error, String message, Throwable exp) {
        super(message, exp);
        this.error = error;
    }

    private WebGatewayException(IServerError error, String message) {
        super(message);
        this.error = error;
    }

    public static WebGatewayException.Builder newBuilder(IServerError error) {
        return new WebGatewayException.Builder(error);
    }

    public IServerError getError() {
        return error;
    }

    public static class Builder {
        private final IServerError error;
        private String message;
        private Throwable exp;

        public Builder(IServerError error) {
            this.error = error;
        }

        public WebGatewayException.Builder message(String message) {
            this.message = message;
            return this;
        }

        public WebGatewayException.Builder message(String format, Object... args) {
            this.message = MessageFormatter.arrayFormat(format, args).getMessage();
            return this;
        }

        public WebGatewayException.Builder causeBy(Throwable exp) {
            this.exp = exp;
            return this;
        }

        public WebGatewayException build() {
            String msg = this.error.toString();
            StringBuilder str = new StringBuilder(msg);

            if (this.message != null) {
                str.append("   ").append(this.message);
            }
            if (this.exp == null) {
                return new WebGatewayException(this.error, str.toString());
            } else {
                return new WebGatewayException(this.error, str.toString(), this.exp);
            }

        }

    }
}
