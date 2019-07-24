package ambitious.but.rubbish.api.runs;

import ambitious.but.rubbish.api.ServWrapper;
import ambitious.but.rubbish.lib.DatabaseConnection;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@WebServlet(name = "RunDashboard")
public class RunDashboard extends ServWrapper {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    /**
     * Returns some basic data for hte last N (right now 4) runs the user has done. These are then displayed on the
     * cards on the main dashboard.
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doRequest((connection, id) -> {
            JSONObject jsonOut = new JSONObject();
            try {
                String query = "SELECT r.\"ID\", (extract(HOUR FROM r.\"Time\") * 60 + extract(MINUTE FROM r.\"Time\")) / r.\"distance\"::FLOAT as pace, r.\"Date\"\n" +
                        "FROM projectdata.run_data r\n" +
                        "WHERE r.\"Runner\" = ?\n" +
                        "ORDER BY r.\"Date\" DESC\n" +
                        "LIMIT 4";
                PreparedStatement st = connection.prepareStatement(query);
                st.setInt(1, id);
                ResultSet rs = st.executeQuery();

                ArrayList<JSONObject> jsonList = new ArrayList<>();
                while (rs.next()) {
                    JSONObject jsonInner = new JSONObject();
                    jsonInner.put("pace", rs.getDouble("pace"));
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
