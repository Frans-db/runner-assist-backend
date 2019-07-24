package ambitious.but.rubbish.api;

import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "WeekSummary")
public class WeekSummary extends ServWrapper {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    /**
     * Returns the average data of all runs this week to display as a week summary. The data is total time ran and
     * total distance ran
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doRequest((connection, id) -> {
            JSONObject jsonOut = new JSONObject();
            try {
                String query = "SELECT SUM(distance) as distance, EXTRACT(HOURS FROM \"Time\") + EXTRACT(MINUTES FROM \"Time\") / 60 as time, EXTRACT(WEEK FROM \"Date\") AS week\n" +
                        "FROM projectdata.run_data\n" +
                        "WHERE EXTRACT(WEEK FROM \"Date\") = EXTRACT(WEEK FROM CURRENT_DATE)\n AND \"Runner\" = ? " +
                        "GROUP BY week, \"Time\"\n";
                PreparedStatement st = connection.prepareStatement(query);
                st.setInt(1, id);
                ResultSet rs = st.executeQuery();

                if (rs.next()){
                    jsonOut.put("distance", rs.getObject("distance"));
                    jsonOut.put("time", rs.getObject("time"));
                    jsonOut.put("week", rs.getObject("week"));
                }
                return jsonOut;
            } catch (SQLException e) {
                throw new httpError("SQL Error", 400);
            }
        }, request, response);
    }
}
