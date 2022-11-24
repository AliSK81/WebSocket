import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.concurrent.Future;
import java.util.logging.Logger;

@ServerEndpoint(value = "/demoApp")
public class WSServerEndpoint {

    static HashSet<Session> sessions = new HashSet<>();

    private static final Logger LOGGER = Logger.getLogger(WSServerEndpoint.class.getName());

    @OnOpen
    public void onOpen(Session session) {
        LOGGER.info("[SERVER]: Handshake successful! - Connected! - Session ID: " + session.getId());
        sessions.add(session);
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        LOGGER.info("[FROM CLIENT]: " + message + ", Session ID: " + session.getId());

        if (message.equalsIgnoreCase("terminate")) {
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Successfully session closed....."));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (message.equalsIgnoreCase("update")) {
            session.getBasicRemote().sendText("ok");
        }
    }

    @OnMessage
    public void onMessage(Session session, ByteBuffer message) throws IOException {
        System.out.println("file recieved");
        byte[] buffer = handleImage(message);
        saveBytes(buffer, "newFile.jpg");
        System.out.println("saved");
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        LOGGER.info("[SERVER]: Session " + session.getId() + " closed, because " + closeReason);
    }

    void saveBytes(byte[] buffer, String newFileName) throws IOException {
        int bytes = buffer.length;
        FileOutputStream fileOutputStream = new FileOutputStream(newFileName);
        fileOutputStream.write(buffer, 0, bytes);
    }

    public byte[] handleImage(ByteBuffer img) {
        return img.array();
    }
}