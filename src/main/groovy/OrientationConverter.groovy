ArrayList<String> ap_axis = ["a", "p"]
ArrayList<String> lr_axis = ["l", "r"]
ArrayList<String> si_axis = ["s", "i"]

ArrayList all_axis = [ap_axis, lr_axis, si_axis]

ArrayList<String> letters = []
ArrayList<int> input_positions = []
ArrayList<int> output_positions = []

def input_orientation = "sal"

def output_orientation = "lai"

for (int i=0; i<input_orientation.size(); i++) {
	if (output_orientation.contains(input_orientation[i])) {
		letters.add(input_orientation[i])
		input_positions.add(input_orientation.indexOf(input_orientation[i]))
		output_positions.add(output_orientation.indexOf(input_orientation[i]))
		
		print("There is '" + input_orientation[i] + "' at position " + input_orientation.indexOf(input_orientation[i]) + " in input and " + output_orientation.indexOf(input_orientation[i]) + " in output\n")
		if(input_orientation.indexOf(input_orientation[i]) == output_orientation.indexOf(input_orientation[i])) {
			if (ap_axis.contains(input_orientation[i])) {
				print("Rotation around ap_axis\n")
			} else if (lr_axis.contains(input_orientation[i])){
				print("Rotation around lr_axis\n")
			} else if (si_axis.contains(input_orientation[i])){
				print("Rotation around si_axis\n")
			}
		} 
	} else {
		for (int j = 0; j<3; j++){
			if ( all_axis[j].contains(input_orientation[i])){
				def temp_axis = all_axis[j]
				temp_axis.remove(input_orientation[i])
				print("Opposite letter of " + input_orientation[i] + " is " + temp_axis[0] +"\n")
			}
		}
	}
}


