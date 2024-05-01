#@ File originalFile

import ij.IJ

def name = originalFile.getName().split("\\.")[0]
def path = new File( originalFile.getParent() )
println "Creating project '$name' in '$path'"

def savePath = new File( path, 'hdf5' )
savePath.mkdirs()

def xmlPath = new File( savePath, name+".xml" )

IJ.run("Define dataset ...", "define_dataset=[Automatic Loader (Bioformats based)] project_filename=[${name}.xml] path=[${originalFile.getAbsolutePath()}] exclude=10 bioformats_series_are?=Tiles bioformats_channels_are?=Channels move_tiles_to_grid_(per_angle)?=[Do not move Tiles to Grid (use Metadata if available)] how_to_load_images=[Re-save as multiresolution HDF5] load_raw_data_virtually dataset_save_path=[${savePath.getAbsolutePath()}] subsampling_factors=[{ {1,1,1}, {2,2,1}, {4,4,1}, {8,8,1} }] hdf5_chunk_sizes=[{ {64,64,1}, {64,32,2}, {32,32,4}, {32,32,4} }] timepoints_per_partition=1 setups_per_partition=0 use_deflate_compression");
