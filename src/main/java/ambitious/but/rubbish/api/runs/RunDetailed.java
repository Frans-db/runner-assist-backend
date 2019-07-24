package ambitious.but.rubbish.api.runs;


import ambitious.but.rubbish.api.ServWrapper;
import ambitious.but.rubbish.lib.DatabaseConnection;
import ambitious.but.rubbish.lib.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@WebServlet(name = "RunDetailed")
public class RunDetailed extends ServWrapper {
    /**
     * Allows for updating of the rating and description of a run the user has done
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doRequest((connection, id) -> {
        //DatabaseConnection dbAuto = new DatabaseConnection("di185");
//        Connection connection = dbAuto.getConnection();
//        int id = 90;

            try {
                JSONObject postData = Utils.JSONFromPost(request);
                int rating = postData.getInt("rating");
                if (rating > 5){
                    rating = 5;
                }
                if (rating < 0){
                    rating = 0;
                }
                String description = postData.getString("description");
                int run = postData.getInt("run_id");

                String query = "UPDATE projectdata.run_data  " +
                        "SET training_description = ?, rating = ? " +
                        "WHERE \"ID\" = ? " +
                        "AND \"Runner\" = ?";
                PreparedStatement st = connection.prepareStatement(query);
                st.setString(1, description);
                st.setInt(2, rating);
                st.setInt(3, run);
                st.setInt(4, id);
                st.executeUpdate();

                if (postData.has("shoe")){
                    String shoeQuery = "UPDATE projectdata.run_data SET \"Shoes\" = ? WHERE \"ID\"=? AND \"Runner\" =?";
                    PreparedStatement shoeSt = connection.prepareStatement(shoeQuery);
                    shoeSt.setInt(1, postData.getInt("shoe"));
                    shoeSt.setInt(2, run);
                    shoeSt.setInt(3, id);
                    shoeSt.executeUpdate();

                    String shoeUpdateQuery = "SELECT FROM updateusershoesdistance(?)";
                    PreparedStatement shoeUpdateSt = connection.prepareStatement(shoeUpdateQuery);
                    shoeUpdateSt.setInt(1, id);
                    shoeUpdateSt.executeQuery();
                }

                connection.commit();

            } catch (SQLException e) {
                System.out.println(e.toString());

                throw new httpError("SQL Error", 400);
            } catch (IOException e) {
                throw new httpError("IO Error", 400);
            }
            return new JSONObject().put("message", "success");
        }, request, response);
    }

    /**
     * Returns detailed information per run like:
     *  Speed over time (grouped together every 10 seconds of gps data)
     *  Distance over time (grouped together every 10 seconds of gps data)
     *  Average value of all step information. The average of 10 seconds of steps is taken and grouped together
     *  Run Description and rating
     * All of this data is then displayed on the detailed run overview
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
       this.doRequest((connection, id) -> {
           JSONObject jsonOut = new JSONObject();
            try {
                Map input = Utils.MapFromGET(request);
                int timeIncrement;
                try {
                     timeIncrement = Integer.valueOf((String) input.get("timeIncrement"));
                } catch(NullPointerException e) {
                    timeIncrement = 30;
                } catch (NumberFormatException e){
                    timeIncrement = 30;
                }
                int runId = Integer.valueOf((String) input.get("run_id"));
                String stepQuery = "SELECT * FROM getRunAverage(?, ?, ?)";
                PreparedStatement stepStatement = connection.prepareStatement(stepQuery);
                stepStatement.setInt(1, id);
                stepStatement.setInt(2, runId);
                stepStatement.setInt(3, timeIncrement);
                ResultSet stepResult = stepStatement.executeQuery();
                JSONObject stepData = stepDataFromRS(stepResult, timeIncrement);

                String gpsQuery = "SELECT * FROM getGpsAverage(?, ?, ?)";
                PreparedStatement gpsStatement = connection.prepareStatement(gpsQuery);
                gpsStatement.setInt(1, runId);
                gpsStatement.setInt(2, id);
                gpsStatement.setInt(3, timeIncrement);
                ResultSet gpsResult = gpsStatement.executeQuery();
                JSONObject gpsData = gpsDataFromRS(gpsResult, timeIncrement);
                jsonOut.put("data", stepData);
                jsonOut.put("gpsData", gpsData);

                String runQuery = "SELECT * FROM projectdata.run_data WHERE \"runid\" = ? AND \"Runner\" = ?";
                PreparedStatement runStatement = connection.prepareStatement(runQuery);
                runStatement.setInt(1, runId);
                runStatement.setInt(2, id);
                ResultSet runResult = runStatement.executeQuery();
                if (runResult.next()){
                    jsonOut.put("rating", runResult.getInt("rating"));
                    jsonOut.put("description", runResult.getString("training_description"));
                    jsonOut.put("shoe", runResult.getInt("shoes"));
                }

                JSONObject config = new JSONObject();
                ArrayList<JSONObject> list = new ArrayList<>();
                list.add(new JSONObject().put("test", 2));
                config.put("config", list);
            } catch (SQLException e) {
                throw new httpError("SQL Error", 400);
            } catch(NullPointerException e){
                throw new httpError("NullpointerException", 400);
            }
            return jsonOut;
        }, request, response);
    }

    /**
     * Creates a JSONObject populated with stepdata from a resultset
     * @param rs the ResultSet from the stepdata query
     * @param timeIncrement The time amount stepdata is grouped by
     * @return JSONObject with format
     *          data: [
     *              key
     *          ]
     * @throws SQLException
     */
    private JSONObject stepDataFromRS(ResultSet rs, int timeIncrement) throws SQLException {
        Map stepMap = Utils.mapFromResultSet(rs);
        JSONObject stepData = new JSONObject();
        String[] sides = {"left", "right"};

        Set<String> columnNames = new HashSet<>();
        ResultSetMetaData meta = rs.getMetaData();
        for (int i = 3; i < meta.getColumnCount(); i++){
            String columnName = meta.getColumnName(i).split("_")[0];
            columnNames.add(columnName);
        }
        for (String columnName : columnNames){
            JSONObject columnObject = new JSONObject();
            for (String side : sides){
                String key = columnName + "_" + side;
                ArrayList keyList = (ArrayList) stepMap.get(key);
                columnObject.put(side, keyList);
            }
            stepData.put(columnName, columnObject);
        }

        ArrayList<Integer> jsonTimes = new ArrayList<>();
        ArrayList mapTimes = (ArrayList) stepMap.get("run_times");
        for (int i = 0; i < mapTimes.size(); i++){
            jsonTimes.add(i * timeIncrement);
        }
        stepData.put("times", jsonTimes);

        return stepData;
    }

    /**
     * Returns a JSONObject populated with speed and distance over time
     * @param rs the resultset
     * @param timeIncrement
     * @return
     * @throws SQLException
     */
    private JSONObject gpsDataFromRS(ResultSet rs, int timeIncrement) throws SQLException {
        Map gpsMap = Utils.mapFromResultSet(rs);
        JSONObject gpsData = new JSONObject();
        ArrayList distanceList = (ArrayList) gpsMap.get("distance");
        ArrayList speedList = (ArrayList) gpsMap.get("speed");

        ArrayList<Integer> jsonTimes = new ArrayList<>();
        ArrayList mapTimes = (ArrayList) gpsMap.get("gps_time");
        for (int i = 0; i < mapTimes.size(); i++){
            jsonTimes.add(i * timeIncrement);
        }
        gpsData.put("times", jsonTimes);

        gpsData.put("distance", distanceList);
        gpsData.put("speed", speedList);
        return gpsData;
    }
}
