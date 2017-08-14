
with open("2017MeshTree.txt", "rb") as input_file, open("mesh_terms.txt", "w+") as output_file:
	data = input_file.readlines()
	for line in data:
		vals = line.split('\t')
		term = ""
		if len(vals) >= 3:
			for i in str(vals[2]):
				if i.isalnum() or i == ' ':
					term += i
			output_file.write(term + "\n")