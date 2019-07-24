package ambitious.but.rubbish.api;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.postgresql.util.PSQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "ambitious.but.rubbish.api.Advice")
public class Advice extends ServWrapper {


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    /**
     * Returns a very basic advice based on the exhaustion rating we calculate for a run.
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doRequest((connection, id) -> {
            JSONObject jsonOut = new JSONObject();
            try {
                String sql = "SELECT \"ID\", \"Date\", distance FROM projectdata.run_data WHERE \"Runner\" = ? ORDER BY \"Date\" LIMIT 3";
                PreparedStatement st = connection.prepareStatement(sql);
                st.setInt(1, id);
                ResultSet rs = st.executeQuery();

                JSONArray advices = new JSONArray();
                while (rs.next()){
                    JSONObject runAdvice = new JSONObject();
                    int run_id = rs.getInt("ID");
                    runAdvice.put("id", run_id);
                    runAdvice.put("distance", rs.getDouble("distance"));
                    runAdvice.put("date", rs.getString("Date"));
                    Double exhaustion = calculateExhaustion(run_id, connection, null);
                    if (exhaustion < 1){
                        runAdvice.put("message", "Try pushing harder!");
                    }else if (exhaustion < 3){
                        runAdvice.put("message", "Keep going like this!");
                    }else {
                        runAdvice.put("message", "Don't push too hard!");
                    }
                    advices.put(runAdvice);
                }
                jsonOut.put("advices", advices);
                return jsonOut;
            } catch (SQLException e) {
                throw new httpError("SQL Error", 400);
            }
        }, request, response);
    }
    /**
     * Calculates the exhaustion rating for a run based on the users baselines, the baselines for this run and a
     * JSON config file with weights. The JSON file is structured like this:
     *  [
     *      {'key': 'pushoffpower', 'weights': {'stddev': 0.5, 'avg': 0.3}},
     *      {'key': 'valueName', 'weights': {'stddev': X, 'avg': Y}}
     *  ]
     * This config object can then be modified to change the exhaustion. A mapping function (to make sure the exhaustion
     * is always between 1 and X still has to be added).
     * @param id
     * @param connection
     * @param config
     * @return
     * @throws SQLException
     */
    private Double calculateExhaustion(int id, Connection connection, JSONObject config) throws SQLException {
        if (config == null){
            JSONParser parser = new JSONParser();
            try{
                FileReader reader = new FileReader(getClass().getResource("/adviceConfig.json").getFile());
                Object obj = parser.parse(reader);

                JSONObject jsonObject = new JSONObject(obj.toString());

                config = jsonObject;
                reader.close();
                //TODO: Error handling
            } catch (ParseException e) {

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Double exhaustion = 0.0;
        String runBaselinesQuery = "SELECT avg(axtibacc_right) as axtibacc_right_avg, stddev(axtibacc_right) as axtibacc_right_stddev, avg(axtibacc_left) as axtibacc_left_avg, stddev(axtibacc_left) as axtibacc_left_stddev, avg(tibimpact_right) as tibimpact_right_avg, stddev(tibimpact_right) as tibimpact_right_stddev, avg(tibimpact_left) as tibimpact_left_avg, stddev(tibimpact_left) as tibimpact_left_stddev, avg(axsacacc_right) as axsacacc_right_avg, stddev(axsacacc_right) as axsacacc_right_stddev, avg(axsacacc_left) as axsacacc_left_avg, stddev(axsacacc_left) as axsacacc_left_stddev, avg(sacimpact_right) as sacimpact_right_avg, stddev(sacimpact_right) as sacimpact_right_stddev, avg(sacimpact_left) as sacimpact_left_avg, stddev(sacimpact_left) as sacimpact_left_stddev, avg(brakingforce_right) as brakingforce_right_avg, stddev(brakingforce_right) as brakingforce_right_stddev, avg(brakingforce_left) as brakingforce_left_avg, stddev(brakingforce_left) as brakingforce_left_stddev, avg(pushoffpower_right) as pushoffpower_right_avg, stddev(pushoffpower_right) as pushoffpower_right_stddev, avg(pushoffpower_left) as pushoffpower_left_avg, stddev(pushoffpower_left) as pushoffpower_left_stddev, avg(tibintrot_right) as tibintrot_right_avg, stddev(tibintrot_right) as tibintrot_right_stddev, avg(tibintrot_left) as tibintrot_left_avg, stddev(tibintrot_left) as tibintrot_left_stddev, avg(vll_right) as vll_right_avg, stddev(vll_right) as vll_right_stddev, avg(vll_left) as vll_left_avg, stddev(vll_left) as vll_left_stddev " +
                "FROM projectdata.stepdata s, projectdata.run_data r " +
                "WHERE r.\"ID\" = ? " +
                "AND s.run = r.\"ID\"";
        PreparedStatement runBaselinesSt = connection.prepareStatement(runBaselinesQuery);
        runBaselinesSt.setInt(1, id);
        ResultSet runRs = runBaselinesSt.executeQuery();

        String userBaselinesQuery = "SELECT * FROM getRunAverage(?)";
        PreparedStatement userBaselinesSt = connection.prepareStatement(userBaselinesQuery);
        userBaselinesSt.setInt(1, id);
        ResultSet userRs = userBaselinesSt.executeQuery();


        if (runRs.next() && userRs.next()) {
            JSONArray configList = config.getJSONArray("config");
            String sides[] = {"left", "right"};
            String aggs[] = {"avg", "stddev"};
            for (int i = 0; i < configList.length(); i++) {
                JSONObject configItem = (JSONObject) configList.get(i);
                for (String side : sides) {
                    for (String agg : aggs) {
                        try {
                            String name = configItem.getString("key") + "_" + side + "_" + agg;
                            Double userValue = userRs.getDouble(name);
                            Double runValue = runRs.getDouble(name);
                            Double weight = configItem.getJSONObject("weights").getDouble(agg);
                            exhaustion += (userValue - runValue) * weight;
                        } catch (PSQLException e) {
                        }
                    }
                }
            }
        }
        return inverseSquareRootUnitActivation(exhaustion, 5.0  );
    }

    /**
     * Activation function to map value x to the range (-1/sqrt(a), 1/sqrt(a))
     * @param x
     * @return x mapped between two numbers
     */
    private Double inverseSquareRootUnitActivation(Double x, Double range){
        Double a = Math.pow(1 / range, 2);
        return x / Math.sqrt(1 + a * Math.pow(x, 2));
    }
}
