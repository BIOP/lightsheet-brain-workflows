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
import ij.IJ;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProcessExecutor {

    /**
     * Executes a shell command.
     *
     * @param processString the command to execute
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if the process is interrupted
     */
    public static void executeTask(String processString) throws IOException, InterruptedException {
        // Log the command
        System.out.println("Executing: " + processString);

        // Start the process
        Process task = Runtime.getRuntime().exec(processString);

        // Handle output and error streams
        StreamGobbler outputGobbler = new StreamGobbler(task.getInputStream(), System.out);
        StreamGobbler errorGobbler = new StreamGobbler(task.getErrorStream(), System.err);
        Thread outputThread = new Thread(outputGobbler);
        Thread errorThread = new Thread(errorGobbler);
        outputThread.start();
        errorThread.start();

        // Wait for the process to complete
        int exitCode = task.waitFor();
        outputThread.join();
        errorThread.join();

        // Check for non-zero exit codes
        if (exitCode != 0) {
            throw new RuntimeException("Process failed with exit code: " + exitCode);
        }
    }

    /**
     * Executes a conda activate task.
     *
     * @param condaActivatePath     path to the conda activation script
     * @param condaEnvironmentName  conda environment name
     * @param input                 input file
     * @param outputFolder          output folder
     * @param voxelSize             voxel size
     * @param orientation           orientation
     * @param brainregSettings      additional brainreg settings
     * @param extras                additional arguments
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if the process is interrupted
     */
    public static void executeCondaTask(String condaActivatePath, String condaEnvironmentName,
                                        String input, String outputFolder, double voxelSize,
                                        String orientation, String brainregSettings, String extras) throws IOException, InterruptedException {
        String processString = String.format(
                "%s activate %s & brainreg \"%s\" \"%s\" -v %f %f %f --orientation %s %s %s",
                condaActivatePath, condaEnvironmentName, input, outputFolder,
                voxelSize, voxelSize, voxelSize, orientation, brainregSettings, extras
        );
        executeTask(processString);
    }

    /**
     * Executes a conda activate task.
     *
     * @param venvPath              path to the virual environment
     * @param input                 input file
     * @param outputFolder          output folder
     * @param voxelSize             voxel size
     * @param orientation           orientation
     * @param brainregSettings      additional brainreg settings
     * @param extras                additional arguments
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if the process is interrupted
     */
    public static void executeVenvTask(String venvPath,
                                       String input, String outputFolder, double voxelSize,
                                       String orientation, String brainregSettings, String extras) throws IOException, InterruptedException {
        String processString = String.format(
                "source %s/bin/activate && brainreg \"%s\" \"%s\" -v %f %f %f --orientation %s %s %s",
                venvPath, input, outputFolder,
                voxelSize, voxelSize, voxelSize, orientation, brainregSettings, extras
        );
        IJ.log("Executing venv task:");
        IJ.log(processString);
        //executeTask(processString);
        IJ.log("Executing venv task:");
        IJ.log("bash -c \"" + processString + "\"");

        // Execute through bash shell using ProcessBuilder
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", processString);
        executeTaskWithProcessBuilder(pb);
    }


    /**
     * Executes a task using ProcessBuilder (for shell commands requiring bash).
     *
     * @param pb the ProcessBuilder configured with the command
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if the process is interrupted
     */
    private static void executeTaskWithProcessBuilder(ProcessBuilder pb) throws IOException, InterruptedException {
        // Log the command
        System.out.println("Executing: " + String.join(" ", pb.command()));

        // Start the process
        Process task = pb.start();

        // Handle output and error streams (reusing your existing StreamGobbler)
        StreamGobbler outputGobbler = new StreamGobbler(task.getInputStream(), System.out);
        StreamGobbler errorGobbler = new StreamGobbler(task.getErrorStream(), System.err);
        Thread outputThread = new Thread(outputGobbler);
        Thread errorThread = new Thread(errorGobbler);
        outputThread.start();
        errorThread.start();

        // Wait for the process to complete
        int exitCode = task.waitFor();
        outputThread.join();
        errorThread.join();

        // Check for non-zero exit codes
        if (exitCode != 0) {
            throw new RuntimeException("Process failed with exit code: " + exitCode);
        }
    }

    // Helper class to handle process output
    private static class StreamGobbler implements Runnable {
        private final java.io.InputStream inputStream;
        private final java.io.PrintStream outputStream;

        public StreamGobbler(java.io.InputStream inputStream, java.io.PrintStream outputStream) {
            this.inputStream = inputStream;
            this.outputStream = outputStream;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputStream.println(line);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error reading process stream", e);
            }
        }
    }
}
