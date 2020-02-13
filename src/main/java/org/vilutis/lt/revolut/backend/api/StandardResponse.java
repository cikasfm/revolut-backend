package org.vilutis.lt.revolut.backend.api;

import spark.utils.Assert;

import java.io.Serializable;

/**
 * Serializable standard response DTO
 */
public class StandardResponse<T extends Serializable> implements Serializable {

    final int status;

    final String message;

    final T data;

    StandardResponse(int status, String message, T data) {
        Assert.notNull(status, "status param cannot be null");
        Assert.notNull(message, "message param cannot be null");
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public StandardResponse(int status, String message) {
        this(status, message, null);
    }

    /**
     * Responds with default status 200, message "OK" and serializes given data to a JSON
     *
     * @param data the object to return in the "data" attribute of the JSON
     * @return a serialized {@link StandardResponse} to a JSON string
     */
    public static <T extends Serializable> StandardResponse<T> respondOK(T data) {
        return new StandardResponse<T>(200, "OK", data);
    }

    /**
     * Responds with given status and message. Used mostly for "error" responses.
     *
     * @param status the status code
     * @param message the message to be returned
     * @return a {@link StandardResponse} to a JSON string
     */
    public static StandardResponse respond(int status, String message) {
        return new StandardResponse(status, message);
    }

    @Override
    public String toString() {
        return "Response{" + "status='" + status + '\'' + ", message='" + message + '\'' + ", data=" + data + '}';
    }
}
