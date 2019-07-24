package ambitious.but.rubbish.api.user;

import ambitious.but.rubbish.api.ServWrapper;
import ambitious.but.rubbish.lib.JWT;
import ambitious.but.rubbish.lib.Token;
import ambitious.but.rubbish.lib.Utils;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "Login")
public class Login extends ServWrapper {
    /**
     * Processing for login POST request from frontend.
     *
     * @param request Incoming HTTP request
     * @param response Outgoing HTTP response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doRequestSessionless((c) -> {
            JWT jwt = new JWT();
            JSONObject jsonIn = new JSONObject();
            JSONObject jout = new JSONObject();
            try {
                JSONObject json = Utils.JSONFromPost(request); // Parses JSON request
                String email = json.getString("username"); // Extracts 'username' field
                String pass = Utils.hashPassword(json.getString("password"));

                String query = "SELECT t.id " +
                        "FROM projectdata.tokens t, projectdata.users u " +
                        "WHERE u.email = ? " +
                        "AND t.id = u.uid";
                PreparedStatement st = c.prepareStatement(query);
                st.setString(1, email);

                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    PreparedStatement delete = c.prepareStatement("DELETE FROM projectdata.tokens WHERE id = ?");
                    delete.setInt(1, rs.getInt("id"));
                    delete.executeUpdate();
                    c.commit();
                }

                String query2 = "SELECT * FROM projectdata.users u " +
                        "WHERE u.email = ? " +
                        "AND u.password = ?";
                PreparedStatement st2 = c.prepareStatement(query2);
                st2.setString(1, email);
                st2.setString(2, pass);
                ResultSet rs2 = st2.executeQuery();
                while (rs2.next()) {
                    jsonIn.put("id", rs2.getInt("uid"));
                    jsonIn.put("name", rs2.getString("username"));
                    jsonIn.put("email", rs2.getString("email"));
                    jsonIn.put("gender", rs2.getString("gender"));
                    jsonIn.put("weight", rs2.getString("weight"));
                    jsonIn.put("height", rs2.getString("height"));
                    jsonIn.put("typeid", rs2.getInt("typeid"));
                    jsonIn.put("age", rs2.getString("age"));
                }
                rs2.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (jsonIn.isEmpty()) {
                throw new httpError("User account does not exist", 400);
            } else {
                jwt.addViaJSON(jsonIn);
                String userJWS = jwt.constructJWT();
                jout.put("user-token", userJWS);
                String sessionKey = new Token(32, "0123456789").nextToken();
                long time = System.currentTimeMillis();
                Map<String, Object> user_token = new HashMap<>();
                user_token.put("id", jsonIn.getInt("id"));
                user_token.put("ssk", sessionKey);
                user_token.put("iat", time);
                jwt.addViaMap(user_token);
                String sessionJws = jwt.constructJWT();
                jout.put("session-token", sessionJws);
                try {
                    PreparedStatement stmt = c.prepareStatement("INSERT INTO projectdata.tokens" +
                            " VALUES(?, ?, ?, ?)");
                    stmt.setInt(1, (int) jsonIn.get("id"));
                    stmt.setString(2, sessionKey);
                    stmt.setLong(3, time);
                    stmt.setLong(4, time + 1800000);
                    stmt.executeUpdate();
                    c.commit();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return jout;
            }
        }, request, response);
    }
}
