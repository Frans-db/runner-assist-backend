package ambitious.but.rubbish.api;

import ambitious.but.rubbish.lib.Utils;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

@WebServlet(name = "RemoveShoe")
public class RemoveShoe extends ServWrapper {
    /**
     * Removes the requested shoes link from database
     *
     * @param request Request containing user id and shoe id
     * @param response Response object for server to use
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doRequest((c, id) -> {
            JSONObject json = new JSONObject();
            Map<String, String> queries = Utils.MapFromGET(request);
            try {
                PreparedStatement statement = c.prepareStatement("DELETE FROM projectdata.user_shoes us " +
                        "WHERE us.uid = ?  " +
                        "AND us.shoe_id = ?");
                statement.setInt(1, id);
                statement.setInt(2, Integer.valueOf(queries.get("shoe_id")));
                statement.executeUpdate();
                c.commit();
                PreparedStatement statement1 = c.prepareStatement("DELETE FROM projectdata.shoe_types st " +
                        "WHERE st.sid = ?");
                statement1.setInt(1, Integer.valueOf(queries.get("shoe_id")));
                statement1.executeUpdate();
                c.commit();
                json.put("Success", "200 OK");
            } catch (SQLException e) {
                e.printStackTrace();
                throw new httpError("Internal Server Error", 500);
            }
            return json;
        }, request, response);
    }
}
