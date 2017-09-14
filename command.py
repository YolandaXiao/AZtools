
with open("command.txt", "rb") as file:
	data = file.readlines()
	for line in data:
		a = line
		break
b = a.replace("C:\Program Files\Java\jdk1.8.0_144\jre", "/usr/lib/jvm/java-8-oracle/jre")
c = b.replace(";", ":")
d = c.replace("C:\Ankur\Code\AZtools", "/home/developer/AZtools")
e = d.replace("C:\Users\\ankur\.m2\\repository", "/home/developer/.m2/repository")
f = e.replace("\\", "/")

with open("linux_comm.txt", "w+") as file:
	intro = '/usr/bin/java "-javaagent:/home/developer/.local/share/umake/ide/idea/lib/idea_rt.jar=63619:/home/developer/.local/share/umake/ide/idea/bin" -Dfile.encoding=UTF-8 '
	file.write(intro)
	file.write(f)
