# lightsheet-brain-workflows

Code to format and preprocess whole-brain cleared brain images acquired with light-sheet fluoresence microscopy.
Work in progress.

#### Setup
...

#### Basic running instructions
- Copy and edit the `parameters_ref.yml` file in `resources` with your username (FirstName_LastName).
- Make sure raw data and analysis paths match are correct.
- Open the `guiBuilder.groovy` file in Fiji.
- Click "Run" to compile.
- A window will open asking for your parameter file.
- Fill in the fields and Start.


#### Brain orientation
- The brain orientation must follow the three-letters [BrainGlobe image space definition](https://brainglobe.info/documentation/setting-up/image-definition.html) (see also [here](https://github.com/brainglobe/bg-space)), later used for atlas registration. This is in reference to the origin of the data (first, top left voxel).
- **First** letter: imaging planes are acquired from far to close to the objective with the brain surface facing the detection objective, so the first image will be the bottom of the brain &rarr; _inferior_ (else _superior_)
- **Second** letter: brains are imaged vertically:
  - With olfactory bulb on top &rarr; _anterior_ 
  - Else, with olfactory bulb at the bottom &rarr; _posterior_
- **Third** letter: images are not mirrored, therefore left part of the image is the left hemisphere of the brain &rarr; _left_ (else _right_)
