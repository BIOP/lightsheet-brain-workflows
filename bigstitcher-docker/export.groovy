#@ File xml_bigstitcher_file
#@ CommandService cs

// Parameters from https://github.com/BIOP/bigdataviewer-biop-tools/blob/bigdataviewer-biop-tools-0.7.0/src/main/java/ch/epfl/biop/scijava/command/spimdata/FuseBigStitcherDatasetIntoOMETiffCommand.java

def path = new File( xml_bigstitcher_file.getParent() )
println "Exporting Tiffs from project '$xml_bigstitcher_file'"

def output_path_directory = new File( path, 'export' )
output_path_directory.mkdirs()

// Defaults
def range_channels = ""
def range_slices = ""
def range_frames = ""
def n_resolution_levels = 6
def use_lzw_compression = true
def split_slices = true
def split_channels = true
def split_frames = false
def override_z_ratio = false
def z_ratio = 1.0
def use_interpolation = false


def result = cs.run(FuseBigStitcherDatasetIntoOMETiffCommand.class, true, "xml_bigstitcher_file", xml_bigstitcher_file, 
															 "range_channels", range_channels, 
															 "range_slices", range_slices, 
															 "range_frames", range_frames, 
															 "output_path_directory", output_path_directory, 
															 "n_resolution_levels", n_resolution_levels, 
															 "use_lzw_compression", use_lzw_compression, 
															 "split_slices", split_slices,
															 "split_channels", split_channels, 
															 "split_frames", split_frames,
															 "override_z_ratio", override_z_ratio,
															 "z_ratio", z_ratio,
															 "use_interpolation", use_interpolation
	               ).get()
										 
										 
println( "Export to TIF series done" )




import ch.epfl.biop.scijava.command.spimdata.FuseBigStitcherDatasetIntoOMETiffCommand
