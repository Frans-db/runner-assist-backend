package ambitious.but.rubbish.lib;

import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;

public class Utils {
    /**
     * Creates a JSON object populated with data from the servlet POST request
     *
     * @param request
     * @return
     * @throws java.io.IOException
     */

    public static JSONObject JSONFromPost(HttpServletRequest request) throws java.io.IOException{
        BufferedReader reader = request.getReader();
        String json = "";
        while (reader.ready()){
            String line = reader.readLine();
            if(line == null) break;
            else json += line;
        }
        reader.close();
        return new JSONObject(json);
    }

    /**
     * Creates a map populated with data from the servlet GET request
     *
     * @param request
     * @return
     */
    public static Map MapFromGET(HttpServletRequest request){
        String query = request.getQueryString();
        Map<String, String> result = new HashMap<>();
        if (query != null && !query.isEmpty()) {
            for (String param : query.split("&")) {
                String[] entry = param.split("=");
                result.put(entry[0], entry[1]);
            }
        }
        return result;
    }

    /**
     * Transforms an arraylist of doubles to an array of doubles
     *
     * @param values
     * @return
     */
    public static Double[] listToArray(ArrayList<Double> values){
        int size = values.size();
        Double[] result = new Double[size];
        for (int i = 0; i < size; i++){
            result[i] = values.get(i);
        }
        return result;
    }

    /**
     * Returns a hashed string of the password
     * @param password
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String hashPassword(String password) throws NoSuchAlgorithmException {
        String salt = "hjdfuit5weyuitew5yugeuoi4g";
        password += salt;
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(password.getBytes());
        byte[] bytes = md.digest();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++){
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    /**
     * Creates a map filled with data from a resultset
     * @param rs
     * @return A map with structure <String>, ArrayList<Object>
     * @throws SQLException
     */
    public static Map<String, ArrayList<Object>> mapFromResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        ArrayList<String> values = new ArrayList<>();
        for (int i = 1; i <= meta.getColumnCount(); i++){
            values.add(meta.getColumnName(i));
        }
        Map<String, ArrayList<Object>> result = new HashMap<>();
        for (String name : values){
            ArrayList<Object> temp = new ArrayList<>();
            result.put(name, temp);
        }

        while (rs.next()){
            for (String name : values){
                result.get(name).add(rs.getObject(name));
            }
        }
        return result;
    }

    /**
     * Implements the haversine formula to calculate the distance between 2 points in latitude and longitude
     * @param lat1
     * @param long1
     * @param lat2
     * @param long2
     * @return
     */
    public static Double gpsDistance(Double lat1, Double long1, Double lat2, Double long2){
        Double radius = 6378100.0;
        Double radLat1 = lat1 * (Math.PI / 180);
        Double radLat2 = lat2 * (Math.PI / 180);
        Double deltaLat = (lat2 - lat1) * (Math.PI / 180);
        Double deltaLong = (long2 - long1) * (Math.PI / 180);

        Double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                    Math.cos(radLat1) * Math.cos(radLat2) *
                            Math.sin(deltaLong / 2) * Math.sin(deltaLong / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return radius * c;
    }

    /**
     * Function used to estimate timestamps for runs. This is done by getting the total time of the run, and the total
     * amount of gps pings, then dividing this to get time per ping. For runs that are recorded correctly this value
     * is about ~1 ping per second. For every GPS ping we then multiply it's position by the time per ping to get an
     * estimation for about when the ping was done. The table is then updated to store this information.
     */
    public static void timeToGPS() {
        /*
        SELECT COUNT(*) cnt,
        to_timestamp(floor((extract('epoch' from time) / 30)) * 30)
        AT TIME ZONE 'UTC' as interval_alias,
        SUM(distance) AS distance,
(SUM(distance) / 30) * 3.6 as speed
        FROM




(SELECT time, id1, lat1, long1, id2, lat2, long2,
2 * 6378100.0 * asin(sqrt((sin(radians((lat2 - lat1) / 2))) ^ 2 + cos(radians(lat1)) * cos(radians(lat2)) * (sin(radians((long2 - long1) / 2))) ^ 2)) as distance


FROM
(SELECT time, lat as lat1, long as long1, "ID" as id1, lead(lat) over (order by "ID") as lat2, lead(long) over (order by "ID") as long2, lead("ID") over (order by "ID") as id2 FROM projectdata.gpsdata
ORDER BY "ID") as sub) as sub2

 GROUP BY interval_alias
        ORDER BY interval_alias ASC;
         */
        try {
            Class.forName("org.postgresql.Driver");
            DatabaseConnection dbAuto = new DatabaseConnection("di185");
            Connection connection = dbAuto.getConnection();
            connection.setAutoCommit(false);

            String query = "SELECT r.\"ID\", count(*) as pings, extract(HOUR FROM r.\"Time\") * 60 * 60 + extract(MINUTE FROM r.\"Time\") * 60 + extract(SECOND FROM r.\"Time\") as time, sub.startTime as startTime\n" +
                    "FROM projectdata.gpsdata g, projectdata.run_data r,\n" +
                    "(SELECT run, min(timestamp) as startTime FROM projectdata.stepdata GROUP BY run) as sub\n" +
                    "WHERE r.\"ID\" = g.run_id\n" +
                    "AND sub.run = r.\"ID\"\n" +
                    "GROUP BY r.\"ID\", r.\"Time\", sub.startTime\n" +
                    "ORDER BY r.\"ID\"";
            PreparedStatement st = connection.prepareStatement(query);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                int runId = rs.getInt("ID");
                int step = (int) Math.round(rs.getDouble("time") / rs.getDouble("pings") * 1000);
                Calendar cal = Calendar.getInstance();
                cal.setTime(rs.getTimestamp("startTime"));

                Calendar c = new GregorianCalendar();
                String gpsQuery = "SELECT \"ID\" FROM projectdata.gpsdata WHERE run_id = ? ORDER BY \"ID\" ASC";
                PreparedStatement gpsStatement = connection.prepareStatement(gpsQuery);
                gpsStatement.setInt(1, runId);
                ResultSet gpsResultSet = gpsStatement.executeQuery();

                while (gpsResultSet.next()) {
                    int gpsId = gpsResultSet.getInt("ID");
                    String updateQuery = "UPDATE projectdata.gpsdata SET time = ? WHERE \"ID\" = ?";
                    PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                    updateStatement.setTimestamp(1, new Timestamp(c.getTimeInMillis()));
                    updateStatement.setInt(2, gpsId);
                    updateStatement.executeUpdate();
                    c.add(Calendar.MILLISECOND, step);
                }


            }
            connection.commit();
            connection.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /**
     * Encodes input String into base64.
     *
     * @param str String to be Encoded
     * @return Encoded String
     */
    public static String encode64(byte[] str) {
        return Base64.getEncoder().encodeToString(str);
    }

    /**
     * Decodes base64 input to plain-text.
     *      *
     * @param encString Base64 Encoded Input
     * @return Decoded Plain-Text Returned in String Form
     */
    public static String decode64(String encString) {
        return new String(Base64.getDecoder().decode(encString));
    }

    /**
     * Convert a HttpServletRequest into String format.
     *
     * @param request Incoming HttpServletRequest
     * @return String Form of Incoming Request
     * @throws java.io.IOException Throws Exception if there is an input problem.
     */
    public static String stringFromPost(HttpServletRequest request) throws java.io.IOException{
        BufferedReader reader = request.getReader();
        String json = "";
        while (reader.ready()){
            json += reader.readLine();
        }
        return json;
    }

    /**
     * Converts a Map object to a String object. Was used for hashing user in database.
     *
     * @param in Map to be Hashed
     * @return String of value in Map
     */
    public static String mapToString(Map<String, Object> in) {
        StringBuilder res = new StringBuilder();
        for (Object obj :
                in.values()) {
            res.append(obj);
        }
        return res.toString();
    }

    /**
     * Function that returns a ResourceManager object since a resource cannot be called from a static context
     *
     * @return ResourceManager Instance
     */
    public KeyPair getRS() {
        ResourceManager rs = new ResourceManager(getClass().getResource("/server_credentials.txt"), new String[]{"type", "key"});
        KeyPairBuilder builder = new KeyPairBuilder(rs.get("public", "key"), rs.get("private", "key"));
        KeyPair keyPair = builder.generateKeyPair();
        return keyPair;
    }
}
