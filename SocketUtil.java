

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class SocketUtil {
  public static void send(Socket s, Message message) {
    OutputStream outputStream = null;
    ObjectOutputStream objectOutputStream = null;
    try {
      outputStream = s.getOutputStream();
      objectOutputStream = new ObjectOutputStream(outputStream);
      objectOutputStream.writeObject(message);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static Object receive(Socket s) {
    InputStream inputStream;
    ObjectInputStream objectInputStream;
    try {
      inputStream = s.getInputStream();
      objectInputStream = new ObjectInputStream(inputStream);
      return objectInputStream.readObject();
    } catch (Exception e) {
      Message m = new Message();
      m.setType(Message.Type.BREAK);
      return m;
    }
  }

  public static Socket create(String ip, int port) {

    try {
      return new Socket(InetAddress.getByName(ip), port);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static Socket createLocal(int port) {

    try {
      return new Socket(InetAddress.getLocalHost(), port);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public void close(InputStream IS, OutputStream OS) {
    if (IS != null) {
      try {
        IS.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    if (OS != null) {
      try {
        OS.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

}

