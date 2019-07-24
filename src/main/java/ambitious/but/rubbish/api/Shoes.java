package ambitious.but.rubbish.api;

import ambitious.but.rubbish.lib.Utils;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "Shoes")
public class Shoes extends ServWrapper {

    /**
     * Processes adding a shoe and linking it to a the user
     *
     * @param request Request containing the shoe parameters in JSON format
     * @param response Response object to inform client if successful or not
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doRequest((c, id) -> {
            JSONObject json = Utils.JSONFromPost(request);
            try {
                PreparedStatement stmnt = c.prepareStatement("INSERT INTO projectdata.shoe_types (brand, model, height_heel, height_forefoot, \"drop\", weight)" +
                        "VALUES (?, ?, ?, ?, ?, ?)");
                stmnt.setString(1,(String) json.get("brand"));
                stmnt.setString(2,(String) json.get("model"));
                stmnt.setInt(3, (int) json.get("height_heel"));
                stmnt.setInt(4, (int) json.get("height_forefoot"));
                stmnt.setInt(5, (int) json.get("drop"));
                stmnt.setInt(6, (int) json.get("weight"));
                stmnt.executeUpdate();
                PreparedStatement statement = c.prepareStatement("SELECT st.sid " +
                        "FROM projectdata.shoe_types st " +
                        "WHERE st.model = ?");
                statement.setString(1, (String) json.get("model"));
                ResultSet rs = statement.executeQuery();
                PreparedStatement stmnt3 = c.prepareStatement("INSERT INTO projectdata.user_shoes (uid, shoe_id) " +
                        "VALUEs (?, ?)");
                if (rs.next()) {
                    stmnt3.setInt(1, id);
                    stmnt3.setInt(2, rs.getInt("sid"));
                    stmnt3.executeUpdate();
                    c.commit();
                } else {
                    throw new httpError("Failed to add shoes to database", 500);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            JSONObject jout = new JSONObject();
            jout.put("Success", 200);
            return jout;
        }, request, response);
    }

    /**
     * Method for getting either all shoes or specific shoe tied to user
     *
     * @param request Request object containing specific shoe to be added
     * @param response Response object used to send the data to client
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doRequest((c, id) -> {
            Map<String, String> queries = Utils.MapFromGET(request);
            JSONObject json = new JSONObject();
            try {
                PreparedStatement statement = c.prepareStatement("SELECT * " +
                        "FROM di185.getusershoedata(?)");
                statement.setInt(1, id);
                ResultSet rs = statement.executeQuery();
                List<Map<String, Object>> resutlSet = parseQuery(rs);
                if (!queries.isEmpty()) {
                    for (Map<String, Object> row :
                            resutlSet) {
                        if (row.get("model").equals(queries.get("model").replace("%20", " "))) {
                            json.put((String) row.get("model"), new JSONObject(row));
                        }
                    }
                } else {
                    for (Map<String, Object> row :
                            resutlSet) {
                        json.put((String) row.get("model"), new JSONObject(row));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return json;
        }, request, response);
    }


    /**
     * Helper method to convert result set of multiple rows to and array list of maps
     *
     * @param rs ResultSet to be converted
     * @return The corresponding array list of maps
     */
    public static List<Map<String, Object>> parseQuery(ResultSet rs) {
        List<String> columns = new ArrayList<>();
        try {
            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                columns.add(meta.getColumnName(i));
            }
            List<Map<String, Object>> result = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> shoe = new HashMap<>();
                for (int i = 0; i < columns.size(); i++) {
                    shoe.put(columns.get(i), rs.getObject(columns.get(i)));
                }
                result.add(shoe);
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
