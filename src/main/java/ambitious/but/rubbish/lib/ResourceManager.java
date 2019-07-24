package ambitious.but.rubbish.lib;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;

public class ResourceManager {

    private HashMap<String, List<String>> db = new HashMap<>();
    private List<String> columns;

    private File file;

    /**
     * Constructor initializes path to database with option for choosing which db to getConnection to.
     *
     * @param resource The URL of the Desired Resource
     * @param columns Columns Aside for First One that Will Exist in Database
     */
    public ResourceManager(URL resource, String[] columns) {
        try {
            this.file = new File(resource.toURI());
            connect();
        } catch (URISyntaxException url) {
            url.printStackTrace();
        }
        this.columns = new LinkedList<>(Arrays.asList(Arrays.copyOfRange(columns, 1, columns.length)));
    }

    /**
     * Return a specific element from entry.
     *
     * @param row Row name as key for the entry
     * @param column Name of column to be returned
     * @throws IllegalArgumentException Throws Exception if an non-existent entry is given
     * @return The column value in String format
     */
    public String get(String row, String column) {
        if (db.containsKey(row)) {
            return db.get(row).get(columnToInt(column));
        } else {
            throw new IllegalArgumentException("Entry Does Not Exist in Local Database");
        }
    }

    /**
     * Returns row in an easy to parse format.
     *
     * @param row Row name as key for entry
     * @throws IllegalArgumentException Throws Exception if an non-existent entry is given
     * @return Value of row in easily parsable form
     */
    public String[] getRow(String row) {
        if (db.containsKey(row)) {
            String[] temp = new String[db.get(row).size() + 1];
            temp[0] = row;
            for (int i = 1, j = 0; i <= columns.size(); i++, j++) {
                temp[i] = db.get(row).get(j);
            }
            return temp;
        } else {
            throw new IllegalArgumentException("Entry Does Not Exist in Local Database");
        }
    }

    /**
     * Returns all the row names in the database in a Set of String format.
     *
     * @return Set of Strings Containing All Keys of Database
     */
    public Set<String> getRows() {
        return db.keySet();
    }

    /**
     * Returns row entries in the database in array of Strings format.
     *
     * @return String[] Containing all Primary Key Entries in Database
     */
    public String[] getRowsString() {
        String[] rows = new String[db.keySet().size()];
        Iterator itr = db.keySet().iterator();
        for (int i = 0; i < db.keySet().size(); i++) {
            if (itr.hasNext()) {
                rows[i] = (String) itr.next();
            }
        }
        return rows;
    }

    /**
     * Return the number of entries in the database.
     *
     * @return The number of entries in database in integer format
     */
    public int getNRows() {
        return db.size();
    }

    /**
     * Gives the integer representation of the specified column.
     *
     * @param column Column to be Converted
     * @return Index of Column
     */
    private int columnToInt(String column) {
        return columns.indexOf(column);
    }

    /**
     * Returns the name of the column at the given index.
     *
     * @param index Index of Column
     * @return Name of Column in String Format
     */
    private String columnFromInt(int index) {
        return columns.get(index);
    }

    /**
     * Reads lines from text document and converts it to database instance.
     */
    private void connect() {
        try {
            Object[] in = Files.lines(this.file.toPath(), Charset.forName("UTF-8")).toArray();
            HashMap<String, List<String>> tempDb = new HashMap<>();
            for (Object obj :
                    in) {
                String[] row = obj.toString().split("[|]");
                tempDb.put(row[0], new ArrayList<>(Arrays.asList(Arrays.copyOfRange(row, 1, row.length))));
            }
            this.db = tempDb;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
