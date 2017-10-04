package com.horses.yours.business.model;

import java.io.Serializable;

/**
 * @author Brian Salvattore
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ErrorEntity implements Serializable {

    private String title;
    private String reason;
    private String suggestion;
    private String error_handler;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public String getErrorHandler() {
        return error_handler;
    }

    public void setErrorHandler(String error_handler) {
        this.error_handler = error_handler;
    }

    public Body newBody() {
        Body body = new Body();
        body.setError(this);
        return body;
    }

    public class Body implements Serializable {
        private ErrorEntity error;

        public ErrorEntity getError() {
            return error;
        }

        public void setError(ErrorEntity error) {
            this.error = error;
        }
    }
}
