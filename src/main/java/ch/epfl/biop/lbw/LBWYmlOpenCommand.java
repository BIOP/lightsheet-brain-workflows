/*-
 * #%L
 * Format and preprocess whole-brain cleared brain images acquired with light-sheet fluorescence microscopy
 * %%
 * Copyright (C) 2024 - 2025 EPFL
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
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
