package ambitious.but.rubbish;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is a testing class can be ignored, and yes this test was necessary don't ask why
 */
public class App
{
    public static void main( String[] args ){
        File file = new File("/home/hackerman/Downloads/RA-data/stepdata_201905011157.csv");
        String res = "";
        try {
            Object[] in = Files.lines(file.toPath(), Charset.forName("UTF-8")).toArray();
            System.out.println(in[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Just Example Code Storage Nothing to See Here
     */
    public static void writeToFile() {
        try {
            List<String> in = new ArrayList<>(Arrays.asList("First Line Time", "Second Line Time"));
            Path file = Paths.get("C:\\Users\\Lt.facechair\\Documents\\IdeaProjectsDocs\\test.txt");
            in.add("Third line Time\nFourth Line Time");
            Files.write(file, in, Charset.forName("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
