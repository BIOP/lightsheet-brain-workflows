import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import ch.epfl.classes.*

File file = new File("D:\\git\\lish_analysis\\lishAnalysis\\src\\main\\resources\\parameters.yml")

// Instantiating a new ObjectMapper as a YAMLFactory
def om = new ObjectMapper(new YAMLFactory())

// Importing YAML parameters
def yaml_param = om.readValue(file, YamlParameters)

// Printing out the information
println("Yaml file info" + yaml_param)

println("Yaml file info" + yaml_param.global_variables.user_list)



/*

// Access the first element of the list and print it as well
println("AAAccessing first element: ${employee.colleagues}")

List<Employee> colleagues = new ArrayList<Employee>(employee.colleagues);

colleagues.add(new Employee("Vladimir", 1000, "Developer", null));
println("Printing collegues: ${colleagues}")

// We want to save this Employee in a YAML file
Employee employee2 = new Employee(employee.name, employee.wage, employee.position, colleagues);
println("Employee info" + employee2.toString())

// ObjectMapper is instantiated just like before
def ObjectMapper om2 = new ObjectMapper(new YAMLFactory());

println("Object mapper info: " + om2)

File output_file = new File("D:\\git\\lish_analysis\\lishAnalysis\\src\\main\\resources\\person2.yaml")

// We write the `employee` into `person2.yaml`
om2.writeValue(output_file, employee2);

*/