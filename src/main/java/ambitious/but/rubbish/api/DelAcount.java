package ambitious.but.rubbish.api;

import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@WebServlet(name = "DelAccount")
public class DelAcount extends ServWrapper {
    /**
     * Removes the given user.
     *
     * @param request Deletion request containing the id in the headers
     * @param response Response saying whether success or failure
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doRequest((c, id) -> {
            JSONObject jsonObject = new JSONObject();
            try {
                PreparedStatement deletSession = c.prepareStatement("DELETE FROM projectdata.tokens t " +
                        "WHERE t.id = ?");
                deletSession.setInt(1, id);
                deletSession.executeUpdate();
                PreparedStatement deletUser = c.prepareStatement("DELETE FROM projectdata.users u " +
                        "WHERE u.uid = ?");
                deletUser.setInt(1, id);
                deletUser.executeUpdate();
                deleteShoes(id, c);
                deleteRuns(id, c);
                c.commit();
                jsonObject.put("message", "Success");
            } catch (SQLException e) {
                e.printStackTrace();
                throw new httpError("Internal Server Error", 500);
            }
            return jsonObject;
        }, request, response);
    }

    /**
     * Deletes all shoes linked to user
     *
     * @param id User id
     * @param c Connection to database
     * @throws httpError Errors out on SQL error
     */
    private void deleteShoes(int id, Connection c) throws httpError{
        try {
            PreparedStatement getLinkedShoes = c.prepareStatement("SELECT us.shoe_id " +
                    "FROM projectdata.user_shoes us " +
                    "WHERE us.uid = ?");
            getLinkedShoes.setInt(1, id);
            ResultSet rs = getLinkedShoes.executeQuery();
            ArrayList<Integer> shoes = getId(rs, "shoe_id");
            PreparedStatement deleteShoeLink = c.prepareStatement("DELETE FROM projectdata.user_shoes us " +
                    "WHERE us.uid = ?");
            deleteShoeLink.setInt(1, id);
            PreparedStatement deleteShoe = c.prepareStatement("DELETE FROM projectdata.shoe_types st " +
                    "WHERE st.sid = ?");
            for (Integer shoe :
                    shoes) {
                deleteShoe.setInt(1, shoe);
                deleteShoe.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new httpError("Internal Server Error", 500);
        }
    }

    /**
     * Deletes all run and stepdata linked to the user
     *
     * @param id User id
     * @param c Connection to database
     * @throws httpError Errors out on SQL error
     */
    private void deleteRuns(int id, Connection c) throws httpError {
        try {
            PreparedStatement getRuns = c.prepareStatement("SELECT rd.\"ID\" " +
                    "FROM projectdata.run_data rd " +
                    "WHERE rd.\"Runner\" = ?");
            getRuns.setInt(1, id);
            ResultSet rs = getRuns.executeQuery();
            ArrayList<Integer> runs = getId(rs, "ID");
            PreparedStatement deleteGps = c.prepareStatement("DELETE FROM projectdata.gpsdata gps " +
                    "WHERE gps.run_id = ?");
            for (Integer run :
                    runs) {
                deleteGps.setInt(1, run);
                deleteGps.executeUpdate();
            }
            PreparedStatement deletRun = c.prepareStatement("DELETE FROM projectdata.run_data rd " +
                    "WHERE rd.\"Runner\" = ?");
            deletRun.setInt(1, id);
            PreparedStatement deleteStep = c.prepareStatement("DELETE FROM projectdata.stepdata s " +
                    "WHERE s.run = ?");
            for (Integer run :
                    runs) {
                deleteStep.setInt(1, run);
                deleteGps.executeUpdate();
            }
            deletRun.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new httpError("Internal Server Error", 500);
        }
    }

    /**
     * Gets all the values of one column in the database
     *
     * @param rs ResultSet of SQL query
     * @param colName Column Name of the desired column
     * @return An array list of all the values in that column
     * @throws httpError Errors on SQL error
     */
    private ArrayList<Integer> getId(ResultSet rs, String colName) throws httpError{
        ArrayList<Integer> res = new ArrayList<>();
        try {
            while (rs.next()) {
                res.add(rs.getInt(colName));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new httpError("Internal Server Erro", 500);
        }
        return res;
    }
}