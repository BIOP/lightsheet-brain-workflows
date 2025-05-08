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

import org.apache.commons.io.FilenameUtils;
import org.scijava.Context;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.task.Task;
import org.scijava.task.TaskService;

import java.io.File;

@Plugin(type = Command.class, menuPath = "Plugins>BIOP>Lightsheet Brain Workflows>LBW - Stitch And Fuse")
public class LBWStitchAndFuseCommand implements Command {

    @Parameter
    File yaml_file;

    @Parameter(persist = false, required = false, visibility = ItemVisibility.MESSAGE) // that's just a way to force the appearance of the nice multi-folder select window
    File dummy = null;

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

    @Parameter(label="Quit when done")
    Boolean force_quit;

    @Parameter
    Context ctx;

    @Parameter
    TaskService taskService;

    @Override
    public void run() {

        Task task = taskService.createTask("Stitch + Fuse "+ FilenameUtils.removeExtension(yaml_file.getName()));

        try {
            int nTasks = 0;
            if ( do_create_dataset ) nTasks++;
            if ( do_channel_alignment ) nTasks++;
            if ( do_stitch_tiles ) nTasks++;
            if ( do_asr_reorientation ) nTasks++;
            if ( do_fusion ) nTasks++;
            if ( do_brain_reg ) nTasks++;

            int iTask = 0;

            task.setProgressMaximum(nTasks);
            task.start();

            task.setStatusMessage("Initializes task");
            StitchAndResave resaver = new StitchAndResave( yaml_file, ctx );

            if (task.isCanceled()) return;

            if ( do_create_dataset ) {
                iTask++;
                task.setProgressValue(iTask);
                task.setStatusMessage("Create BigStitcher Dataset...");
                resaver.createBigStitcherDataset();
            }

            if (task.isCanceled()) return;
            if ( do_channel_alignment ) {
                iTask++;
                task.setProgressValue(iTask);
                task.setStatusMessage("Channel alignments...");
                resaver.alignChannels();
            }

            if (task.isCanceled()) return;
            if ( do_stitch_tiles ) {
                iTask++;
                task.setProgressValue(iTask);
                task.setStatusMessage("Stitching tiles...");
                resaver.stitchTiles();
            }

            if (task.isCanceled()) return;
            if ( do_asr_reorientation ) {
                iTask++;
                task.setProgressValue(iTask);
                task.setStatusMessage("Reorient to ASR...");
                resaver.toASR( );
            }

            if (task.isCanceled()) return;
            if ( do_fusion ) {
                iTask++;
                task.setProgressValue(iTask);
                task.setStatusMessage("Fuse Dataset...");
                resaver.fuseDataset( );
            }

            if (task.isCanceled()) return;
            if ( do_brain_reg ) {
                iTask++;
                task.setProgressValue(iTask);
                task.setStatusMessage("Register with BrainReg...");
                resaver.runRegistration();
            }

            if (task.isCanceled()) return;

            if (force_quit) {
                System.exit(0);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            task.finish();
        }

    }


}
