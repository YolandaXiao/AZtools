# AZtools

![AZtools](https://aztec.bio/public/images/bd2k.png)

AZtools is a Java web service that supports [AZtec](http://aztec.bio/) in extracting metadata and content of software tools from PDF files containing academic publications.

The code is licensed under GNU Affero General Public License version 3.

## Using AZtools (Assumes Linux System)

`$ cd AZtools/`

`$ mkdir AZtools/src/main/java/extraction/summary/abs_to_summ/`

`$ cp AZtools/src/main/java/webapp/.Globs.java AZtools/AZtools/src/main/java/webapp/Globs.java`

Uncomment all of the code in `AZtools/AZtools/src/main/java/upload/Globs.java` and change the path variables to match your system. Also add in your email address and password in their respective fields.

Execute the command in `linux_comm.txt` after: `$ cd AZtools/src/main/resources`

Go to `localhost:8092` in your browser

1. Upload any number of PDFs of total size <= 1 GB

2. Click Submit and wait for JSON response

Email a PDF to the email address you specified in Globs.java

1. Wait for a response.

2. Should not take a long time depending on number of requests currently in queue.

POST to `localhost:8092/` with a PDF (parameter name "file") 

1. HTTP Response will contain JSON output of main metadata fields.

2. Also possible to submit multiple PDFs at once.

## Troubleshoot

Make sure the JDK version specified in AZtools/AZtools/pom.xml matches the installed JDK on your machine.

If you are using IntelliJ IDEA, be sure to add AZtools/AZtools/lib and all subdirectories (as "Classes") as modules by going to File->Project Structure->Project Settings->Modules and clicking on the green "+" symbol.

Ensure that the paths specified in Globs.java match your system's environment.

## Contact

Email `aztools.nih@gmail.com` for comments, questions, and bug reports.

Enjoy!
