

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
      Object receive;
      receive = SocketUtil.receive(socket);
      if (receive instanceof Message) {
        Message response = (Message) receive;
        if (l != null) {
          try {
            l.success(response);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }
  }

  public interface ResponseListener {
    void success(Message response) throws IOException;
  }
}
