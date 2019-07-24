package ambitious.but.rubbish.api.user;

import ambitious.but.rubbish.api.ServWrapper;
import ambitious.but.rubbish.exceptions.EmailAlreadyExistsException;
import ambitious.but.rubbish.exceptions.IncorrectPasswordException;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

@WebServlet(name = "userData")
/**
 * Upload new personal data (weight, height etc.)
 */
public class userData extends ServWrapper {
    /**
     * Allows the user data to be changed. When changing the password a check is done that the old password is correct,
     * when changing email a check is done that the email is not yet used by a different user. All other data can be
     * changed without constraints
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doRequest((connection, id) -> {
            JSONObject json = Utils.JSONFromPost(request);
            try {
                if (json.has("email")){
                    updateEmail(id, json.getString("email"), connection);
                }
                if (json.has("username")){
                    updateName(id, json.getString("username"), connection);
                }
                if (json.has("weight")){
                    updateWeight(id, json.getDouble("weight"), connection);
                }
                if (json.has("height")){
                    updateHeight(id, json.getDouble("height"), connection);
                }
                if (json.has("age")){
                    updateAge(id, json.getString("age"), connection);
                }
                if (json.has("newPassword") && json.has("oldPassword")){
                    updatePassword(id, json.getString("newPassword"), json.getString("oldPassword"), connection);
                }
                if (json.has("typeid")) {
                    updatePremiumStatus(id, json.getInt("typeid"), connection);
                }
                connection.commit();
                return new JSONObject().put("message", "success");
            } catch(EmailAlreadyExistsException e){
                throw new httpError("Email already exists", 400);
            } catch (SQLException e) {
                throw new httpError("SQL Error", 400);
            } catch (NoSuchAlgorithmException e) {
                throw new httpError("Hashing algorithm does not exist", 400);
            } catch (IncorrectPasswordException e) {
                throw new httpError("Password is incorrect", 400);
            }
        }, request, response);
    }

    /**
     * Get usermetrics and average run data from the user
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doRequest((connection, id) -> {
            JSONObject jsonOut = new JSONObject();
            try {
                String query = "SELECT count(*) as runs, sum(distance) as distance, sum(time) as time, b.sacral_left, b.braking_left, b.pushoff_left, b.sacral_right, b.braking_right, b.pushoff_right\n" +
                        "FROM\n" +
                        "(SELECT sum(r.\"distance\"::float) as distance, sum(extract(HOUR FROM r.\"Time\") * 60 + extract(MINUTE FROM r.\"Time\")) as time\n" +
                        "FROM projectdata.users u, projectdata.run_data r\n" +
                        "WHERE r.\"Runner\" = u.uid\n" +
                        "AND u.uid = ?\n" +
                        "GROUP BY u.uid, r.\"Date\"\n" +
                        "ORDER BY r.\"Date\"\n" +
                        "LIMIT 3) as sub,\n" +
                        "\n" +
                        "projectdata.users u, projectdata.baselines b\n" +
                        "WHERE u.uid = b.runner_id\n" +
                        "GROUP BY b.sacral_left, b.braking_left, b.pushoff_left, b.sacral_right, b.braking_right, b.pushoff_right";
                PreparedStatement st = connection.prepareStatement(query);
                st.setInt(1, id);
                ResultSet rs = st.executeQuery();

                if (rs.next()) {
                    jsonOut.put("runAmount", rs.getInt("runs"));
                    jsonOut.put("totalDistance", rs.getDouble("distance"));
                    jsonOut.put("totalTime", rs.getInt("time"));

                    Double baseline[] = new Double[6];
                    baseline[0] = rs.getDouble("sacral_left");
                    baseline[1] = rs.getDouble("braking_left");
                    baseline[2] = rs.getDouble("pushoff_left");
                    baseline[3] = rs.getDouble("sacral_right");
                    baseline[4] = rs.getDouble("braking_right");
                    baseline[5] = rs.getDouble("pushoff_right");
                    jsonOut.put("baselines", baseline);
                }
            } catch (SQLException e) {
                throw new httpError("SQL Error", 400);
            }
            return jsonOut;
        }, request, response);
    }

    /**
     * Updates the users email, also checks if the new email is unique
     * @param user
     * @param email
     * @param c
     * @return
     * @throws SQLException
     */
    private void updateEmail(int user, String email, Connection c) throws SQLException, EmailAlreadyExistsException {
        String selectQuery = "SELECT email, uid FROM projectdata.users WHERE email = ?";
        PreparedStatement stSelect = c.prepareStatement(selectQuery);
        stSelect.setString(1, email);
        ResultSet rs = stSelect.executeQuery();
        if (rs.next()){
            if (rs.getInt("uid") != user){
                throw new EmailAlreadyExistsException("Email " + email + " already exists");
            }
        }

        String updateQuery = "UPDATE projectdata.users SET email = ? WHERE uid = ?";
        PreparedStatement stUpdate = c.prepareStatement(updateQuery);
        stUpdate.setString(1, email);
        stUpdate.setInt(2, user);
        stUpdate.executeUpdate();
        stUpdate.close();
    }

    /**
     * Updates the users name, also checks if the new username is unique
     * @param user
     * @param name
     * @param c
     * @return
     * @throws SQLException
     */
    private void updateName(int user, String name, Connection c) throws SQLException {
        String updateQuery = "UPDATE projectdata.users SET username = ? WHERE uid = ?";
        PreparedStatement stUpdate = c.prepareStatement(updateQuery);
        stUpdate.setString(1, name);
        stUpdate.setInt(2, user);
        stUpdate.executeUpdate();
        stUpdate.close();
    }

    /**
     * Updates the users weight
     * @param user
     * @param weight
     * @param c
     * @throws SQLException
     */
    private void updateWeight(int user, double weight, Connection c) throws SQLException {
        double roundedWeight = Math.round(weight * 10.0) / 10.0;

        String query = "UPDATE projectdata.users SET weight = ? WHERE uid = ?";
        PreparedStatement st = c.prepareStatement(query);
        st.setDouble(1, roundedWeight);
        st.setInt(2, user);
        st.executeUpdate();
        st.close();
    }

    /**
     * Updates the users height
     * @param user
     * @param height
     * @param c
     * @throws SQLException
     */
    private void updateHeight(int user, double height, Connection c) throws SQLException {
        double roundedHeight = Math.round(height * 100.0) / 100.0;

        String query = "UPDATE projectdata.users SET height = ? WHERE uid = ?";
        PreparedStatement st = c.prepareStatement(query);
        st.setDouble(1, roundedHeight);
        st.setInt(2, user);
        st.executeUpdate();
        st.close();
    }

    private void updateAge(int user, String age, Connection c) throws SQLException {
        String query = "UPDATE projectdata.users SET age = ? WHERE uid = ?";
        PreparedStatement st = c.prepareStatement(query);
        st.setString(1, age);
        st.setInt(2, user);
        st.executeUpdate();
    }

    private void updatePassword(int user, String newPassword, String oldPassword, Connection c) throws NoSuchAlgorithmException, SQLException, IncorrectPasswordException {
        String newHash = Utils.hashPassword(newPassword);
        String oldHash = Utils.hashPassword(oldPassword);

        String checkQuery = "SELECT password FROM projectdata.users WHERE uid = ?";
        PreparedStatement st = c.prepareStatement(checkQuery);
        st.setInt(1, user);
        ResultSet rs = st.executeQuery();
        if (rs.next()){
            if (rs.getString("password").equals(oldHash)){
                String updateQuery = "UPDATE projectdata.users SET password = ? WHERe uid = ?";
                PreparedStatement updateSt = c.prepareStatement(updateQuery);
                updateSt.setString(1, newHash);
                updateSt.setInt(2, user);
                updateSt.executeUpdate();
            }else{
                throw new IncorrectPasswordException("Password does not match");
            }
        }else{
            throw new IncorrectPasswordException("Password does not match");
        }
    }

    private void updatePremiumStatus(int user, int typeid, Connection c) throws SQLException{
        PreparedStatement statement = c.prepareStatement("UPDATE projectdata.users " +
                "SET typeid = ? " +
                "WHERE uid = ?");
        statement.setInt(1, typeid);
        statement.setInt(2, user);
        statement.executeUpdate();
    }
}
