package ambitious.but.rubbish.api;

import ambitious.but.rubbish.lib.DatabaseConnection;
import ambitious.but.rubbish.lib.Utils;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@WebServlet(name = "Calendar")
public class Calendar extends ServWrapper {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    /**
     * Generates basic run data (ID, distance, time and date) for every run, sorted by date.
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doRequest((connection, id) -> {
            JSONObject jsonOut = new JSONObject();
            try {
                String query = "SELECT \"ID\", \"distance\", \"Time\", \"Date\"\n" +
                        "FROM projectdata.run_data r\n" +
                        "WHERE \"Runner\" = ? " +
                        "ORDER BY \"Date\" DESC";
                PreparedStatement st = connection.prepareStatement(query);
                st.setInt(1, id);
                ResultSet rs = st.executeQuery();
                ArrayList<JSONObject> jsonList = new ArrayList<>();
                while (rs.next()) {
                    JSONObject jsonInner = new JSONObject();
                    jsonInner.put("id", rs.getInt("ID"));
                    jsonInner.put("distance", rs.getDouble("distance"));
                    jsonInner.put("time", rs.getTime("Time"));
                    jsonInner.put("date", rs.getDate("Date"));
                    jsonList.add(jsonInner);
                }
                jsonOut.put("runs", jsonList);


            } catch (SQLException e) {
                throw new httpError("SQL Error", 400);
            }

            return jsonOut;
        }, request, response);
    }
}
