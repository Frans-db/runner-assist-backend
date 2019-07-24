package ambitious.but.rubbish.api.user;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import ambitious.but.rubbish.api.*;
import ambitious.but.rubbish.api.Calendar;
import ambitious.but.rubbish.api.runs.RunDashboard;
import ambitious.but.rubbish.api.runs.RunDetailed;
import ambitious.but.rubbish.api.runs.RunGPS;
import ambitious.but.rubbish.lib.DatabaseConnection;
import ambitious.but.rubbish.lib.Signer;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.Test;
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ServletsTest {
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    private static JSONObject jLogin = new JSONObject();
    private static JSONObject jUCreate = new JSONObject();
    private static JSONObject jrun = new JSONObject();
    private static JSONObject jshoe = new JSONObject();
    private static int shoeID = 0;
    private List<String> d = new ArrayList<>();
    private Enumeration<String> heads;
    private static String token = "";
    private static String model = Integer.toString(new Random().nextInt());
    private String res = "";
    @BeforeClass
    public static void classSet(){
        // User creation test json
        jUCreate.put("username", "junitTester"+ new Random().nextInt());
        jUCreate.put("password", "junitTesterer");
        jUCreate.put("weight", "85");
        jUCreate.put("height", "175");
        jUCreate.put("email", "spammail"+new Random().nextInt()+"@mail.ru");
        jUCreate.put("age", "1987-05-24");
        jUCreate.put("typeid", "1");
        jUCreate.put("gender", "0");
        // User login test json
        jLogin.put("username","email@email.email");
        jLogin.put("password","Password1!");
        // Run post/get test json
        jrun.put("run_id", 3);
        jrun.put("rating", ThreadLocalRandom.current().nextInt(1, 5));
        jrun.put("description", "Fine." + new Random().nextInt());
        // Shoe post json
        jshoe.put("brand", "Puma");
        jshoe.put("model", model);
        jshoe.put("height_heel", 23);
        jshoe.put("height_forefoot", 20);
        jshoe.put("drop", 25);
        jshoe.put("weight",2);

    }
    @Before
    public void setUP() throws IOException {
        d.add("app-key");
        d.add("session-token");
        d.add("date-time");
        heads = Collections.enumeration(d);
        MockitoAnnotations.initMocks(this);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        int gotrating = 0;
        String gotString = "";
        when(request.getHeaderNames()).thenReturn(heads);
        long time = System.currentTimeMillis();
        Signer sign = new Signer("+" + time + "+");
        when(request.getHeader("date-time")).thenReturn(String.valueOf(time));
        when(request.getHeader("app-key")).thenReturn(sign.singKeyTesting());
        when(request.getHeader("session-token")).thenReturn(token);


    }
    @Test
    public void testAUserCreate() throws Exception {
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jUCreate.toString())));
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        new UserCreation().doPost(request, response);
        writer.flush();
        res = stringWriter.toString();
        assertTrue(res.contains("\"newUser\""));
        assertTrue(res.contains(jUCreate.get("username").toString()));
    }
    @Test
    public void testBLogin() throws Exception {
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jLogin.toString())));
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        new Login().doPost(request, response);
        writer.flush();
        String[] temp = stringWriter.toString().split("\"");
        token = temp[7];
        assertTrue(stringWriter.toString().contains("\"user-token\""));
        assertTrue(stringWriter.toString().contains("\"session-token\""));

    }
    @Test

    public void testCPostRunDetail() throws Exception {
        int gotrating = 0;
        String gotString = "";
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jrun.toString())));
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        new RunDetailed().doPost(request, response);
        writer.flush();
        DatabaseConnection dbAuto = new DatabaseConnection("di185");
        Connection connection = dbAuto.getConnection();
        String query = "SELECT rating,training_description FROM projectdata.run_data WHERE \"ID\" = 3";
        PreparedStatement st = connection.prepareStatement(query);
        ResultSet rs = st.executeQuery();
        if (rs.next()){
            gotrating = rs.getInt(1);
            gotString = rs.getString(2);
        }

        assertEquals(jrun.get("rating") , gotrating);
        assertEquals(jrun.get("description"), gotString);

    }
    @Test
    public void testDRunGET() throws Exception {
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jrun.toString())));
        when(request.getQueryString()).thenReturn("this=kur&run_id=3");
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        new RunDetailed().doGet(request, response);
        writer.flush();
        assertTrue(stringWriter.toString().contains("\"rating\""));
        assertTrue(stringWriter.toString().contains("\"description\""));
        assertTrue(stringWriter.toString().contains("\"gpsData\""));
        assertTrue(stringWriter.toString().contains("\"pushoffpower\""));
        assertTrue(stringWriter.toString().contains("\"times\""));
        assertTrue(stringWriter.toString().contains("\"left\""));
        assertTrue(stringWriter.toString().contains("\"right\""));
        assertTrue(stringWriter.toString().contains("\"axtibacc\""));
        assertTrue(stringWriter.toString().contains("\"vll\""));
        assertTrue(stringWriter.toString().contains("\"brakingforce\""));
        assertTrue(stringWriter.toString().contains("\"tibintrot\""));
        assertTrue(stringWriter.toString().contains("\"pushoffpower\""));
        assertTrue(stringWriter.toString().contains("\"axsacacc\""));
    }
    @Test
    public void testEShoePost() throws Exception {
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jshoe.toString())));
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        new Shoes().doPost(request, response);
        writer.flush();
        DatabaseConnection dbAuto = new DatabaseConnection("di185");
        Connection connection = dbAuto.getConnection();
        String query = "SELECT * FROM projectdata.shoe_types WHERE \"model\" = ?";
        PreparedStatement st = connection.prepareStatement(query);
        st.setString(1,model);
        ResultSet rs = st.executeQuery();
        if(rs.next()) {
            shoeID = rs.getInt("sid");
            assertEquals("Puma", rs.getString(2));
            assertEquals(model, rs.getString(3));
            assertEquals(23, rs.getInt(4));
            assertEquals(20, rs.getInt(5));
            assertEquals(25, rs.getInt(6));
            assertEquals(2, rs.getInt(7));
        }
        connection.close();
    }
    @Test
    public void testFShoeGet() throws Exception {
        when(request.getQueryString()).thenReturn("model=Napali&sid=3");
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        new Shoes().doGet(request, response);
        writer.flush();
        assertTrue(stringWriter.toString().contains("\"Napali\""));
        assertTrue(stringWriter.toString().contains("\"drop\":5"));
        assertTrue(stringWriter.toString().contains("\"height_forefoot\":24"));
        assertTrue(stringWriter.toString().contains("\"weight\":224"));
        assertTrue(stringWriter.toString().contains("\"model\":\"Napali\""));
        assertTrue(stringWriter.toString().contains("\"brand\":\"Hoka One One\""));
    }
    @Test
    public void testGshoeRemove() throws IOException, ServletException, SQLException {
        when(request.getQueryString()).thenReturn("model="+model+"&shoe_id="+shoeID);
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        new RemoveShoe().doGet(request, response);
        String query = "SELECT * FROM projectdata.shoe_types";
        DatabaseConnection dbAuto = new DatabaseConnection("di185");
        Connection connection = dbAuto.getConnection();
        PreparedStatement st = connection.prepareStatement(query);
        ResultSet rs = st.executeQuery();
        List<Map<String, Object>> that = Shoes.parseQuery(rs);
        for(Map<String,Object>  map : that){
            assertFalse(map.containsValue(shoeID));
        }
    }
    @Test
    public void testHCalendar() throws Exception {
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jLogin.toString())));
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        new Calendar().doGet(request, response);
        writer.flush();
        assertTrue(stringWriter.toString().contains("2018-11-21"));
        assertTrue(stringWriter.toString().contains("\"runs\""));
        assertTrue(stringWriter.toString().contains("\"id\""));
        assertTrue(stringWriter.toString().contains("\"distance\""));
        assertTrue(stringWriter.toString().contains("\"time\""));


    }
    @Test
    public void testIGPSrun() throws IOException, ServletException {
        when(request.getQueryString()).thenReturn("model=ranom&run_id=3");
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        new RunGPS().doGet(request, response);
        writer.flush();
        assertTrue(stringWriter.toString().contains("\"longMax\""));
        assertTrue(stringWriter.toString().contains("6.616141"));
        assertTrue(stringWriter.toString().contains("\"latMin\""));
        assertTrue(stringWriter.toString().contains("51.920883"));
        assertTrue(stringWriter.toString().contains("\"latMax\""));
        assertTrue(stringWriter.toString().contains("51.93649"));
    }
    @Test
    public void testJRunDash() throws Exception{
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        new RunDashboard().doGet(request, response);
        writer.flush();
        assertTrue(stringWriter.toString().contains("\"runs\""));
        assertTrue(stringWriter.toString().contains("\"date\":\"2019-06-26\""));
        assertTrue(stringWriter.toString().contains("\"pace\""));
        assertTrue(stringWriter.toString().contains("5.115384615384615"));

    }
    @Test
    public void testKuserDataGET()throws Exception{
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        new userData().doGet(request, response);
        writer.flush();
        assertTrue(stringWriter.toString().contains("\"baselines\""));
        assertTrue(stringWriter.toString().contains("56.343107534367114,-85.66263089800171"));
        assertTrue(stringWriter.toString().contains("\"totalTime\""));
        assertTrue(stringWriter.toString().contains("\"runAmount\""));
        assertTrue(stringWriter.toString().contains("\"totalDistance\""));
    }
    @Test
    public void testLuserDataPOST()throws Exception{
        JSONObject newdata = new JSONObject();
        double gotHeight = 0.0;
        double gotWeight = 0.0;
        newdata.put("height", ThreadLocalRandom.current().nextDouble(1.0, 1.99));
        newdata.put("weight", ThreadLocalRandom.current().nextDouble(55.0, 100.0));
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(newdata.toString())));
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        new userData().doPost(request, response);
        writer.flush();
        String query = "SELECT height,weight FROM projectdata.users WHERE uid = 90";
        DatabaseConnection dbAuto = new DatabaseConnection("di185");
        Connection connection = dbAuto.getConnection();
        PreparedStatement st = connection.prepareStatement(query);
        ResultSet rs = st.executeQuery();
        if(rs.next()){
            gotHeight = rs.getDouble(1);
            gotWeight = rs.getDouble(2);
        }
        assertEquals(newdata.getDouble("height"),gotHeight,0.01);
        assertEquals(newdata.getDouble("weight"),gotWeight,0.2);
        connection.close();
    }
    @Test
    public void testYUserExists() throws Exception {
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jUCreate.toString())));
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        new UserCreation().doPost(request, response);
        writer.flush();
    }

    @Test
    public void testWadvice() throws Exception {
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jUCreate.toString())));
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        new Advice().doGet(request, response);
        writer.flush();
        assertTrue(stringWriter.toString().contains("\"advices\""));
        assertTrue(stringWriter.toString().contains("\"date\":\"2018-11-21 00:00:00\""));
        assertTrue(stringWriter.toString().contains("\"message\":\"Try pushing harder!\""));
        assertTrue(stringWriter.toString().contains("\"distance\":7.88" ));
        assertTrue(stringWriter.toString().contains("\"id\":7"));
    }
    @Test
    public void testZLogout() throws Exception {
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jLogin.toString())));
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        new Logout().doGet(request, response);
        writer.flush();
        assertTrue(stringWriter.toString().contains("\"Message\""));
        assertTrue(stringWriter.toString().contains("\"Success\""));

    }
}
