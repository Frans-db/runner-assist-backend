package ambitious.but.rubbish.api.user;

import ambitious.but.rubbish.api.ServWrapper;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// TODO Add Logout functionality
@WebServlet(name = "Logout")
public class Logout extends ServWrapper {
    /**
     * Method for logging out a user from dashboard
     *
     * @param request Request containing user to be logged out
     * @param response Response object fro server to respond to client
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doRequest((c, id) -> {
            try {
                String query = "DELETE FROM projectdata.tokens WHERE id = ?";
                PreparedStatement st = c.prepareStatement(query);
                st.setInt(1, id);
                st.executeUpdate();
                c.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return new JSONObject().put("Message", "Success");
        }, request, response);
    }
}
