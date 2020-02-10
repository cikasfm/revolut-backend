package org.vilutis.lt.revolut.backend.storage;

import java.sql.SQLException;

public interface ExceptionHelper {

    static RuntimeException convertException(SQLException ex) {
        StringBuilder message = new StringBuilder();
        for (Throwable next : ex) {
            if (next instanceof SQLException) {
                SQLException e = (SQLException) next;
                if (message.length() > 0) {
                    message.append("\n");
                }
                // TODO : should I print the full stack here?
                // e.printStackTrace(System.err);

                message.append("SQLState: " + e.getSQLState() + "\n");
                message.append("Error Code: " + e.getErrorCode() + "\n");
                message.append("Message: " + e.getMessage() + "\n");

            } else {
                message.append(String.format(""));
            }

            Throwable t = ex.getCause();
            while (t != null) {
                message.append("Cause: " + t + "\n");
                t = t.getCause();
            }
        }
        return new RuntimeException(message.toString(), ex);
    }

}
