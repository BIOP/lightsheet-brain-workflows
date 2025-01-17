package ch.epfl.biop.lbw;

import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;

@Plugin(type = Command.class, menuPath = "Plugins>BIOP>Lightsheet Brain Workflows>LBW - Stitch And Fuse")
public class LBWStitchAndFuseCommand implements Command {

    @Parameter
    File yaml_file;

    @Parameter(label="Create BigStitcher dataset")
    Boolean do_create_dataset;

    @Parameter(label="Perform channel alignment")
    Boolean do_channel_alignment;

    @Parameter(label="Stitch tiles")
    Boolean do_stitch_tiles;

    @Parameter(label="Reorient to ASR")
    Boolean do_asr_reorientation;

    @Parameter(label="Export fused image")
    Boolean do_fusion;

    @Parameter(label="Run Brainreg")
    Boolean do_brain_reg;

    @Parameter
    LogService log;

    @Parameter
    Context ctx;

    @Override
    public void run() {
        try {

            StitchAndResave resaver = new StitchAndResave( yaml_file, ctx );

            if ( do_create_dataset ) resaver.createBigStitcherDataset();

            if ( do_channel_alignment ) resaver.alignChannels();

            if ( do_stitch_tiles ) resaver.stitchTiles();

            if ( do_asr_reorientation ) resaver.toASR( );

            if ( do_fusion ) resaver.fuseDataset( );

            if ( do_brain_reg ) resaver.runRegistration();

        } catch (Exception e) {

            throw new RuntimeException(e);

        }

    }


}
