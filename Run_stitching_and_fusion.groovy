@Grab('org.yaml:snakeyaml:2.2')   
   
#@ File yamlFile   
#@ Boolean doCreateDataset (label="Create BigStitcher dataset") 
#@ Boolean doChannelAlignment (label="Perform channel alignment") 
#@ Boolean doStitchTiles (label="Stitch tiles") 
#@ Boolean doASRReorientation (label="Reorient to ASR") 
#@ Boolean doFusion (label="Export fused image") 
#@ Boolean doBrainreg (label="Run Brainreg") 
#@ LogService log 
   
import org.yaml.snakeyaml.Yaml   
import groovy.xml.XmlSlurper   
import groovy.xml.XmlUtil   
import java.nio.charset.StandardCharsets   
import ij.IJ   
import java.time.Duration   
 
def resaver = new StitchAndResave( yamlFile ) 
 
if ( doCreateDataset ) resaver.createBigStitcherDataset()   
  
if ( doChannelAlignment ) resaver.alignChannels()   
 
if ( doStitchTiles ) resaver.stitchTiles()   
   
if ( doASRReorientation ) resaver.toASR( )  
  
if ( doFusion ) resaver.fuseDataset( )  
  
if ( doBrainreg ) resaver.runRegistration()  
  
 
class StitchAndResave {   
	def settings   
	def nTiles = 0 // Number of tiles, for "Fix view" during alignment   
	def logtrace = []   
	def tic // For timing purposes   
	   
	StitchAndResave( def yamlFile ) {   
			Yaml parser = new Yaml()   
			this.settings = parser.load( yamlFile.text ) 
			//settings.each{ IJ.log(it.toString()) }   
	}   
	   
	def createBigStitcherDataset() {   
		// If the XML already exists, remove it, otherwise it will complain   
		def bigStitcherXml = new File( settings.bigstitcher.xml_file )   
		if( bigStitcherXml.exists() ) { bigStitcherXml.delete() }   
   
		 // Loading data from CZI file--------------------------------------------------------------------------------------------------------   
        // Check if the 'doResaving' flag is set to true, if so, resave in HDF5 format   
        if ( settings.bigstitcher.reader == "fast" ) {   
            addToLog( "INFO: Start Fast reader", false )   
            // addToLog( "Loading/Resaving time = ")   
            // Import CZI file and resave it in xml format   
            //IJ.run("Make CZI Dataset for BigStitcher", "czi_file=[" + settings.general.input_file + "] output_folder=[" + settings.general.output_dir + "]")   
            IJ.run("Make CZI Dataset for BigStitcher", "czi_file=[" + settings.general.input_file + "] erase_if_file_already_exists=true xml_out=["+settings.bigstitcher.xml_file+"]");  
             
            // Import dataset into bigsticher 
            addToLog("INFO: Fast reader DONE", true ) 
             
        } else {   
	        print("INFO: Start resaving", false)   
	   
	        addToLog("Computing script time when resaving in HDF5\n")   
	        addToLog("Loading/Resaving time = ")   
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
	                "timepoints_per_partition=1 setups_per_partition=0 use_deflate_compression")   
        }   
           
        // BUGFIX: Change the channel name to be the same as channel ID. Otherwise we cannot reorient the sample because the channel name is different for the command and we cannot parse that   
        addToLog( "Fixing channel names and IDs", false )   
		def xml = new XmlSlurper().parse(settings.bigstitcher.xml_file)   
		def channels = xml.SequenceDescription.ViewSetups.Attributes.findAll{ it.@name == "channel" }   
  
		// Make sure channels have names matching the ID		   
		channels.Channel.each{ c ->   
			def id = c.id.text()  
			c.name = id   
		}   
		   
		// Save it. If the character set is not UTF 8 then Bitstitcher will complain   
		def writer = new OutputStreamWriter(new FileOutputStream( settings.bigstitcher.xml_file), StandardCharsets.UTF_8 )   
		XmlUtil.serialize(xml, writer)   
		writer.close()   
		   
		// Take advantage of this to get the number of tiles   
		this.nTiles = xml.SequenceDescription.ViewSetups.Attributes.findAll{ it.@name == "tile" }.Tile.size()   
		
				
		// Flip along the Y axis. This is a particularity of the Zeiss Lightsheet Z1
		addToLog( "INFO: Flipping along Y axis", true )
		
		IJ.run("Apply Transformations", "select=[" + settings.bigstitcher.xml_file + "] apply_to_angle=[All angles] apply_to_channel=[All channels] apply_to_illumination=[All illuminations] apply_to_tile=[All tiles] apply_to_timepoint=[All Timepoints] transformation=Rigid apply=[Current view transformations (appends to current transforms)] define=Matrix same_transformation_for_all_channels same_transformation_for_all_tiles timepoint_0_all_channels_illumination_0_angle_0=[1.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0]");
	    
	    addToLog( "INFO: Flipping along Y axis DONE", true )
	    
	    addToLog( "INFO: Resaving DONE", true )   
   
	}   
	   
	def alignChannels() {   
		// Perform pairwise shift calculations   
        addToLog("Start channel pairwise alignment", false )   
           
        IJ.run("Calculate pairwise shifts ...",   
                "select=[" + settings.bigstitcher.xml_file + "] " +   
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
        )   
        //I ommitted this otpion as it was giving me errors -> "channels=[use Channel Cam1] " --> This was fixed by Oli  
 
 
 
   
   
           
        IJ.run("Filter pairwise shifts ...", "select=[" + settings.bigstitcher.xml_file + "] " +   
                "filter_by_link_quality " +   
                "min_r=" + settings.bigstitcher.channel_alignment.filter_min_r + " " +   
                "max_r=1")   
   
   
        IJ.run("Optimize globally and apply shifts ...",   
                "select=[" + settings.bigstitcher.xml_file + "] " +   
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
                "how_to_treat_tiles=[treat individually]") //yaml_parameters.channel_alignment_parameters.optimize_fix_group   
                   
        addToLog("Channel pairwise alignment DONE", true )   
   
   
	}   
	   
	def stitchTiles() {   
            // Perform global optimization and apply shifts   
        addToLog("Start tile pairwise alignment", false )   
   
   
        // Perform Tile alignement-------------------------------------------------------------------------------------------------   
        // Perform pairwise shift calculations   
        def channelAlignString = "select=[" + settings.bigstitcher.xml_file + "] " +   
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
			                     "how_to_treat_tiles=compare "   
                   
		if( settings.bigstitcher.tile_alignment.use_channel != null ) {   
			channelAlignString += "channels=[use Channel " + settings.bigstitcher.tile_alignment.use_channel + "] "   
		}   
		   
        channelAlignString += "downsample_in_x=" + settings.bigstitcher.tile_alignment.pairwise_shifts_downsamples.x + " " +   
			                  "downsample_in_y=" + settings.bigstitcher.tile_alignment.pairwise_shifts_downsamples.y + " " +   
			                  "downsample_in_z=" + settings.bigstitcher.tile_alignment.pairwise_shifts_downsamples.z   
			                       
                   
        IJ.run("Calculate pairwise shifts ...", channelAlignString )   
           
        // Filter the pairwise shifts based on certain criteria   
   
        IJ.run("Filter pairwise shifts ...", "select=[" + settings.bigstitcher.xml_file + "] " +   
                "filter_by_link_quality " +   
                "min_r=" + settings.bigstitcher.tile_alignment.filter_min_r + " " +   
                "max_r=1")   
   
        // Perform global optimization and apply shifts   
           
        // Need to figure out the group to fix   
        // Use the "middle" so compute the number of tiles and divide by 2   
        def halfTiles = "0-"+ (Math.round( this.nTiles / 2 ) as int)   
           
        IJ.run( "Optimize globally and apply shifts ...",   
                "select=[" + settings.bigstitcher.xml_file + "] " +   
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
                "fix_group_0-$halfTiles" )   
   
		addToLog("Channel pairwise alignment DONE", true )   
   
         
        // Perform ICP refinement----------------------------------------------------------------------   
        // Perform ICP (Iterative Closest Point) refinement   
    	addToLog("Start ICP refinement", false )   
        IJ.run("ICP Refinement ...",   
                "select=[" + settings.bigstitcher.xml_file + "] " +   
                "process_angle=[All angles] " +   
                "process_channel=[All channels] " +   
                "process_illumination=[All illuminations] " +   
                "process_tile=[All tiles] " +   
                "process_timepoint=[All Timepoints] " +   
                "icp_refinement_type=[" + settings.bigstitcher.icp_refinement.icp_refinement_type + "] " +   
                //This was set in the macro recorder but not in LSENS code so I removed it -> "global_optimization_strategy=[Two-Round: Handle unconnected tiles, remove wrong links RELAXED (5.0x / 7.0px)] " +   
                "downsampling=[" +settings.bigstitcher.icp_refinement.downsampling + "] " +   
                "interest=[" + settings.bigstitcher.icp_refinement.interest + "] " +   
                "icp_max_error=[" + settings.bigstitcher.icp_refinement.icp_max_error + "]")   
   
    	addToLog("ICP refinement DONE", true )   
    	   
	}   
	   
		/*   
	 * Convert the initial brain orientation (in brainglobe coordinates) to ASR   
	 */   
	def toASR() {   
		// Make sure that it is written in caps   
		def originalOrientation = settings.bigstitcher.reorientation.raw_orientation.toUpperCase()  
		  
		def t = [:]   
		t['IPL'] = [[axis:"y-axis", angle:180], [axis:"x-axis", angle:-90]]  
		t['IAL'] = [[axis:"y-axis", angle:180], [axis:"x-axis", angle:-90], [axis:"z-axis", angle:180]]  
 
		t['RAS'] = [[axis:"y-axis", angle:-90], [axis:"x-axis", angle:90]]   
		t['SAL'] = [[axis:"y-axis", angle:180], [axis:"x-axis", angle:90]]  
		t['PSL'] = [[axis:"y-axis", angle:180]]  
		t['PIR'] = [[axis:"x-axis", angle:180]]  
		t['LAI'] = [[axis:"y-axis", angle:-90], [axis:"x-axis", angle:90]]  
		 
		t['IAR'] = [[axis:"x-axis", angle:90]]  
		t['AIL'] = [[axis:"z-axis", angle:180]]  
		t['ASR'] = []  
		t['RPI'] = [[axis:"y-axis", angle:-90], [axis:"x-axis", angle:-90]]  
		t['LPS'] = [[axis:"y-axis", angle:90],  [axis:"x-axis", angle:-90]]  
		t['SPR'] = [[axis:"x-axis", angle:-90]]  
		  
		//... and so on   
	   
	 	if( !settings.bigstitcher.reorientation.reorient_to_asr )  
	 		return  
	 	  
		// If the transformation exists, then use it   
		if ( t.containsKey( originalOrientation ) ) {   
			t[originalOrientation].each{ p ->   
				// Build the command    
				def command = "select=[${settings.bigstitcher.xml_file}] "+ 
							  "apply_to_angle=[All angles] "+ 
							  "apply_to_channel=[All channels] "+ 
							  "apply_to_illumination=[All illuminations] "+ 
							  "apply_to_tile=[All tiles] "+ 
							  "apply_to_timepoint=[All Timepoints] "+ 
							  "transformation=Rigid "+ 
							  "apply=[Current view transformations (appends to current transforms)] "+ 
							  "define=[Rotation around axis] "+ 
							  "same_transformation_for_all_channels "+ 
							  "same_transformation_for_all_tiles "+ 
							  "axis_timepoint_0_all_channels_illumination_0_angle_0=${p.axis} "+ 
							  "rotation_timepoint_0_all_channels_illumination_0_angle_0=${p.angle}"  				 
				IJ.log( command )   
				// Run it   
				IJ.run("Apply Transformations", command)   
			}   
		   
		// Otherwise inform that it's not going to be done and mention which transforms are available
		} else {   
			addToLog("We do not have a transformation from '$initialPos'to 'ASR' Skipping reorientation step", false )   
			addToLog("Available Transformations to ASR are from the following orientations ${t.keySet().toString()}", false )   
			return   
		}   
		   
		// Debug show the dataset   
	    // IJ.run("BigStitcher", "browse=[" + settings.bigstitcher.xml_file + "] select=[" + settings.bigstitcher.xml_file + "]")   
 
	}   
	   
	/*  
	 * Fuse using a better way than    
	 */  
	   
	/*   
	 * Fuse   
	 */   
	def fuseDataset() {   
		addToLog( "Start data fusion", false )  
		// Create fused directory  
		def fusedDirectory = new File( settings.general.output_dir + "/" + settings.bigstitcher.fusion.fuse_dir )  
		fusedDirectory.mkdirs()  
		  
		  
		if( settings.bigstitcher.fusion.fusion_type == "fast" ) {   
   
			addToLog( "Fast data fusion", false )   
            IJ.run("Fuse a BigStitcher dataset to OME-Tiff",   
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
                    "fusion_method=[" + settings.bigstitcher.fusion.fusion_method + "] ")   
   
        } else {   
   
			addToLog( "Default data fusion", false )   
            IJ.run("Fuse dataset ...",   
                    "select=[" + settings.bigstitcher.xml_file + "] " +   
                    "process_angle=[All angles] " +   
                    "process_channel=[All channels] " +   
                    "process_illumination=[All illuminations] " +   
                    "process_tile=[All tiles] " +   
                    "process_timepoint=[All Timepoints] " +   
                    "bounding_box=[Currently Selected Views] " +   
                    "downsampling=" + settings.bigstitcher.fusion.downsampling + " " +   
                    "pixel_type=[16-bit unsigned integer] " +   
                    "interpolation=[Linear Interpolation] " +   
                    "image=[Precompute Image] " +   
                    "interest_points_for_non_rigid=[-= Disable Non-Rigid =-] " +   
                    "blend " +   
                    settings.bigstitcher.fusion.preserve_original + " " +   
                    "produce=[" + settings.bigstitcher.fusion.produce + "] " +   
                    "fused_image=[" + settings.bigstitcher.fusion.fused_image + "] " +   
                    "output_file_directory=[" + fusedDirectory + "] " +   
                    "filename_addition=[" + new File(settings.general.input_file).getName() + "]") 	   
        }  
        addToLog("Data fusion DONE", true)   
	}  
	  
	def runRegistration2() {  
		addToLog( "Getting voxel size for dataset", false )   
		def xml = readXML( settings.bigstitcher.xml_file )  
		def voxelSize = Double.parseDouble( xml.SequenceDescription.ViewSetups.ViewSetup[0].voxelSize.size.text().split(" ").min()) * settings.bigstitcher.fusion.downsampling  
		  
		// Number of channels  
		def nC = xml.SequenceDescription.ViewSetups.Attributes.findAll{ it.@name == "channel" }.Channel.size()  
		  
		def fusedDirectory = new File( settings.general.output_dir + "/" + settings.bigstitcher.fusion.fuse_dir )  
		def imageName = new File( settings.general.input_file ).getName()  
		// Make a folder for the registration and place the tiff file sequence in it  
		// Get the filename  
		  
		def fileNames = (0..(nC-1)).collect{ new File( fusedDirectory, imageName + "_fused_tp_0_ch_${it}.tif") }  
		  
		def extras = ""  
		// Append extra channels  
		if( fileNames.size() > 1 ) {  
			extras = " -a " + fileNames.collect{ "\"$it\"" }.join(" ")  
		}  
		IJ.log( extras )  
		  
		// If the reorientation did not take place, use the one provided by the user  
		def orientation = "asr"  
		if( !settings.bigstitcher.reorientation.reorient_to_asr ) {  
			orientation = settings.bigstitcher.reorientation.raw_orientation.toLowerCase()  
		}  
		// Execute the docker command, these need to be added to the yml  
		//'''docker run -t -v "F:\Lightsheet Workflows\analysis\Olivier_Burri\AB001\export":"/stitching" brainreg brainreg /stitching /stitching/registered -v 2 2 100 --orientation ial'''  
		runBrainreg(fileNames[0], fileNames[0].getParent()+"/registered", voxelSize, extras )  
	  
	}  
	  
	def runRegistration() {  
		  
		if( settings.brainreg.atlas_registration== false ) return  
		  
        addToLog( "Getting voxel size for dataset", false )   
		def xml = readXML( settings.bigstitcher.xml_file )  
		def voxelSize = Double.parseDouble( xml.SequenceDescription.ViewSetups.ViewSetup[0].voxelSize.size.text().split(" ").min()) * settings.bigstitcher.fusion.downsampling  
		  
		// Number of channels  
		def nC = xml.SequenceDescription.ViewSetups.Attributes.findAll{ it.@name == "channel" }.Channel.size()  
		  
		def fusedDirectory = new File( settings.general.output_dir + "/" + settings.bigstitcher.fusion.fuse_dir )  
		def imageName = new File( settings.general.input_file ).getName()  
		// Make a folder for the registration and place the tiff file sequence in it  
		// Get the filename  
		  
		def fileNames = (0..(nC-1)).collect{ new File( fusedDirectory, imageName + "_fused_tp_0_ch_${it}.tif") }  
		  
		def extras = ""  
		// Append extra channels  
		if( fileNames.size() > 1 ) {  
			extras = "-a " + fileNames.collect{ "\"$it\"" }.join(" ")  
		}  
		IJ.log( extras )  
		  
		  
		// If the reorientation did not take place, use the one provided by the user
		def orientation = "asr"
		if( !settings.bigstitcher.reorientation.reorient_to_asr ) {  
			orientation = settings.bigstitcher.reorientation.raw_orientation.toLowerCase()  
		}  
		  
		if (settings.brainreg.conda_environement_name != null ) {  
			runBrainreg(fileNames[0], fileNames[0].getParent()+"/registered", voxelSize, orientation, extras )  
		} else {  
			// Execute the docker command, these need to be added to the yml  
			//'''docker run -t -v "F:\Lightsheet Workflows\analysis\Olivier_Burri\AB001\export":"/stitching" brainreg brainreg /stitching /stitching/registered -v 2 2 100 --orientation ial'''  
			def processString = "docker run -t -v \"${fileNames[0].getParent()}\":\"/stitching\" ${settings.brainreg.docker_container_name} brainreg /stitching/${fileNames[0].getName()} stitching/registered -v $voxelSize $voxelSize $voxelSize --orientation $orientation$extras"  
			IJ.log( processString )  
			def task = processString.execute()  
			task.waitForProcessOutput(System.out, System.err)  
		}  
	}  
	  
	def runBrainreg( def input, def outputFolder, def voxelSize, def orientation, def extras ) {  
		// Get all parameters from the yaml  
		def brainregSettings = settings.brainreg.parameters.collect{ p ->  
			if (p.getValue() instanceof Boolean ) {  
				if( p.getValue() ) return "--${p.getKey()}"  
			} else {  
				return "--${p.getKey()} ${p.getValue()}"  
			}  
		}.join(" ")  
		  
 
		def processString = settings.brainreg.conda_activate_path+" activate  ${settings.brainreg.conda_environement_name} & brainreg \"${input}\" \"${outputFolder}\" -v $voxelSize $voxelSize $voxelSize --orientation $orientation $brainregSettings $extras"  
		IJ.log( processString )  
		def task = processString.execute()  
		task.waitForProcessOutput(System.out, System.err)  
	}  
   
    def addToLog( def message, boolean logTimeSinceLast ) {   
    	def toWrite = ""   
    	if( logTimeSinceLast ) {   
    		toWrite = message.toString() + " --> "+computeTime()   
    	} else {   
    		this.tic = System.nanoTime()   
    	}   
    	IJ.log( toWrite.toString() )   
        logtrace << toWrite   
    }  
       
    // TODO we can read the XML and try to optimize the parameter names... ?   
    def readXML( xmlFile ) {   
    	def xml = new XmlSlurper().parse( xmlFile )   
    	return xml   
    }  
       
    def computeTime() {   
		Duration duration = Duration.ofNanos( System.nanoTime() - this.tic )   
		return String.format( "%02d:%02d:%02d", duration.toMinutes() % 60 as Integer, duration.getSeconds() % 60 as Integer, duration.getNano() / 1000000 as Integer )   
    }  
}