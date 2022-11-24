import jakarta.websocket.*;
import org.glassfish.tyrus.client.ClientManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.logging.Logger;

@ClientEndpoint
public class WSClient {

    private static final java.util.logging.Logger LOGGER = Logger.getLogger(WSServerEndpoint.class.getName());
    private static CountDownLatch latch;

    @OnOpen
    public void onOpen(Session session) throws IOException {
        LOGGER.info("[CLIENT]: Connected to server... \n[CLIENT]: Session ID: " + session.getId());
        try {
            session.getBasicRemote().sendText("update");
        } catch (IOException e) {
            e.printStackTrace();
        }

        syncLargeImage(readBytes("src/main/resources/rose.jpg"), session);

    }

    @OnMessage
    public void onMessage(Session session, String message) {
        LOGGER.info("[FROM SERVER]: " + message + ", Session ID: " + session.getId());
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        LOGGER.info("[CLIENT]: Session " + session.getId() + " close, because " + closeReason);
        latch.countDown();
    }

    @OnError
    public void onError(Session session, Throwable err) {
        LOGGER.info("[CLIENT]: Error!, Session ID: " + session.getId() + ", " + err.getMessage());
    }

    public static void main(String[] args) {
        latch = new CountDownLatch(1);
        ClientManager clientManager = ClientManager.createClient();
        URI uri = null;
        try {
            uri = new URI("ws://localhost:8080/java/demoApp");
            clientManager.connectToServer(WSClient.class, uri);
            latch.await();
        } catch (URISyntaxException | DeploymentException | InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    byte[] readBytes(String filePath) throws IOException {
        byte[] buffer = new byte[50000];
        FileInputStream fileInputStream = new FileInputStream(filePath);
        int bytes = fileInputStream.read(buffer, 0, buffer.length);
        return buffer;
    }

    public void syncLargeImage(byte[] buffer, Session session) {
        ByteBuffer img = ByteBuffer.wrap(buffer);
        System.out.println("Sending file...");
        Future<Void> deliveryProgress = session.getAsyncRemote().sendBinary(img);
        boolean delivered = deliveryProgress.isDone();
        System.out.println("File sent? " + delivered);
    }



}