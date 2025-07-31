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

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ch.epfl.biop.scijava.command.spimdata.CreateCZIDatasetCommand;
import ch.epfl.biop.scijava.command.spimdata.FuseBigStitcherDatasetIntoOMETiffCommand;
import ij.IJ;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import mpicbg.spim.data.registration.ViewTransform;
import org.scijava.Context;
import org.scijava.command.CommandService;

public class StitchAndResave {
    Config settings;
    int nTiles = 0; // Number of tiles, for "Fix view" during alignment
    int nChannels = 0;
    List<String> logtrace = new ArrayList<>();
    long tic; // For timing purposes
    final Context ctx;

    public StitchAndResave(File yamlFile, Context ctx) throws Exception {
        this.ctx = ctx;
        this.tic = System.nanoTime();
        settings = Config.loadFromFile(yamlFile);
    }

    public void createBigStitcherDataset() throws SpimDataException, ExecutionException, InterruptedException {
        // If the XML already exists, remove it, otherwise it will complain
        File bigStitcherXml = new File( settings.bigstitcher.xml_file );
        if( bigStitcherXml.exists() ) {
            boolean delete = bigStitcherXml.delete();
            if (!delete) {
                throw new RuntimeException("Error: couldn't delete pre-existing BigStitcher xml file "+settings.bigstitcher.xml_file);
            }
        }

        // Loading data from CZI file--------------------------------------------------------------------------------------------------------
        // Check if the 'doResaving' flag is set to true, if so, resave in HDF5 format
        if ( settings.bigstitcher.reader.equals("fast") ) {
            addToLog( "INFO: Start Fast reader", false );
            //IJ.run("Make CZI Dataset for BigStitcher", "czi_file=[" + settings.general.input_file + "] erase_if_file_already_exists=true xml_out=["+settings.bigstitcher.xml_file+"]");
            ctx.getService(CommandService.class)
                    .run(CreateCZIDatasetCommand.class, true,
                           "czi_file", fromURI(settings.general.input_file),
                            "erase_if_file_already_exists", true,
                            "xml_out", fromURI(settings.bigstitcher.xml_file)).get();
            // Import dataset into bigsticher
            addToLog("INFO: Fast reader DONE", true );

        } else {

            addToLog("INFO: Start resaving", false);
            addToLog("Computing script time when resaving in HDF5\n", false);
            addToLog("Loading/Resaving time = ", false);
            IJ.run("BigStitcher",
                    "select=define " +
                            "define_dataset=[Automatic Loader (Bioformats based)] " +
                            "project_filename=[" + settings.bigstitcher.xml_file + "] " +
                            "path=[" + settings.general.input_file + "] " +
                            "exclude=10 " +
                            "bioformats_series_are?=Tiles " +
                            "bioformats_channels_are?=Channels " +
                            "move_tiles_to_grid_(per_angle)?=[Do not move Tiles to Grid (use Metadata if available)] " +
                            "how_to_load_images=[Re-save as multiresolution HDF5] " +
                            "load_raw_data_virtually " +
                            "dataset_save_path=[" + settings.general.output_dir + "] " +
                            //"subsampling_factors=[" + resaving_subsampling_factors + "] " +
                            //"hdf5_chunk_sizes=[" + resaving_hdf5_chunk_size + "] " +
                            "timepoints_per_partition=1 setups_per_partition=0 use_deflate_compression");
        }

        // BUGFIX: Change the channel name to be the same as channel ID. Otherwise, we cannot reorient the sample because the channel name is different for the command and we cannot parse that
        addToLog( "Fixing channel names and IDs", false );
        SpimData dataset = new XmlIoSpimData().load(fromURI(settings.bigstitcher.xml_file));
        dataset.getSequenceDescription().getAllChannels().forEach((id, channel)-> channel.setName(Integer.toString(id)));

        // Save it.
        new XmlIoSpimData().save(dataset, fromURI(settings.bigstitcher.xml_file));

        // Take advantage of this to get the number of tiles
        this.nTiles = dataset.getSequenceDescription().getAllTilesOrdered().size();
        this.nChannels = dataset.getSequenceDescription().getAllChannels().size();
        addToLog( "INFO: Resaving DONE", true );

    }

    void alignChannels() {
        // Perform pairwise shift calculations
        addToLog("Start channel pairwise alignment", false );

        IJ.run("Calculate pairwise shifts ...",
                "select=[" + toURI(settings.bigstitcher.xml_file) + "] " +
                        "process_angle=[All angles] " +
                        "process_channel=[All channels] " +
                        "process_illumination=[All illuminations] " +
                        "process_tile=[All tiles] " +
                        "process_timepoint=[All Timepoints] " +
                        "method=[Phase Correlation] " +
                        "show_expert_grouping_options " +
                        "how_to_treat_timepoints=[treat individually] " +
                        "how_to_treat_channels=compare " +
                        "how_to_treat_illuminations=group " +
                        "how_to_treat_angles=[treat individually] " +
                        "how_to_treat_tiles=[treat individually] " +
                        "downsample_in_x=" + settings.bigstitcher.channel_alignment.pairwise_shifts_downsamples.x + " " +
                        "downsample_in_y=" + settings.bigstitcher.channel_alignment.pairwise_shifts_downsamples.y + " " +
                        "downsample_in_z=" + settings.bigstitcher.channel_alignment.pairwise_shifts_downsamples.z + " " +
                        "channels=[use Channel 0] "
        );
        //I ommitted this option as it was giving me errors -> "channels=[use Channel Cam1] " --> This was fixed by Oli


        IJ.run("Filter pairwise shifts ...", "select=[" + toURI(settings.bigstitcher.xml_file) + "] " +
                "filter_by_link_quality " +
                "min_r=" + settings.bigstitcher.channel_alignment.filter_min_r + " " +
                "max_r=1");

        IJ.run("Optimize globally and apply shifts ...",
                "select=[" + toURI(settings.bigstitcher.xml_file) + "] " +
                        "process_angle=[All angles] " +
                        "process_channel=[All channels] " +
                        "process_illumination=[All illuminations] " +
                        "process_tile=[All tiles] " +
                        "process_timepoint=[All Timepoints] " +
                        "relative=2.500 " +
                        "absolute=3.500 " +
                        "global_optimization_strategy=[Two-Round using metadata to align unconnected Tiles] " +
                        "show_expert_grouping_options " +
                        "how_to_treat_timepoints=[treat individually] " +
                        "how_to_treat_channels=compare " +
                        "how_to_treat_illuminations=group " +
                        "how_to_treat_angles=[treat individually] " +
                        "how_to_treat_tiles=[treat individually]"); //yaml_parameters.channel_alignment_parameters.optimize_fix_group

        addToLog("Channel pairwise alignment DONE", true );

    }

    void stitchTiles() {
        // Perform global optimization and apply shifts
        addToLog("Start tile pairwise alignment", false );

        // Perform Tile alignement
        // Perform pairwise shift calculations
        String channelAlignString = "select=[" + toURI(settings.bigstitcher.xml_file) + "] " +
                "process_angle=[All angles] " +
                "process_channel=[All channels] " +
                "process_illumination=[All illuminations] " +
                "process_tile=[All tiles] " +
                "process_timepoint=[All Timepoints] " +
                "method=[Phase Correlation] " +
                "show_expert_grouping_options " +
                "how_to_treat_timepoints=[treat individually] " +
                "how_to_treat_illuminations=group " +
                "how_to_treat_angles=[treat individually] " +
                "how_to_treat_tiles=compare ";

        if( settings.bigstitcher.tile_alignment.use_channel != null ) { // BAD, not good to use a null check here
            channelAlignString += "channels=[use Channel " + settings.bigstitcher.tile_alignment.use_channel + "] ";
        }

        channelAlignString += "downsample_in_x=" + settings.bigstitcher.tile_alignment.pairwise_shifts_downsamples.x + " " +
                "downsample_in_y=" + settings.bigstitcher.tile_alignment.pairwise_shifts_downsamples.y + " " +
                "downsample_in_z=" + settings.bigstitcher.tile_alignment.pairwise_shifts_downsamples.z;


        IJ.run("Calculate pairwise shifts ...", channelAlignString );

        // Filter the pairwise shifts based on certain criteria

        IJ.run("Filter pairwise shifts ...", "select=[" + toURI(settings.bigstitcher.xml_file) + "] " +
                "filter_by_link_quality " +
                "min_r=" + settings.bigstitcher.tile_alignment.filter_min_r + " " +
                "max_r=1");

        // Perform global optimization and apply shifts

        // Need to figure out the group to fix
        // Use the "middle" so compute the number of tiles and divide by 2
        String halfTiles = "0-"+ (Math.round( this.nTiles / 2 ));

        IJ.run( "Optimize globally and apply shifts ...",
                "select=[" + toURI(settings.bigstitcher.xml_file) + "] " +
                        "process_angle=[All angles] " +
                        "process_channel=[All channels] " +
                        "process_illumination=[All illuminations] " +
                        "process_tile=[All tiles] " +
                        "process_timepoint=[All Timepoints] " +
                        "relative=2.500 " +
                        "absolute=3.500 " +
                        "global_optimization_strategy=[Two-Round using metadata to align unconnected Tiles] " +
                        "show_expert_grouping_options " +
                        "how_to_treat_timepoints=[treat individually] " +
                        "how_to_treat_channels=group " +
                        "how_to_treat_illuminations=group " +
                        "how_to_treat_angles=[treat individually] " +
                        "how_to_treat_tiles=compare " +
                        "fix_group_0-$halfTiles" );

        addToLog("Channel pairwise alignment DONE", true );


        // Perform ICP refinement----------------------------------------------------------------------
        // Perform ICP (Iterative Closest Point) refinement
        addToLog("Start ICP refinement", false );
        IJ.run("ICP Refinement ...",
                "select=[" + toURI(settings.bigstitcher.xml_file) + "] " +
                        "process_angle=[All angles] " +
                        "process_channel=[All channels] " +
                        "process_illumination=[All illuminations] " +
                        "process_tile=[All tiles] " +
                        "process_timepoint=[All Timepoints] " +
                        "icp_refinement_type=[" + settings.bigstitcher.icp_refinement.icp_refinement_type + "] " +
                        //This was set in the macro recorder but not in LSENS code so I removed it -> "global_optimization_strategy=[Two-Round: Handle unconnected tiles, remove wrong links RELAXED (5.0x / 7.0px)] " +
                        "downsampling=[" +settings.bigstitcher.icp_refinement.downsampling + "] " +
                        "interest=[" + settings.bigstitcher.icp_refinement.interest + "] " +
                        "icp_max_error=[" + settings.bigstitcher.icp_refinement.icp_max_error + "]");

        addToLog("ICP refinement DONE", true );

    }

    /*
     * Convert the initial brain orientation (in brainglobe coordinates) to ASR
     */
    void toASR() throws SpimDataException {
        // Make sure that it is written in caps
        String originalOrientation = fixMirroring();

        //... and so on

        if( !settings.bigstitcher.reorientation.reorient_to_asr ) return;

        // If the transformation exists, then use it
        if ( ASR.t.containsKey(originalOrientation) ) {
            ASR.t.get(originalOrientation).forEach( p -> {
                    // Build the command
                    String command = "select=["+toURI(settings.bigstitcher.xml_file)+"] "+
                    "apply_to_angle=[All angles] "+
                    "apply_to_channel=[All channels] "+
                    "apply_to_illumination=[All illuminations] "+
                    "apply_to_tile=[All tiles] "+
                    "apply_to_timepoint=[All Timepoints] "+
                    "transformation=Rigid "+
                    "apply=[Current view transformations (appends to current transforms)] "+
                    "define=[Rotation around axis] "+
                    "same_transformation_for_all_channels "+
                    "same_transformation_for_all_tiles ";

                    // UNTESTED WITH MULTIPLE CHANNELS!!
                    if (nChannels == 1) {
                        command+="axis_timepoint_0_channel_0_illumination_0_angle_0="+p.get("axis")+" "+
                                "rotation_timepoint_0_channel_0_illumination_0_angle_0="+p.get("angle");
                    } else {
                        command+="axis_timepoint_0_all_channels_illumination_0_angle_0="+p.get("axis")+" "+
                        "rotation_timepoint_0_all_channels_illumination_0_angle_0="+p.get("angle");
                    }

                IJ.log( command );

                // Run it
                IJ.run("Apply Transformations", command);
            });

            // Otherwise inform that it's not going to be done and mention which transforms are available
        } else {
            addToLog("We do not have a transformation from "+originalOrientation+" to 'ASR' Skipping reorientation step", false );
            addToLog("Available Transformations to ASR are from the following orientations "+ASR.t.keySet(), false );
            return;
        }

        // Debug show the dataset
        // IJ.run("BigStitcher", "browse=[" + settings.bigstitcher.xml_file + "] select=[" + settings.bigstitcher.xml_file + "]")

    }

    void fuseDataset() throws ExecutionException, InterruptedException {

        addToLog( "Start data fusion", false );

        // Create fused directory
        File fusedDirectory = new File( settings.general.output_dir + "/" + settings.bigstitcher.fusion_config.fuse_dir );
        fusedDirectory.mkdirs();


        if( "fast".equals(settings.bigstitcher.fusion_config.fusion_type) ) {

            addToLog( "Fast data fusion", false );
            // 	at net.preibisch.mvrecon.fiji.plugin.queryXML.GenericLoadParseQueryXML.queryXML(GenericLoadParseQueryXML.java:271)
            ctx.getService(CommandService.class).run(
                    FuseBigStitcherDatasetIntoOMETiffCommand.class, true,
                    "xml_bigstitcher_file", settings.bigstitcher.xml_file,
                    "output_path_directory", fusedDirectory,
                    "range_channels", "",
                    "range_slices", "",
                    "range_frames", "",
                    "n_resolution_levels", 1,
                    "use_lzw_compression", false,
                    "split_slices", false,
                    "split_channels", false,
                    "split_frames", false,
                    "override_z_ratio", false,
                    "use_interpolation", false,
                    "fusion_method", settings.bigstitcher.fusion_config.fusion_method
            ).get();

            /*IJ.run("Fuse a BigStitcher dataset to OME-Tiff",
                    "xml_bigstitcher_file=[" + settings.bigstitcher.xml_file + "] " +
                            "output_path_directory=[" +fusedDirectory+ "] " +
                            "range_channels= " +
                            "range_slices= " +
                            "range_frames= " +
                            "n_resolution_levels=1 " +
                            "use_lzw_compression=false " +
                            "split_slices=false " +
                            "split_channels=false " +
                            "split_frames=false " +
                            "override_z_ratio=false " + //false
                            "z_ratio= " + //empty
                            "use_interpolation=false " +
                            "fusion_method=[" + settings.bigstitcher.fusion_config.fusion_method + "] ");*/

        } else {

            addToLog( "Default data fusion", false );

            //IJ.run("Image Fusion", "select=C:/Users/chiarutt/Dropbox/BIOP/lightsheet-brain-workflows/data/out/Axel_Bisi/ExampleBrain/ExampleBrain.xml process_angle=[All angles] process_channel=[All channels] process_illumination=[All illuminations] process_tile=[All tiles] process_timepoint=[All Timepoints] bounding_box=[Currently Selected Views] downsampling=1 interpolation=[Linear Interpolation] fusion_type=[Avg, Blending] pixel_type=[32-bit floating point] interest_points_for_non_rigid=[-= Disable Non-Rigid =-] produce=[Each timepoint & channel] fused_image=[Display using ImageJ] display=[precomputed (fast, complete copy in memory before display)] min_intensity=0 max_intensity=255");

            IJ.run("Image Fusion",//"Fuse dataset ...",
                    "select=[" + toURI(settings.bigstitcher.xml_file) + "] " +
                            "process_angle=[All angles] " +
                            "process_channel=[All channels] " +
                            "process_illumination=[All illuminations] " +
                            "process_tile=[All tiles] " +
                            "process_timepoint=[All Timepoints] " +
                            "bounding_box=[Currently Selected Views] " +
                            "downsampling=" + settings.bigstitcher.fusion_config.downsampling + " " +
                            "pixel_type=[16-bit unsigned integer] " +
                            "interpolation=[Linear Interpolation] " +
                            "image=[Precompute Image] " +
                            "interest_points_for_non_rigid=[-= Disable Non-Rigid =-] " +
                            "blend " +
                            settings.bigstitcher.fusion_config.preserve_original + " " +
                            "produce=[" + settings.bigstitcher.fusion_config.produce + "] " +
                            "fused_image=[" + settings.bigstitcher.fusion_config.fused_image + "] " +
                            "output_file_directory=[" + fusedDirectory + "] " +
                            "filename_addition=[" + new File(settings.general.input_file).getName() + "]");
        }
        addToLog("Data fusion DONE", true);
    }

    void runRegistration2() throws SpimDataException, IOException, InterruptedException {
        addToLog( "Getting voxel size for dataset", false );

        SpimData dataset = new XmlIoSpimData().load(fromURI(settings.bigstitcher.xml_file));

        Double voxelSize = Arrays.stream(dataset.getSequenceDescription().getViewSetupsOrdered()
                .get(0).getVoxelSize().dimensionsAsDoubleArray()).min().getAsDouble() * settings.bigstitcher.fusion_config.downsampling;

        // Number of channels
        int nC = dataset.getSequenceDescription().getAllChannels().size();

        File fusedDirectory = new File( settings.general.output_dir + "/" + settings.bigstitcher.fusion_config.fuse_dir );
        String imageName = new File( settings.general.input_file ).getName();

        // Make a folder for the registration and place the tiff file sequence in it
        // Get the filename
        List<File> fileNames = IntStream.range(0, nC)
                .mapToObj(i -> new File(fusedDirectory, imageName + "_fused_tp_0_ch_" + i + ".tif"))
                .collect(Collectors.toList());

        String extras = "";

        // Append extra channels
        if (fileNames.size() > 1) {
            extras = " -a " + fileNames.stream()
                    .map(file -> "\"" + file + "\"")
                    .collect(Collectors.joining(" "));
        }

        IJ.log( extras );

        // If the reorientation did not take place, use the one provided by the user
        String orientation = "asr";
        if( !settings.bigstitcher.reorientation.reorient_to_asr ) {
            orientation = settings.bigstitcher.reorientation.raw_orientation.toLowerCase();
        }

        // Execute the docker command, these need to be added to the yml
        //'''docker run -t -v "F:\Lightsheet Workflows\analysis\Olivier_Burri\AB001\export":"/stitching" brainreg brainreg /stitching /stitching/registered -v 2 2 100 --orientation ial'''
        runBrainreg(fileNames.get(0), fileNames.get(0).getParent()+"/registered", voxelSize, orientation, extras );

    }

    void runRegistration() throws SpimDataException, IOException, InterruptedException {

        //if( settings.brainreg.atlas_registration == false ) return; ?????????????????????? What the heck ????????????????

        addToLog( "Getting voxel size for dataset", false );

        SpimData dataset = new XmlIoSpimData().load(fromURI(settings.bigstitcher.xml_file));

        double voxelSize = Arrays.stream(dataset.getSequenceDescription().getViewSetupsOrdered()
                .get(0).getVoxelSize().dimensionsAsDoubleArray()).min().getAsDouble() * settings.bigstitcher.fusion_config.downsampling;

        // Number of channels
        int nC = dataset.getSequenceDescription().getAllChannels().size();

        File fusedDirectory = new File( settings.general.output_dir + "/" + settings.bigstitcher.fusion_config.fuse_dir );
        String imageName = new File( settings.general.input_file ).getName();

        // Make a folder for the registration and place the tiff file sequence in it
        // Get the filename
        List<File> fileNames = IntStream.range(0, nC)
                .mapToObj(i -> new File(fusedDirectory, imageName + "_fused_tp_0_ch_" + i + ".tif"))
                .collect(Collectors.toList());

        String extras = "";

        // Append extra channels
        if (fileNames.size() > 1) {
            extras = " -a " + fileNames.stream()
                    .map(file -> "\"" + file + "\"")
                    .collect(Collectors.joining(" "));
        }

        IJ.log( extras );


        // If the reorientation did not take place, use the one provided by the user
        String orientation = "asr";
        if( !settings.bigstitcher.reorientation.reorient_to_asr ) {
            orientation = settings.bigstitcher.reorientation.raw_orientation.toLowerCase();
        }

        if ((settings.brainreg.conda_environement_name != null) || (settings.brainreg.venv_path != null)) {
            runBrainreg(fileNames.get(0), fileNames.get(0).getParent()+"/registered", voxelSize, orientation, extras );
        } else {
            throw new UnsupportedOperationException("Cannot run docker currently");
            // Execute the docker command, these need to be added to the yml
            //'''docker run -t -v "F:\Lightsheet Workflows\analysis\Olivier_Burri\AB001\export":"/stitching" brainreg brainreg /stitching /stitching/registered -v 2 2 100 --orientation ial'''
            /*String processString = "docker run -t -v \"${fileNames[0].getParent()}\":\"/stitching\" ${settings.brainreg.docker_container_name} brainreg /stitching/${fileNames[0].getName()} stitching/registered -v $voxelSize $voxelSize $voxelSize --orientation $orientation$extras";
            IJ.log( processString );
            def task = processString.execute()
            task.waitForProcessOutput(System.out, System.err);*/
        }
    }

    void runBrainreg( File input, String outputFolder, double voxelSize, String orientation, String extras ) throws IOException, InterruptedException {
        // Get all parameters from the yaml
        String brainregSettings = Config.BrainReg.getParamsAsString(settings.brainreg.parameters);

        //throw new UnsupportedOperationException("Cannot run BrainReg currently - TODO");

        /*String processString = settings.brainreg.conda_activate_path+" activate  ${settings.brainreg.conda_environement_name} & brainreg \"${input}\" \"${outputFolder}\" -v $voxelSize $voxelSize $voxelSize --orientation $orientation $brainregSettings $extras"
        IJ.log( processString )
        def task = processString.execute()
        task.waitForProcessOutput(System.out, System.err)*/

        if ((settings.brainreg.venv_path == null) || settings.brainreg.venv_path.isEmpty()) {
            ProcessExecutor.executeCondaTask(
                    settings.brainreg.conda_activate_path,
                    settings.brainreg.conda_environement_name,
                    input.getAbsolutePath(), outputFolder, voxelSize,
                    orientation, brainregSettings, extras);
        } else {
            ProcessExecutor.executeVenvTask(
                    settings.brainreg.venv_path,
                    input.getAbsolutePath(), outputFolder, voxelSize,
                    orientation, brainregSettings, extras);
        }




        /*ProcessExecutor.executeCondaTask(
                // TODO
        );*/

    }

    String fixMirroring() throws SpimDataException {
        String orientation = settings.bigstitcher.reorientation.raw_orientation.toLowerCase();
        Map<String, String>  mirrorOrientations = new HashMap<>();

        mirrorOrientations.put("psr","psl");
        mirrorOrientations.put("pil","pir");
        mirrorOrientations.put("rps","ras");
        mirrorOrientations.put("sar","sal");
        mirrorOrientations.put("lap","lai");
        mirrorOrientations.put("ial","iar");
        mirrorOrientations.put("air","ail");
        mirrorOrientations.put("asl","asr");
        mirrorOrientations.put("rai","rpi");
        mirrorOrientations.put("ipr","ipl");
        mirrorOrientations.put("lpi","lps");
        mirrorOrientations.put("spl","spr");

        if ( mirrorOrientations.containsKey(orientation) ) {

            SpimData dataset = new XmlIoSpimData().load(fromURI(settings.bigstitcher.xml_file));

            // Check transform exists
            // def xml = readXML( settings.bigstitcher.xml_file );
            List<ViewTransform> allTransforms = dataset.getViewRegistrations().getViewRegistrationsOrdered().get(0).getTransformList();

            Optional<ViewTransform> flipTransform = allTransforms.stream().filter(viewTransform -> viewTransform.hasName() && viewTransform.getName().contains("Manually defined transformation (Rigid/Affine by matrix)"))
                    .findFirst();

            //def flipTansform = xml.ViewRegistrations.ViewRegistration[0].ViewTransform.find{ it.Name.text().contains("Manually defined transformation (Rigid/Affine by matrix)") }

            addToLog( "INFO: Flipping along X axis", false );
            //FuseBigStitcherDatasetIntoOMETiffCommand
            if (!flipTransform.isPresent()) {

                IJ.run( "Apply Transformations", "select=[" + toURI(settings.bigstitcher.xml_file) + "] "+
                        "apply_to_angle=[All angles] "+
                        "apply_to_channel=[All channels] "+
                        "apply_to_illumination=[All illuminations] "+
                        "apply_to_tile=[All tiles] "+
                        "apply_to_timepoint=[All Timepoints] "+
                        "transformation=Rigid "+
                        "apply=[Current view transformations (appends to current transforms)] "+
                        "define=Matrix "+
                        "same_transformation_for_all_channels "+
                        "same_transformation_for_all_tiles "+
                        "timepoint_0_all_channels_illumination_0_angle_0=[-1.0, 0.0, 0.0, 0.0, "+
                        "0.0, 1.0, 0.0, 0.0, "+
                        "0.0, 0.0, 1.0, 0.0]");

                addToLog( "INFO: Flipping along X axis DONE", true );

            } else {

                addToLog( "INFO: Flipping along X axis not necessary, already in XML metadata", true );

            }

            return mirrorOrientations.get(orientation);

        } else {
            addToLog( "INFO: Orientation " + orientation + " not in flipped orientation database, returning orientation as-is", true );
            return orientation;
        }
    }

    void addToLog( String message, boolean logTimeSinceLast ) {
        String toWrite = "";
        if( logTimeSinceLast ) {
            toWrite = message + " --> " + computeTime();
        } else {
            this.tic = System.nanoTime();
        }

        IJ.log( toWrite );
        logtrace.add(toWrite);
    }

    String computeTime() {
        Duration duration = Duration.ofNanos( System.nanoTime() - this.tic );
        return String.format( "%02d:%02d:%02d", duration.toMinutes() % 60, duration.getSeconds() % 60, duration.getNano() / 1000000 );
    }

    public static String toURI(String in) {
        return in;
    }

    public static String fromURI(String in) {
        return in;
    }
}
