package ambitious.but.rubbish.lib;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

public class DataProcessing {
    /**
     * Calculates the pearson correlation coefficient of 2 arrays of doubles
     *
     * @param x
     * @param y
     * @return
     */
    public static Double pearsonCorrelation(Double[] x, Double[] y){
        int xSize = x.length;
        int ySize = y.length;
        Double[] xNormal = normalDistribution(x);
        Double[] yNormal = normalDistribution(y);

        Double cov = 0.0;
        Double denom = Math.sqrt(xNormal[1] * yNormal[1]);
        for (int i = 0; i < xSize; i++){
            cov += (x[i] - xNormal[0]) * (y[i] - yNormal[0]);
        }

        return cov / denom;
    }

    /**
     * Calculates a mean and standard deviation from an array of doubles
     *
     * @param values
     * @return
     */
    public static Double[] normalDistribution(Double[] values){
        Double mean = 0.0;
        Double stdev = 0.0;
        int size = values.length;
        for (int i = 0; i < size; i++){
            mean += values[i];
        }
        mean /= size;
        for (int i = 0; i < size; i++){
            stdev += Math.pow(mean - values[i], 2);
        }
        stdev /= (size - 1);
        Double[] result = {mean, stdev};

        return result;
    }

    /**
     * Calculates a mean and standard deviation from an arraylist of doubles
     *
     * @param values
     * @return
     */
    public static Double[] normalDistribution(ArrayList<Double> values){
        return normalDistribution(Utils.listToArray(values));
    }

    public static ResultSet calculateBaselineForUser(Connection c, int uid){
        //TODO: Decide amount of runs needed for baseline
        //TODO: Make this a database function >:(
        //TODO: Turn this into a simpler sql query when this function is moved to the database
        String query = "SELECT count(*), avg(s.axtibacc_right) as axtibacc_right, avg(s.axtibacc_left) as axtibacc_left, avg(s.tibimpact_right) as tibimpact_right, avg(s.tibimpact_left) as tibimpact_left, avg(s.axsacacc_right) as axsacacc_right, avg(s.axsacacc_left) as axsacacc_left, avg(s.sacimpact_right) as sacimpact_right, avg(s.sacimpact_left) as sacimpact_left, avg(s.brakingforce_right) as brakingforce_right, avg(s.brakingforce_left) as brakingforce_left, avg(s.pushoffpower_right) as pushoffpower_right, avg(s.pushoffpower_left) as pushoffpower_left, avg(s.tibintrot_right) as tibintrot_right, avg(s.tibintrot_left) as tibintrot_left, avg(s.vll_right) as vll_right, avg(s.vll_left) as vll_left\n" +
                "\n" +
                "FROM projectdata.run_data r, projectdata.stepdata s, projectdata.users u\n" +
                "WHERE r.\"ID\" = s.run\n" +
                "AND r.\"Runner\" = u.uid\n" +
                "AND u.uid = ?\n" +
                "GROUP BY u.uid ";
        return null;
    }

    public static ResultSet calculateBaselineForRun(Connection c, int run_id) throws SQLException {
        String query = "SELECT avg(axtibacc_right) as axtibacc_right_avg, stddev(axtibacc_right) as axtibacc_right_stddev, avg(axtibacc_left) as axtibacc_left_avg, stddev(axtibacc_left) as axtibacc_left_stddev, avg(tibimpact_right) as tibimpact_right_avg, stddev(tibimpact_right) as tibimpact_right_stddev, avg(tibimpact_left) as tibimpact_left_avg, stddev(tibimpact_left) as tibimpact_left_stddev, avg(axsacacc_right) as axsacacc_right_avg, stddev(axsacacc_right) as axsacacc_right_stddev, avg(axsacacc_left) as axsacacc_left_avg, stddev(axsacacc_left) as axsacacc_left_stddev, avg(sacimpact_right) as sacimpact_right_avg, stddev(sacimpact_right) as sacimpact_right_stddev, avg(sacimpact_left) as sacimpact_left_avg, stddev(sacimpact_left) as sacimpact_left_stddev, avg(brakingforce_right) as brakingforce_right_avg, stddev(brakingforce_right) as brakingforce_right_stddev, avg(brakingforce_left) as brakingforce_left_avg, stddev(brakingforce_left) as brakingforce_left_stddev, avg(pushoffpower_right) as pushoffpower_right_avg, stddev(pushoffpower_right) as pushoffpower_right_stddev, avg(pushoffpower_left) as pushoffpower_left_avg, stddev(pushoffpower_left) as pushoffpower_left_stddev, avg(tibintrot_right) as tibintrot_right_avg, stddev(tibintrot_right) as tibintrot_right_stddev, avg(tibintrot_left) as tibintrot_left_avg, stddev(tibintrot_left) as tibintrot_left_stddev, avg(vll_right) as vll_right_avg, stddev(vll_right) as vll_right_stddev, avg(vll_left) as vll_left_avg, stddev(vll_left) as vll_left_stddev " +
                "FROM projectdata.run_data r, projectdata.stepdata s " +
                "WHERE r.\"ID\" = s.run " +
                "AND r.\"ID\" = ? " +
                "GROUP BY r.\"ID\"";
        PreparedStatement st = c.prepareStatement(query);
        st.setInt(1, run_id);

        return st.executeQuery();
    }

    public static void calculateExhaustion(Connection c, int run_id, int uid, JSONObject config) throws SQLException {
        Double exhaustion = 0.0;
        ResultSet runBaselines = calculateBaselineForRun(c, run_id);
        ResultSet userBaselines = calculateBaselineForUser(c, uid);

        if (runBaselines.next() && userBaselines.next()){
            JSONArray configArray = (JSONArray) config.get("config");
            for (int i = 0; i < configArray.length(); i++){
                JSONObject configOption = (JSONObject) configArray.get(i);
                String key = configOption.getString("key");

                JSONObject meanConfig = configOption.getJSONObject("mean");
                Double meanDifference = runBaselines.getDouble(key + "_mean") - userBaselines.getDouble(key + "_mean");
                Double meanExhaustion = calculateExhaustionPerKey(meanDifference, meanConfig);

                JSONObject stddevConfig = configOption.getJSONObject("stddev");
                Double stddevDifference = runBaselines.getDouble(key + "_stddev") - userBaselines.getDouble(key + "_stddev");
                Double stddevExhaustion = calculateExhaustionPerKey(stddevDifference, stddevConfig);

                Double keyExhaustion = meanExhaustion + stddevExhaustion;
                exhaustion += keyExhaustion;
            }
        }
    }

    public static Double calculateExhaustionPerKey(Double difference, JSONObject config){
        String diffType = "";
        Double exhaustion = 0.0;

        if (difference > 0){
            diffType = "incr";
        }else{
            diffType = "decr";
        }
        if (config.getString("type") .equals("abs")){
            exhaustion += config.getDouble(diffType) * difference;
        }else if(config.getString("type").equals("perc")){

        }

        return 0.0;
    }
}
