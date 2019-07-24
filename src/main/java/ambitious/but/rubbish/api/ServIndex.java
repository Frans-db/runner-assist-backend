package ambitious.but.rubbish.api;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "ServIndex")
public class ServIndex extends ServWrapper {
    /**
     * Creates route table object. Add all serverlets to this function so that they can be called by the front-end.
     */
    private JSONObject buildJSON() {
        JSONArray jArray = new JSONArray();
        JSONObject body = new JSONObject();
        jArray.put(addRoute("ServIndex","index","GET"));
        jArray.put(addRoute("Login", "login", "POST"));
        jArray.put(addRoute("UserCreation", "create", "POST"));
        jArray.put(addRoute("GetRunDetailed", "run", "GET"));
        jArray.put(addRoute("SetRunDetailed", "run", "POST"));
        jArray.put(addRoute("Calendar", "calendar", "GET"));
        jArray.put(addRoute("RunDashboard", "rundashboard", "GET"));
        jArray.put(addRoute("Logout", "logout", "GET"));
        jArray.put(addRoute("RunGps", "gps", "GET"));
        jArray.put(addRoute("GetShoes", "shoes", "GET"));
        jArray.put(addRoute("SetShoes", "shoes", "POST"));
        jArray.put(addRoute("GetData", "userdata", "GET"));
        jArray.put(addRoute("SetUserData", "userdata", "POST"));
        jArray.put(addRoute("RemoveShoe", "removeshoe", "GET"));
        jArray.put(addRoute("WeekSummary", "summary", "GET"));
        jArray.put(addRoute("AddRun", "addrun", "POST"));
        jArray.put(addRoute("Advice", "advice", "GET"));
        jArray.put(addRoute("DelAccount", "delete","GET"));


        body.put("routes", jArray);
        return body;
    }

    /**
     * Creates a route object to be added to routes table.
     *
     * @param label The Name of the Servlet
     * @param path Api Path to Servley After "/api/...".
     * @param method Request Method Used
     * @return Returns the
     */
    private JSONObject addRoute(String label, String  path, String method) {
        JSONObject route = new JSONObject();

        route.put("label", label);
        route.put("path", path);
        route.put("method", method);

        return route;
    }

    /**
     * Creates all Route objects and add them to Route table and sends them via in JSON format.
     *
     * @param request GET Request for the Route Table
     * @param response JSON Response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doRequestSessionless((conn) -> {
            JSONObject json;
            json = buildJSON();
            return json;
        }, request, response);
    }
}
