

import java.io.IOException;
import java.net.Socket;
import java.util.Vector;

public class ClientThread extends Thread {
    Socket socket;
    ResponseListener l;
    private boolean shotDown;

    public ClientThread() {
    }

    public ClientThread(Socket socket, ResponseListener L) {
        this.socket = socket;
        this.l = L;
    }

    public boolean isShotDown() {
        return shotDown;
    }

    public void setShotDown(boolean shotDown) {
        this.shotDown = shotDown;
    }

    @Override
    public void run() {
        while (!shotDown) {
            System.out.println(socket.getInetAddress().getHostName());
            Object receive = SocketUtil.receive(socket);
            System.out.println(receive);
            if (receive instanceof Message) {
                Message response = (Message) receive;
                if (l != null) {
                    l.success(response);
                }
            }
        }
    }

    public interface ResponseListener {
        void success(Message response);
    }
}
