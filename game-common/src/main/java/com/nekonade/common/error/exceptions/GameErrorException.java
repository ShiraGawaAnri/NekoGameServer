package com.nekonade.common.error.exceptions;

import com.nekonade.common.error.IServerError;
import org.slf4j.helpers.MessageFormatter;

public class GameErrorException extends BasicException {

    public GameErrorException(IServerError error, Object data) {
        super(error, data);
    }

    public GameErrorException(String message, IServerError error, Object data) {
        super(message, error, data);
    }

    public GameErrorException(String message, Throwable cause, IServerError error, Object data) {
        super(message, cause, error, data);
    }

    public GameErrorException(Throwable cause, IServerError error, Object data) {
        super(cause, error, data);
    }

    public GameErrorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, IServerError error, Object data) {
        super(message, cause, enableSuppression, writableStackTrace, error, data);
    }

    public GameErrorException(IServerError error, String message, Object data) {
        super(error, message, data);
    }

    public static Builder newBuilder(IServerError error) {
        return new Builder(error);
    }

    public static class Builder extends BasicException.Builder<GameErrorException> {

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

        public GameErrorException build() {
            String msg = this.error.toString();
            StringBuilder str = new StringBuilder(msg);

            if (this.message != null) {
                str.append("   ").append(this.message);
            }
            if (this.data != null) {

            }
            if (this.exp == null) {
                return new GameErrorException(this.error, str.toString(), data);
            } else {
                return new GameErrorException(this.error, str.toString(), this.exp);
            }

        }

    }

//    /**
//     *
//     */
//    private static final long serialVersionUID = 1L;
//
//    private final IServerError error;
//
//    @Getter
//    private final Object data;
//
//
//    public GameErrorException() {
//        this.error = null;
//        this.data = null;
//    }
//
//    public GameErrorException(IServerError error, Object data) {
//        this.error = error;
//        this.data = data;
//    }
//
//    private GameErrorException(IServerError error, String message, Throwable exp) {
//        super(message, exp);
//        this.error = error;
//        this.data = null;
//    }
//
//    private GameErrorException(IServerError error, String message, Throwable exp, Object data) {
//        super(message, exp);
//        this.error = error;
//        this.data = data;
//    }
//
//    private GameErrorException(IServerError error, String message, Object data) {
//        super(message);
//        this.error = error;
//        this.data = data;
//    }
//
//    public static Builder newBuilder(IServerError error) {
//        return new Builder(error);
//    }
//
//    public IServerError getError() {
//        return error;
//    }
//
//    public static class Builder {
//
//        private final IServerError error;
//
//        private String message;
//
//        private Throwable exp;
//
//        private Object data;
//
//        public Builder(IServerError error) {
//            this.error = error;
//        }
//
//        public Builder message(String message) {
//            this.message = message;
//            return this;
//        }
//
//        public Builder data(Object data) {
//            this.data = data;
//            return this;
//        }
//
//        public Builder message(String format, Object... args) {
//            this.message = MessageFormatter.arrayFormat(format, args).getMessage();
//            return this;
//        }
//
//        public Builder causeBy(Throwable exp) {
//            this.exp = exp;
//            return this;
//        }
//
//        public GameErrorException build() {
//            String msg = this.error.toString();
//            StringBuilder str = new StringBuilder(msg);
//
//            if (this.message != null) {
//                str.append("   ").append(this.message);
//            }
//            if (this.data != null) {
//
//            }
//            if (this.exp == null) {
//                return new GameErrorException(this.error, str.toString(), data);
//            } else {
//                return new GameErrorException(this.error, str.toString(), this.exp);
//            }
//
//        }
//
//    }

}
