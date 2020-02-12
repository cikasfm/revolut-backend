package org.vilutis.lt.revolut.backend.test;

import java.io.IOException;
import java.net.ServerSocket;

public interface TestUtil {

    static int findRandomOpenPort() {
        int localPort;

        // find a random available port
        try (ServerSocket s = new ServerSocket(0)) {
            localPort = s.getLocalPort();
        } catch (IOException e) {
            throw new Error(e.getMessage(), e);
        }
        return localPort;
    }
}
