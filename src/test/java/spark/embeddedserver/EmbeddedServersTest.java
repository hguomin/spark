package spark.embeddedserver;

import java.io.File;

import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import spark.embeddedserver.jetty.EmbeddedJettyFactory;
import spark.embeddedserver.jetty.JettyServerFactory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static spark.Spark.get;
import static spark.Spark.stop;

public class EmbeddedServersTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();


    @Test
    public void testAddAndCreate_whenCreate_createsCustomServer() throws Exception {
        // Create custom Server
        Server server = new Server();
        File requestLogDir = temporaryFolder.newFolder();
        File requestLogFile = new File(requestLogDir, "request.log");
        server.setRequestLog(new NCSARequestLog(requestLogFile.getAbsolutePath()));
        JettyServerFactory serverFactory = mock(JettyServerFactory.class);
        when(serverFactory.create(0, 0, 0)).thenReturn(server);

        String id = "custom";

        // Register custom server
        EmbeddedServers.add(id, new EmbeddedJettyFactory(serverFactory));
        EmbeddedServer embeddedServer = EmbeddedServers.create(id, null, null, false);
        assertNotNull(embeddedServer);
        embeddedServer.ignite("localhost", 0, null, 0, 0, 0);

        assertTrue(requestLogFile.exists());
        embeddedServer.extinguish();
        verify(serverFactory).create(0, 0, 0);
    }

    @Test
    public void testAdd_whenConfigureRoutes_createsCustomServer() throws Exception {
        String id = "custom";
        File requestLogDir = temporaryFolder.newFolder();
        File requestLogFile = new File(requestLogDir, "request.log");
        // Register custom server
        EmbeddedServers.add(EmbeddedServers.Identifiers.JETTY, new EmbeddedJettyFactory((i, j, k) -> {
            Server server = new Server();
            server.setRequestLog(new NCSARequestLog(requestLogFile.getAbsolutePath()));
            return server;
        }));

        get("/", (request, response) -> "OK");

        stop();
        assertTrue(requestLogFile.exists());

    }

}