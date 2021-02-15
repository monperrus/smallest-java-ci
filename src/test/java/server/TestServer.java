package server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Run Maven tests: mvn test

public class TestServer {
    
    @Test
    public void dummyTest() {
        int a = 1;
        assertEquals(a,1);
    }

    @Test
    public void dummyTest2() {
        ContinuousIntegrationServer server = new ContinuousIntegrationServer();
        int a = server.dummyFunction();
        assertEquals(1,a);
    }
}
