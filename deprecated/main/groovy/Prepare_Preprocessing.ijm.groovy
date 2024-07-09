@Grab('org.yaml:snakeyaml:2.2')

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.DumperOptions
import org.apache.commons.io.FilenameUtils
import static groovy.io.FileType.FILES

#@ File baseYaml
#@ File analysisFolder (label="Folder storing analysis results", style="directory")
#@ File[] rawFolders (label="Folders containing CZI Files", style="directory")


def config = readYamlConfig( baseYaml )

def user = config.general_parameters.user

// Create folder in analysis folder
def userAnalysisFolder = new File( analysisFolder, user )

def filesToAnalyse = []

// Simply prepare all the data
rawFolders.each{ folder ->
	// Copy the base YAML file to each subfolder and give it the name imageName_params.yml
	folder.eachFileRecurse(FILES) {
    	if(it.name.endsWith('.czi')) {
    		filesToAnalyse.add( it )
    	}
    }
}

filesToAnalyse.each{ cziFile ->
	
	// Get the required information. Folder with the name of the dataset
	def imageName = FilenameUtils.getBaseName( cziFile.getAbsolutePath() )
	def outputDirectory = new File( userAnalysisFolder, imageName )
	outputDirectory.mkdirs()

	
	def localYamlConfigFile = new File(outputDirectory, "${imageName}_configuration.yml" )
	
	// Prepare the configuration 
	config.general_parameters.input_path = cziFile.toString()
	config.general_parameters.output_dir = outputDirectory.toString()
	config.general_parameters.yaml_parameter_file = localYamlConfigFile.toString()

	// Define the xml already here
	def bigStitcherXMLFile = new File(outputDirectory, "${imageName}.xml" )
	config.bigstitcher.xml_file = bigStitcherXMLFile.toString()
	
	
	
	saveYamlToFile(config, localYamlConfigFile)
	
}
return

def readYamlConfig(File yamlFile) {
	Yaml parser = new Yaml()
	def data = parser.load( yamlFile.text )
	data.each { println it }
	println "Bigstitcher: "+data.bigstitcher
	return data
}

def saveYamlToFile( def yamlConfig, def yamlPath ) {
    final DumperOptions options = new DumperOptions()
    options.setDefaultFlowStyle( DumperOptions.FlowStyle.BLOCK )
    options.setPrettyFlow( true )
    def yaml = new Yaml( options )
    

    def writer = new FileWriter( yamlPath )
    println "saving to ${yamlPath.toString()}"
    yaml.dump( yamlConfig, writer )
    writer.close()
}
