package ambitious.but.rubbish.lib.test;
import static org.junit.Assert.*;
import ambitious.but.rubbish.lib.DatabaseConnection;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnectTest {
    private static DatabaseConnection dbconn;
    private Connection c;
    @BeforeClass
    public static void setUp(){
        dbconn = new DatabaseConnection("di185");
    }
    @Test
    public void nameTest(){
        assertEquals("di185", dbconn.getDbName());
    }
    @Test
    public void hostTest(){
        assertEquals("castle.ewi.utwente.nl", dbconn.getHost());
    }
    @Test
    public void passwordTest(){ assertEquals("seM9C98U", dbconn.getPassword()); }
    @Test
    public void userTest(){
        assertEquals("di185", dbconn.getUser());
    }
}
