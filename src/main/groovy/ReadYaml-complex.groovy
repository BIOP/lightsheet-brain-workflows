// @File(label = "Please select your yaml file") yamlFile
// @File(label="Select the CZI file", style="file") CZI_file
// @File(label="Select the output folder", style="directory") output_dir
// @Boolean(label="Use fast reader?", value=true) doFast
// @Boolean(label="Resave in HDF5?", value=false) doResaving
// @Boolean(label="Fast fusion?", value=false) doFuse



yamlFile = new File("D:\\git\\lish_analysis\\lishAnalysis\\src\\main\\resources\\parameters.yml")

// ------------------ Structure avec des classes



// ------------------- Ouvre le ficheir yaml avec un inputstream
InputStream inputStream = null;
try {
	inputStream = new FileInputStream(yamlFile);
} catch (FileNotFoundException e) {
	e.printStackTrace();
}
Constructor c = new Constructor(YamlParameters.class);

// ------------------ On défini la structure du yaml
Yaml yaml = new Yaml(c);

//------------------- On ouvre et on parse
YamlParameters parameters = yaml.load(inputStream);//inputStream as InputStream);

println(parameters.general_parameters.downsampling)

println(parameters.resaving_parameters.subsampling_factors[0])

import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.Yaml;
import ch.epfl.classes.*