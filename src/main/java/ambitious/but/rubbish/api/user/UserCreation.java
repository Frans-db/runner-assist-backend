package ambitious.but.rubbish.api.user;

import ambitious.but.rubbish.api.ServWrapper;
import ambitious.but.rubbish.exceptions.EmailAlreadyExistsException;
import ambitious.but.rubbish.exceptions.UserAlreadyExistsException;
import ambitious.but.rubbish.lib.Utils;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "UserCreation")
public class UserCreation extends ServWrapper {
    /**
     * Processing for User creation POST request from frontend.
     *
     * @param request Incoming HTTP request
     * @param response Outgoing HTTP response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doRequestSessionless((connection) -> {
            JSONObject jsonOut = new JSONObject();
            try{
                JSONObject json = Utils.JSONFromPost(request);
                createAccount(connection, json);
                connection.commit();
                jsonOut.put("newUser", json.getString("username"));
            } catch(SQLException sqle){
                System.err.println("Error connecting: " + sqle);
                throw new httpError("Internal Server", 500);
            } catch(EmailAlreadyExistsException e){
                jsonOut.put("error", e.toString());
                throw new httpError("Email Already Exists", 400);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                throw new httpError("Hashing algorithm can't be found", 400);
            }
            return jsonOut;
        }, request, response);
    }

    /**
     * Inserts data from a json object into a database
     *
     * @param connection Connection to Database
     * @param json JSON Object to be Processed
     * @throws java.sql.SQLException Exception Coming from SQL Query
     */
    protected void createAccount(Connection connection, JSONObject json) throws java.sql.SQLException, EmailAlreadyExistsException, NoSuchAlgorithmException {
        Double weight = json.getDouble("weight");
        Double height = json.getDouble("height");
        String name = json.getString("username");
        String pwd = Utils.hashPassword(json.getString("password"));

        String email = json.getString("email");
        String age = json.getString("age");
        int type = json.getInt("typeid");
        int gender = json.getInt("gender");

        checkEmail(email, connection);

        String query = "INSERT INTO projectdata.users(weight, username, typeid, password, height, gender, email, age) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement st = connection.prepareStatement(query);
        st.setDouble(1, weight);
        st.setString(2, name);
        st.setInt(3, type);
        st.setString(4, pwd);
        st.setDouble(5, height);
        st.setInt(6, gender);
        st.setString(7, email);
        st.setString(8, age);

        st.executeUpdate();
    }

    /**
     * Checks if email is unique in the system
     * @param email
     * @param c
     * @throws SQLException
     * @throws EmailAlreadyExistsException
     */
    private void checkEmail(String email, Connection c) throws SQLException, EmailAlreadyExistsException {
        String query = "SELECT * FROM projectdata.users u WHERE u.email=?";
        PreparedStatement st = c.prepareStatement(query);
        st.setString(1, email);
        ResultSet rs = st.executeQuery();
        if (rs.next()){
            String error = "Email " + email + " already exists";
            throw new EmailAlreadyExistsException(error);
        }
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Not in Use
    }
}
