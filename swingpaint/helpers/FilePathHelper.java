package swingpaint.helpers;

/*
 * This class contains a static methods that help process file paths.
 */
public class FilePathHelper {
    /*
     * Converts an array of Strings, each representing one item
     * in a path definition, into a single path where each item
     * is separated by the system file separator character.
     */
    public static String bitsToPath(String[] bits) {
        String path = "";
        for(String bit : bits) {
            path += bit;
            path += System.getProperty("file.separator");
        }
        // remove extra separator at end.
        path = path.substring(0, path.length()-1);

        return path;
    }
}
