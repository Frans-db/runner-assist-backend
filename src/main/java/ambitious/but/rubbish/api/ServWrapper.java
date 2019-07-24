package ambitious.but.rubbish.api;

import ambitious.but.rubbish.lib.DatabaseConnection;
import ambitious.but.rubbish.lib.JWT;
import ambitious.but.rubbish.lib.Signer;
import org.joda.time.DateTime;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "ServletWrapper")
public abstract class ServWrapper extends HttpServlet {
    private Map<String, Object> headers;

    /**
     * Verifies the incoming request is from the client and is recent
     *
     * @param time Current
     * @throws httpError If error in authorization throws a 401 error
     */
    private void verifyOrigin(DateTime time) throws httpError{
        Signer signer = new Signer("+" + Long.valueOf((String) headers.get("date-time")) + "+");
        // Verify request is recent
        if (time.plusSeconds(90).isBeforeNow()) {
            throw new httpError("Response is too old", 401);
        }
        // Verify Client Signature
        if (!signer.verifyKey((String) headers.get("app-key"))) {
            throw new httpError("Validation failed", 401);
        }
    }

    /**
     * Gather the header data from the request to the servlet.
     *
     * @param request The Request Made to the Servlet
     * @return Returns the Required Security Headers in Map Object
     */
    private Map<String, Object> authHeaders(HttpServletRequest request) throws httpError{
        Map<String, Object> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String elem = headerNames.nextElement();
            String[] values = {"app-key", "session-token", "date-time"};
            if (Arrays.asList(values).contains(elem)) {
                headers.put(elem, request.getHeader(elem));
            }
        }
        if (headers.size() < 3) {
            if (headers.containsKey("session-token")) {
                throw new httpError("Security headers missing", 401);
            }
        }
        return headers;
    }

    /**
     * Authenticates requests made to servlets.
     *
     * @param request The Incoming Request Object
     * @param c The DataBaseConnection Instance of the Servlet
     * @return Either the ID of the user that made the request or -1 if the authentication failed.
     */
    private int authRequest(HttpServletRequest request, Connection c) throws httpError {
        // Checks if all headers are present, null means they are not
        if (!headers.containsKey("session-token")) {
            throw new httpError("Security headers missing", 401);
        }
        // Variable Setup
        Map<String, Object> jwt = new JWT((String) headers.get("session-token")).claims();
        DateTime time = new DateTime(Long.valueOf((String) headers.get("date-time")));
        verifyOrigin(time);
        // Try/Catch statement ensures errors force a re-log
        try {
            Statement stmt = c.createStatement();
            // Checks whether the given user id has a session open
            ResultSet rs = stmt.executeQuery("SELECT * " +
                    "FROM projectdata.tokens t " +
                    "WHERE t.id = '" + jwt.get("id") + "'");
            // Checks if id exists in Database
            if (rs.next()) {
                // Checks if the corresponding session key is correct
                if (!rs.getString("sessionkey").equals(jwt.get("ssk"))) {
                    throw new httpError("Session key is incorrect", 401);
                }
            } else {
                throw new httpError("Session does not exist", 401);
            }
            // Checks if session is still active
            if (time.isAfter(rs.getLong("expires_on"))) {
                throw new httpError("Session has already expired", 401);
            }
            rs.close();
            return (int) jwt.get("id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new httpError("SQL error occurred", 500);
    }

    /**
     * Session-ed request wrapper for handling response wrapping
     *
     * @param _request Interface for lambda function for code passthroughs
     * @param request Request object from the wrapped servlet
     * @param response Response object from the wrapped servlet
     */
    protected void doRequest(httpRequest _request, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        DatabaseConnection dbAuto = new DatabaseConnection("di185");
        Connection connection = dbAuto.getConnection();
        PrintWriter out = response.getWriter();
        try {
            headers = authHeaders(request);
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "POST");
            response.setHeader("Access-Control-Allow-Headers", "*");
            response.setHeader("Access-Control-Expose-Headers", "date-time, app-key");
            response.setContentType("application/json");
            int id = authRequest(request, connection);
            long time = System.currentTimeMillis();
            PreparedStatement statement = connection.prepareStatement("UPDATE projectdata.tokens " +
                    "SET expires_on = " + (time + 1800000) + " " +
                    "WHERE id = " + id);
            statement.executeUpdate();
            connection.commit();
            Signer signer = new Signer("+" + time + "+");
            response.setHeader("app-key", signer.signKey());
            response.setHeader("date-time", String.valueOf(time));
            JSONObject json = _request.get(connection, id);
            response.setStatus(200);
            out.println(json);
        } catch (httpError e) {
            System.err.println(e.message);
            e.printStackTrace();
            response.sendError(e.code, e.message);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (Exception e) {

            }
            out.close();
        }
    }

    /**
     * Session-less request wrapper for handling response wrapping
     *
     * @param _request Interface for lambda function for code pass through
     * @param request Request object from the wrapped servlet
     * @param response Response object from the wrapped servlet
     */
    protected void doRequestSessionless(httpRequestSessionless _request, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        DatabaseConnection dbAuto = new DatabaseConnection("di185");
        Connection connection = dbAuto.getConnection();
        PrintWriter out = response.getWriter();
        try {
            headers = authHeaders(request);
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "POST");
            response.setHeader("Access-Control-Allow-Headers", "*");
            response.setHeader("Access-Control-Expose-Headers", "date-time, app-key");
            response.setContentType("application/json");
            DateTime date = new DateTime(Long.valueOf((String) headers.get("date-time")));
            verifyOrigin(date);
            long time = System.currentTimeMillis();
            Signer signer = new Signer("+" + time + "+");
            response.setHeader("app-key", signer.signKey());
            response.setHeader("date-time", String.valueOf(time));
            JSONObject json = _request.get(connection);
            response.setStatus(200);
            out.println(json);
        } catch (httpError e) {
            System.err.println(e.message);
            e.printStackTrace();
            response.sendError(e.code, e.message);
        } finally {
            try {
                connection.close();
            } catch (Exception e) {

            }
            out.close();
        }
    }

    /**
     * Interface used to pass through code to be wrapped
     */
    public interface httpRequest {
        JSONObject get(Connection conn, int id) throws httpError, IOException;
    }

    /**
     * Same as the previous interface but for session-less servlets
     */
    public interface httpRequestSessionless {
        JSONObject get(Connection conn) throws httpError;
    }

    /**
     * Exception handling for sending 401 errors
     */
    public class httpError extends Exception {
         int code;
         String message;
         public httpError(String message, int code) {
            this.code = code;
            this.message = message;
        }
    }
}