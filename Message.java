

import java.io.Serializable;

public class Message implements Serializable {
  private int x;
  private int y;

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  private boolean quit;

  public boolean isQuit() {
    return quit;
  }

  public void setQuit(boolean quit) {
    this.quit = quit;
  }

  private Object content;
  private Type type;
  private String from;
  private String to;
  private String fromPlayer;
  private String toPlayer;

  public Message() {
  }

  public Message(Type type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return "Message{"
        + "content=" + content
        + ", type=" + type
        + ", from='" + from + '\''
        + ", to='" + to + '\''
        + ", fromPlayer='" + fromPlayer + '\''
        + ", toPlayer='" + toPlayer + '\''
        + '}';
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
    LOGIN, //登陆
    REG, //注册
    LOGOUT, //下线
    FORGOT, //忘记密码
    FIGHT, //对战
    FIGHT_SUCCESS, //对战成功
    FIGHT_FAILURE, //对战失败
    SUCCESS, //成功
    FAILURE, //失败
    PLANT, //下子
    PLAYER, //玩家列表
    BREAK, //服务器崩溃
    WAIT, //掉线待重连
    RENEW, //重连
    END, //游戏结束
    REFUSE, //拒绝查看玩家列表
    REFRESH, //刷新战绩
    WRONG, //密码错误
    NONEXISTENCE, //账户不存在
  }
}
