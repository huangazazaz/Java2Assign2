

import java.io.Serializable;

public class Message implements Serializable {
  private Object content;
  private Type type;
  private String from;
  private String to;
  private String fromPlayer;
  private String toPlayer;

  public Message() {
  }

  public Message(Object content, Type type, String from, String to) {
    this.content = content;
    this.type = type;
    this.from = from;
    this.to = to;
  }

  @Override
  public String toString() {
    return "Message{" +
        "content=" + content +
        ", type=" + type +
        ", from='" + from + '\'' +
        ", to='" + to + '\'' +
        ", fromPlayer='" + fromPlayer + '\'' +
        ", toPlayer='" + toPlayer + '\'' +
        '}';
  }

  public Object getContent() {
    return content;
  }

  public void setContent(Object content) {
    this.content = content;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public String getFromPlayer() {
    return fromPlayer;
  }

  public void setFromPlayer(String fromPlayer) {
    this.fromPlayer = fromPlayer;
  }

  public String getToPlayer() {
    return toPlayer;
  }

  public void setToPlayer(String toPlayer) {
    this.toPlayer = toPlayer;
  }
  public enum Type {
    LOGIN,//登陆
    REG,//注册
    FORGOT,//忘记密码
    LIST,//在线用户
    FIGHT,//对战
    FIGHT_SUCCESS,//对战成功
    SUCCESS,//成功
    FAILURE,//失败
    PLANT,//下子
    PLAYER,//玩家列表
  }
}
