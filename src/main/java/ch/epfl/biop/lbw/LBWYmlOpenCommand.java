package ch.epfl.biop.lbw;

import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.swing.script.TextEditor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Plugin(type = Command.class, menuPath = "Plugins>BIOP>Lightsheet Brain Workflows>LBW - Open YML Template")
public class LBWYmlOpenCommand implements Command {


    @Parameter
    Context ctx;

    @Override
    public void run() {
        try {
            TextEditor editor = new TextEditor(ctx);
            editor.setVisible(true);
            editor.open(createTempFileFromURL("https://raw.githubusercontent.com/BIOP/lightsheet-brain-workflows/refs/heads/main/parameters_template.yml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Downloads content from the given URL and saves it to a temporary text file.
     *
     * @param urlString The URL to download content from.
     * @return A File object pointing to the temporary text file.
     * @throws IOException If an I/O error occurs.
     */
    public static File createTempFileFromURL(String urlString) throws IOException {
        // Open a connection to the URL
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Check for successful response code
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to download content. HTTP response code: " + responseCode);
        }

        // Read content from the input stream
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        File tempFile = File.createTempFile("template", ".yml");
        tempFile.deleteOnExit(); // Ensure the file is deleted on JVM exit

        // Write content to the temporary file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
        } finally {
            reader.close();
            connection.disconnect();
        }

        return tempFile;
    }

}
