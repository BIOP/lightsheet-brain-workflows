package ch.epfl.biop.lbw;
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
