# Lightsheet Brain Workflows

Code to format and preprocess whole-brain cleared brain images acquired with light-sheet fluoresence microscopy.
Work in progress.

Right now this repo allows you to process lighsheet data from CZI files though BigStitcher and Brainreg

# About
The goal of this repository is to build tools to allow for the scalable stitching fusing and processing of tiled  lightsheet data using existing tools.

## Rationale

# Local usage

## Installation

### This repo

Download and unzip this repository somewhere

### Fiji 

You must have a working Fiji installation with the BigStitcher update site enabled
1. Start Fiji and go to Help > Update
2. Click on "Manage update sites"
3. Check BigStitcher
4. Click on Save and close and restart Fiji

### BrainReg

You can install brainreg and the 25um Allen mouse brain with the following commands

```
conda create -n brainreg python==3.11 -y
conda activate brainreg
conda install -y -c conda-forge brainreg
brainglobe install -a allen_mouse_25um
pip install brainglobe-atlasapi
```

## Use 

### Prepare for processing
1. From within Fiji, run the script called `YamlGuiCreator.groovy` 
2. You will be prompted for an input YML file, select the `parameters_template.yml` file
3. On the GUI, fill in the necessary fields
	- Under General: Specify your user name and the directory where you would like your processing data to be saved
	- BigStitcher tab: These are the parameters for stitching and fusing the lighthseet data. 
	- Brainreg tab: For local usage, specify your conda environment name that you installed using the instructions above 
4. Run tab: Select one or more folders containing CZI files for processing
7. Click on Save

This will produce on YML file per CZI file in the save directory 

### Run processing

Open the script `Run_stitching_and_fusion.groovy`

Run the script and select a single YML file in your output directory. This will process the entire brain


# Cluster usage

Work in progress


#### Brain orientation
- The brain orientation must follow the three-letters [BrainGlobe image space definition](https://brainglobe.info/documentation/setting-up/image-definition.html) (see also [here](https://github.com/brainglobe/bg-space)), later used for atlas registration. This is in reference to the origin of the data (first, top left voxel).
- **First** letter: imaging planes are acquired from far to close to the objective with the brain surface facing the detection objective, so the first image will be the bottom of the brain &rarr; _inferior_ (else _superior_)
- **Second** letter: brains are imaged vertically:
  - With olfactory bulb on top &rarr; _anterior_ 
  - Else, with olfactory bulb at the bottom &rarr; _posterior_
- **Third** letter: images are not mirrored, therefore left part of the image is the left hemisphere of the brain &rarr; _left_ (else _right_)
