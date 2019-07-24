package ambitious.but.rubbish.api.runs;

import ambitious.but.rubbish.api.ServWrapper;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Arrays;
import java.util.List;

@MultipartConfig
@WebServlet(name = "AddRun")
public class AddRun extends ServWrapper {
    /**
     * Method for handling parsing of new run in the form of a gpx file
     *
     * @param request Request from client containing
     * @param response Response containing the success state of the method
     * @throws ServletException Thrown if the error with the file extraction from request
     * @throws IOException Thrown if there is a problem with parsing the file
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doRequest((c, id) -> {
            JSONObject json = new JSONObject();
            try {
                Part gpxFile = request.getPart("gpx");
                Part csvFile = request.getPart("cvs");
                String gpxName = gpxFile.getSubmittedFileName();
                String csvName = csvFile.getSubmittedFileName();
                InputStream gpx = gpxFile.getInputStream();
                InputStream csv = csvFile.getInputStream();
                BufferedReader brGPX = new BufferedReader(new InputStreamReader(gpx));
                BufferedReader brCSV = new BufferedReader(new InputStreamReader(csv));
                try {
                    makeRun(brGPX, c, id);
                    c.commit();
                    addStep(brCSV, c, id);
                    c.commit();
                    json.put("Added Run", csvName);
                    json.put("GPS Added", gpxName);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (ServletException e) {
                e.printStackTrace();
            }
            return json;
        }, request, response);
    }

    /**
     * Adds run to the run_data table in database
     *
     * @param file BufferedReader from file
     * @param c Connection to database
     * @param id User id
     * @throws httpError Throws error if anything goes wrong
     */
    private void makeRun(BufferedReader file, Connection c, int id) throws IOException, httpError {
        BufferedReader br = file;
        String line;
        StringBuilder res = new StringBuilder();
        while ((line = br.readLine()) != null) {
            res.append(line + "\n");
        }
        try {
            PreparedStatement statement = c.prepareStatement("INSERT INTO projectdata.run_data (\"Runner\", gpx) " +
                    "VALUES (?, ?)");
            statement.setInt(1, id);
            SQLXML xmlString = c.createSQLXML();
            xmlString.setString(res.toString());
            statement.setSQLXML(2, xmlString);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new httpError("Internal Server Error", 500);
        }
    }

    /**
     * Adds the step data to the database
     *
     * @param file BufferedReader from file
     * @param c Connection to database
     * @param id User id
     * @throws httpError Throws error if anything goes wrong
     */
    private void addStep(BufferedReader file, Connection c, int id) throws  IOException, httpError {
        BufferedReader br = file;
        List<String> columns = Arrays.asList(br.readLine().split(","));
        try {
            PreparedStatement getRunId = c.prepareStatement("SELECT rd.\"ID\" " +
                    "FROM projectdata.run_data rd " +
                    "WHERE rd.\"Runner\" = ? " +
                    "ORDER BY rd.\"ID\" DESC");
            getRunId.setInt(1, id);
            ResultSet rs = getRunId.executeQuery();
            int run_id = rs.getInt("ID");
            PreparedStatement insertStep = c.prepareStatement("INSERT INTO projectdata.stepdata (step, run, surface, axtibacc_right, tibimpact_right, axsacacc_right, sacimpact_right, brakingforce_right, pushoffpower_right, tibintrot_right, vll_right, axtibacc_left, tibimpact_left, axsacacc_left, sacimpact_left, brakingforce_left, pushoffpower_left, tibintrot_left, vll_left, \"IC_right\", to_right, ic_left, to_left, \"timestamp\", runner_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                insertStep.setInt(1, Integer.valueOf(values[columns.indexOf("step")]));
                insertStep.setInt(2, run_id);
                insertStep.setInt(3, Integer.valueOf(values[columns.indexOf("surface")]));
                insertStep.setDouble(4, Double.valueOf(values[columns.indexOf("axtibacc_right")]));
                insertStep.setDouble(5, Double.valueOf(values[columns.indexOf("tibimpact_right")]));
                insertStep.setDouble(6, Double.valueOf(values[columns.indexOf("axsacacc_right")]));
                insertStep.setDouble(7, Double.valueOf(values[columns.indexOf("sacimpact_right")]));
                insertStep.setDouble(8, Double.valueOf(values[columns.indexOf("brakingforce_right")]));
                insertStep.setDouble(9, Double.valueOf(values[columns.indexOf("pushoffpower_right")]));
                insertStep.setDouble(10, Double.valueOf(values[columns.indexOf("tibintrot_right")]));
                insertStep.setDouble(11, Double.valueOf(values[columns.indexOf("vll_right")]));
                insertStep.setDouble(12, Double.valueOf(values[columns.indexOf("axtibacc_left")]));
                insertStep.setDouble(13, Double.valueOf(values[columns.indexOf("tibimpact_left")]));
                insertStep.setDouble(14, Double.valueOf(values[columns.indexOf("axsacacc_left")]));
                insertStep.setDouble(15, Double.valueOf(values[columns.indexOf("sacimpact_left")]));
                insertStep.setDouble(16, Double.valueOf(values[columns.indexOf("brakingforce_left")]));
                insertStep.setDouble(17, Double.valueOf(values[columns.indexOf("pushoffpower_left")]));
                insertStep.setDouble(18, Double.valueOf(values[columns.indexOf("tibintrot_left")]));
                insertStep.setDouble(19, Double.valueOf(values[columns.indexOf("vll_left")]));
                insertStep.setString(20, values[columns.indexOf("ic_right")]);
                insertStep.setString(21, values[columns.indexOf("to_right")]);
                insertStep.setString(22, values[columns.indexOf("ic_left")]);
                insertStep.setString(23, values[columns.indexOf("to_left")]);
                insertStep.setTimestamp(24, Timestamp.valueOf(values[columns.indexOf("time")]));
                insertStep.setInt(25, id);
                insertStep.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new httpError("Internal Server Error", 500);
        }
    }
}