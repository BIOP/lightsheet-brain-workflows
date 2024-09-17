# Lightsheet Brain Workflows 🔬🧠

Code to format and preprocess whole-brain cleared brain images acquired with light-sheet fluoresence microscopy. 

This repository allows you to process lighsheet data from .czi files through BigStitcher and BrainGlobe's brainreg.

# About
The goal of this repository is to build tools to allow for the scalable stitching, fusion and processing of tiled lightsheet data using existing tools.
In addition whole-brain fused image stacks are registered to a reference atlas for downstream analyses.

# Local usage

## Installation

### This repo

Download and unzip this repository somewhere e.g. in your `Users/Github` repository.

### Fiji 

You must have a working Fiji installation with the BigStitcher update site enabled.
1. Start Fiji and go to Help > Update
2. Click on "Manage update sites"
3. Check BigStitcher
4. Click on Save and close and restart Fiji

### brainreg

You can install brainreg and, for example, download the Allen Mouse Brain atlas (barrel-enhanced) at 10um resolution, with the following commands:

```
conda create -n brainreg python==3.11 -y
conda activate brainreg
conda install -y -c conda-forge brainreg
brainglobe install -a allen_mouse_bluebrain_barrels_10um
```

Further documentation about brainreg, atlases and registration parameters can be found in the [BrainGlobe's dedicated documentation](https://brainglobe.info/about.html).
Any atlas unavailable locally will be downloaded upon first call, so there is no need to pre-download atlases. 
But this can be done like so, once brainreg is installed.
```
brainglobe install -a allen_mouse_bluebrain_barrels_10um
```

## Use 

### Prepare for processing
1. Make yourself a copy of `parameters_template.yml`, replacing user-specific fields and parameters e.g. `save_dir`, `conda_activate_path`, etc.
2. From within Fiji, run the script called `YamlGuiCreator.groovy` 
3. You will be prompted for an input .yml file, select your `parameters_<user>.yml` file
4. On the GUI, fill in the necessary fields as necessary
	- Under General: Specify your user name and the directory where you would like your processing data to be saved
	- BigStitcher tab: These are the parameters for stitching and fusing the lighthseet data
	**Tip**: for debugging/first try, it is useful to downsample the fused images 8x to quickly check the results.
	- Brainreg tab: For local usage, specify your conda environment name that you installed using the instructions above. The conda location can be found with `where conda`.
	**Tip**: 
		- For optimal registration parameters, leave the default ones. From experience, `grid-spacing` and `bending-energy-weight` seem to matter more.
		- For debugging, it is useful to register your brains to a lower resolution atlas e.g. 25um, 50um. If all works, then register to higher resolution.
5. Run tab: Select one or more folders containing CZI files for processing
6. Click on Save. This will generateone folder per brain .czi file, each contaning a ZYXXX_configuration.yml file where ZYXXX is your mouse name.
7. Double check the content of these configuration .yml files to make sure all the fields are as desired.

### Run processing

Open the script `Run_stitching_and_fusion.groovy`.

1. Run the script and select a single .yml brain configuration file in your output directory. 
2. Choose which preprocessing steps to perform. Default to all.
	**Tip**: Useful for troubleshooting/optimizing registration when steps are already performed, select only the steps to repeat.

This will process the entire brain. The log and console windows are useful to check for abnormal preprocessing.
The Fiji editor will display run errors.

**Tip**: While preprocessing (stitching, fusion) can be done in parallel with two Fiji instances opened, atlas registration at high resolution is often limited to one brain at a time.


### Downstream analyses of preprocessed brains

After atlas registration using `brainreg`, downstream analyses include can be conveniently performed using BrainGlobe's other tool:
1. [Silicon probe track segmentation](https://brainglobe.info/tutorials/segmenting-1d-tracks.html) using `brainglobe-segmentation`
2. [3D cell detection](https://brainglobe.info/tutorials/cellfinder-detection.html]) using `cellfinder` / `brainmapper`
3. etc.

Other applications that do not involve BrainGlobe's tool are of course also possible, starting from the fused image stacks or from the atlas-registered brain.

BrainGlobe is maintained and evolving, regularly check for updates!

# Cluster usage for batch processing

**Work in progress**.

# Additional information

#### Brain orientation in the BrainGlobe's image space definition
- The brain orientation must follow the three-letters [BrainGlobe image space definition](https://brainglobe.info/documentation/setting-up/image-definition.html) (see also [here](https://github.com/brainglobe/bg-space)), later used for atlas registration. This is in reference to the origin of the data (first, top left voxel).
- **First** letter: imaging planes are acquired from far to close to the objective with the brain surface facing the detection objective, so the first image will be the bottom of the brain &rarr; _inferior_ (else _superior_)
- **Second** letter: brains are imaged vertically:
  - With olfactory bulb on top &rarr; _anterior_ 
  - Else, with olfactory bulb at the bottom &rarr; _posterior_
- **Third** letter: images are not mirrored, therefore left part of the image is the left hemisphere of the brain &rarr; _left_ (else _right_)

**Notes**: 
	- It is easier to always image your brains in the same orientation to keep processing consistent and simpler.  
	- The Zeiss microscope acquires images in a mirror view. By default, this flips along the y-axis (vertical) the raw tiles before any reorientation is done. 
	Therefore, the raw input orientation in the BrainGlobe space is that of the actual acquisition (facing the brain if positioned as the Zeiss camera).
	For example, the script will convert IAL to ASR (default coronal views). 
	- Double-check that the re-oriented tiff stack is correct, ask yourself e.g. "should I see fluroescent signal in this hemisphere?".
	
	
#### Atlas registration
- Atlas registration is performed on ASR-oriented brains for easier control of registration quality, but could be done in any orientation.
- From experience, a more intact brain (including olfactory bulbs) is essential for good registration.
- Sufficient background fluorescence seems important to make sure all brain contours are included.
- In case of abnormal illumination (e.g. due to blood stains), the registration "collapses" near the brain edges. Reducing the grid-spacing (-20, -30) ensure a constrained registration less prone to underfitting.
	
