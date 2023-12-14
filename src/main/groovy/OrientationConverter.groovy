ArrayList<String> ap_axis = ["a", "p"]
ArrayList<String> lr_axis = ["l", "r"]
ArrayList<String> si_axis = ["s", "i"]

ArrayList<String> letters = []
ArrayList<int> input_positions = []
ArrayList<int> output_positions = []

def input_orientation = "sal"

def output_orientation = "ail"

for (int i=0; i<input_orientation.size(); i++) {
	if (output_orientation.contains(input_orientation[i])) {
		letters.add(input_orientation[i])
		input_positions.add(input_orientation.indexOf(input_orientation[i]))
		output_positions.add(output_orientation.indexOf(input_orientation[i]))
		
		print("In input there is '" + input_orientation[i] + "' at position " + input_orientation.indexOf(input_orientation[i]) + " in input and " + output_orientation.indexOf(input_orientation[i]) + " in output\n")
		if(input_orientation.indexOf(input_orientation[i]) == output_orientation.indexOf(input_orientation[i])) {
			if (ap_axis.contains(input_orientation[i])) {
				print("Rotation around ap_axis\n")
			} else if (lr_axis.contains(input_orientation[i])){
				print("Rotation around lr_axis\n")
			} else if (si_axis.contains(input_orientation[i])){
				print("Rotation around si_axis\n")
			}
		}
	}
}
