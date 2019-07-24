package ambitious.but.rubbish.api.runs;

import ambitious.but.rubbish.api.ServWrapper;
import ambitious.but.rubbish.lib.DatabaseConnection;
import ambitious.but.rubbish.lib.Utils;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@WebServlet(name = "ambitious.but.rubbish.api.runs.RunGPS")
public class RunGPS extends ServWrapper {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    /**
     * Returns the maximum and minimum values of longitude and latitude (so we can scale the map) and all the gpx data
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doRequest((connection, id) -> {
            JSONObject jsonOut = new JSONObject();
            try{
                Map data = Utils.MapFromGET(request);
                int runId = Integer.valueOf((String) data.get("run_id"))     ;

                String query = "SELECT * FROM getGpsBoundaries(?, ?)";
                PreparedStatement st = connection.prepareStatement(query);
                st.setInt(1, id);
                st.setInt(2, runId);
                ResultSet rs = st.executeQuery();
                if (rs.next()){
                    jsonOut.put("longMax", rs.getDouble("max_long"));
                    jsonOut.put("longMin", rs.getDouble("min_long"));
                    jsonOut.put("latMax", rs.getDouble("max_lat"));
                    jsonOut.put("latMin", rs.getDouble("min_lat"));
                }

                String query2 = "SELECT gpx FROM projectdata.run_data WHERE \"ID\"= ? AND \"Runner\" = ?";
                PreparedStatement st2 = connection.prepareStatement(query2);
                st2.setInt(1, runId);
                st2.setInt(2, id);
                ResultSet rs2 = st2.executeQuery();
                if (rs2.next()){
                    jsonOut.put("gpx", rs2.getString("gpx"));
                }

            } catch (SQLException e) {
                throw new httpError("SQL Error", 400);
            }
            return jsonOut;
        }, request, response);
    }
}
