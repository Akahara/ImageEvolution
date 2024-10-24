package fr.wonder.gl;

public class FilesUtils {

    public static String getFileExtension(String filePath) {
        return filePath.substring(filePath.lastIndexOf('.')+1);
    }
}
