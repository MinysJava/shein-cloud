//Хендлер авторизации

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.sql.*;

public class AuthService extends ChannelInboundHandlerAdapter {
    private static Connection connection;
    private static Statement stmt;

    public static void connection() {       //Метод для подключяемся к базе user'ов
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:shein-cloud-server\\src\\main\\resources\\udb.db");
            stmt = connection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if(msg instanceof LoginRequest){
            LoginRequest lr = (LoginRequest) msg;       // Получяем логин и пароль
            String nikName = AuthService.getNickByLoginAndPass(lr.getLogin(), lr.getPass());    //Проверяем их по базе
            if (nikName != null) {          // Если автоизация прошла то удаляем хендлер авторизации из трубы и добавляем главный хендлер
                                            // с передачей имени пользователя
                ctx.pipeline().removeLast();
                ctx.pipeline().addLast(new ServerHandler(nikName));
                ctx.writeAndFlush(new Request("loginOk", nikName));
            } else {
                ctx.writeAndFlush(new Request("loginFail"));
            }
        }
        if(msg instanceof Request){
            Request rf = (Request) msg;
            switch (rf.getCommand()) {      // обработка команды на закрытие канала при отключении клиента
                case ("close"):
                    ctx.close();
                    break;
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public static void setNewUsers(int id, String login, String pass, String nickName) {    // метод для добавления нового пользователя
        int hash = pass.hashCode();
        String sql = String.format("INSERT INTO users (id, login, password, nickname) VALUES('%s', '%s', '%s', '%s')", id, login, hash, nickName);

        try {
            int t = stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getNickByLoginAndPass(String login, String pass) {     //Проверяем логин и пароль по базе
        int hash = pass.hashCode();
        String sql = String.format("SELECT nickname FROM users where login = '%s' and password = '%s'", login, hash);

        try {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void disconnect() {   // Метод для отключения от БД
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
