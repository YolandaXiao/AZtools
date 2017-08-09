total_false = 0
total_true = 0

with open("output.txt", "rb") as file:
	for line in file:
		arr = line.split(",")
		for i in range(len(arr)):
			arr[i] = arr[i].strip()
		if "False" in arr[4]:
			total_false += 1
			print arr[0]
			print arr[1]
			print arr[3]
			print "\n"
		elif "True" in arr[4]:
			total_true += 1

print "Total false:", total_false
print "Total true:", total_true
Accuracy = total_true * 1.0 / (total_true+total_false) * 100
print "Accuracy:", Accuracy
