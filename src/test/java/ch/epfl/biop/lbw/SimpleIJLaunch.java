package ch.epfl.biop.lbw;

import net.imagej.ImageJ;

public class SimpleIJLaunch {
    /**
     * This main function serves for development purposes.
     * It allows you to run the plugin immediately out of
     * your integrated development environment (IDE).
     *
     * @param args whatever, it's ignored
     * @throws Exception ExecutionException – if the computation threw an exception, InterruptedException – if the current thread was interrupted while waiting
     */
    public static void main(final String... args) throws Exception {
        // create the ImageJ application context with all available services
        final ImageJ ij = new ImageJ();
        ij.ui().showUI();

        //ij.command().run(LBWSettingsCreateCommand.class, true).get();
        //ij.command().run(LBWStitchAndFuseCommand.class, true).get();
    }
}
