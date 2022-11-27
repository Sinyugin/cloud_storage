package server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuthService {


    private static class UserData {
        private String nick;
        private String login;
        private String pass;

        public UserData(String nick, String login, String password) {
            this.nick = nick;
            this.login = login;
            this.pass = password;
        }

        public String getNick() {
            return nick;
        }

        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return pass;
        }
    }
    private List<UserData> users;

    public AuthService(){
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/db/users.db");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select * from users ");
            users = new ArrayList<>();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String nick = resultSet.getString("nick");
                String login = resultSet.getString("login");
                String pass = resultSet.getString("pass");
                users.add(new UserData(nick, login, pass));
//                System.out.println(String.format("%s - %s - %s - %s", id, nick, login, pass)); //логин для теста программы
            }
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getNickByLoginAndPassword(String login, String pass) { //возвращает nick по введенному логину и паролю
        return users.stream()
                .filter(user -> login.equals(user.getLogin())
                        && pass.equals(user.getPassword()))
                .findFirst()
                .map(UserData::getNick)
                .orElse("Пользователь не найден");
    }
}
