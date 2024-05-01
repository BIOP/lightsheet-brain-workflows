# Pull base Miniforge
FROM condaforge/miniforge3:latest

# Define maintainer.
LABEL maintainer="Olivier Burri"

# Make a conda environment
RUN	conda install -y -c conda-forge brainreg niftyreg && \
	brainglobe install -a allen_mouse_25um

# Install the atlas-api from brainglobe
RUN pip install brainglobe-atlasapi

#Need to make a symlink to reg_aladin and maybe others, as it seemd BrainReg looks for it as though it were installed by pip
# but it is installed via conda
RUN mkdir -p /opt/conda/lib/python3.10/site-packages/brainreg/core/bin/nifty_reg/linux_x64/ && \
	ln -s /opt/conda/bin/reg_aladin /opt/conda/lib/python3.10/site-packages/brainreg/core/bin/nifty_reg/linux_x64/ && \
	ln -s /opt/conda/bin/reg_f3d /opt/conda/lib/python3.10/site-packages/brainreg/core/bin/nifty_reg/linux_x64/ && \
	ln -s /opt/conda/bin/reg_measure /opt/conda/lib/python3.10/site-packages/brainreg/core/bin/nifty_reg/linux_x64/ && \
	ln -s /opt/conda/bin/reg_tools /opt/conda/lib/python3.10/site-packages/brainreg/core/bin/nifty_reg/linux_x64/ && \
	ln -s /opt/conda/bin/reg_transform /opt/conda/lib/python3.10/site-packages/brainreg/core/bin/nifty_reg/linux_x64/ && \
	ln -s /opt/conda/bin/reg_resample /opt/conda/lib/python3.10/site-packages/brainreg/core/bin/nifty_reg/linux_x64/ && \
	ln -s /opt/conda/bin/reg_jacobian /opt/conda/lib/python3.10/site-packages/brainreg/core/bin/nifty_reg/linux_x64/ && \
	ln -s /opt/conda/bin/reg_average /opt/conda/lib/python3.10/site-packages/brainreg/core/bin/nifty_reg/linux_x64/

# What the container should run when it is started.
#ENTRYPOINT [ "brainreg" ]
