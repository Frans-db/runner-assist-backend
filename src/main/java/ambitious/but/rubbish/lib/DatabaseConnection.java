package ambitious.but.rubbish.lib;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private Connection c;
    private String host;
    private String dbName;
    private String password;
    private String user;

    /**
     * Constructor loads credentials to class instance.
     *
     * @param name Name of Database
     */
    public DatabaseConnection(String name) {
        ResourceManager resourceManage = new ResourceManager(getClass().getResource("/connections.txt"), new String[] {"dbName", "host", "user", "password"});
        if (!resourceManage.getRows().contains(name)) {
          throw new IllegalArgumentException("Database Connection Does not Exists");
        }
        try {
            String[] dbInfo = resourceManage.getRow(name);
            this.dbName = dbInfo[0];
            this.host = dbInfo[1];
            this.user = dbInfo[2];
            this.password = dbInfo[3];
        } catch (IllegalArgumentException e) {
            e.getMessage();
        }
    }

    /**
     * Sets up connection to database.
     */
    private void connect() {
        try {
            Class.forName("org.postgresql.Driver");
            if (password.equals("null")) {
                c = DriverManager.getConnection("jdbc:postgresql://" + host + ":5432/" + dbName);
            } else {
                c = DriverManager.getConnection("jdbc:postgresql://" + host + ":5432/" + dbName, user, "seM9C98U");
            }
            c.setAutoCommit(false);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Returns the current database connection object. If it does not exist or is closed
     *
     * @return Returns Connection Object
     */
    public Connection getConnection() {
        try {
            if (c == null || c.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return c;
    }

    public boolean closed() {
        try {
            return c.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Closes the instances' database connection.
     */
    public void closeConnection() {
        try {
            if (!c.isClosed()) {
                c.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public String getHost(){ return host; }

    public String getDbName(){ return dbName; }

    public String getPassword(){ return password; }

    public String getUser(){ return user; }
}
