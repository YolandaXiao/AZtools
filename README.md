# AZtools 

[![Build Status](https://travis-ci.org/dwyl/esta.svg?branch=master)](https://github.com/ankurpapneja/AZtools)

### Introduction

[AZtools](http://dev.aztec.io:8092) is a service that supports [AZtec](http://aztec.bio/) in extracting the metadata of software developed by the biomedical research community. Biomedical software may consist of algorithms, databases, visualizations, or any other codebase.

The code is licensed under GNU Affero General Public License version 3.

### Usage

AZtools takes in as input one of the following:
1) Academic Publication(s) (PDFs) that describe the tool. [Here](https://watermark.silverchair.com/api/watermark?token=AQECAHi208BE49Ooan9kkhW_Ercy7Dm3ZL_9Cf3qfKAc485ysgAAAfcwggHzBgkqhkiG9w0BBwagggHkMIIB4AIBADCCAdkGCSqGSIb3DQEHATAeBglghkgBZQMEAS4wEQQMQhded2DnO4zlPefZAgEQgIIBqjZxb55REhuiDFQDquPHsqoFIEhtRpsVFfelss2OcuDZQCXGMi9BF5-T4xcaAMxaeE6lK_q7hNjyM0TQQwblclAJS_QUCOplin0UdITtI5Eh4MvURzeR0EFtxXY6EgdyYhG7OPORGcdyhY4K4vVmG9hOqu93H4EK1669i2Xf_XbESpE4e35qBQGeO9LLlcQlQ4sdhYuY0bGp6u8ygoVYw6inDHcXLR0JPzXD8UyUdaSDnjLEonoQM85uLIbK2URnLTyISxRUkQ_kgi6EqonK4QH-USqiZDYMAlPyjl4B66V4wU-Axxw9UdJn0r-hRoeWOPO91XjdfE3TgHCqZtO_LQj9hEGI0Tag3WtahyXN9Eb2hoSZthc5adfKVDdNsWkkeBpNfx1hFyPXkGG7yEoG63xzXF772emEsy5kKjhgQ9ZXA73Rstssgt7naoMJP1ywK5VbH6mC5TapEGb0qjRMsRnS-bf7CielKdR08qAjaGvunRTSasX3MQOy8dW5cYqmAc8rRATTh_NYf3ou66Cs7uV617DfpOfy-Y75NYteZ9UhrK8dstcGlk6JYg) is an example for ChimerDB 3.0, a database for fusion genes.
2) PubMed Central Reference Number (PMCID), a unique identification number for works in PubMed Central's digital archive of scientific literature.

AZtools is at [http://dev.aztec.io](http://dev.aztec.io). You may submit any number of PDFs such that the total size is less than 1 GB. There is also a field for the PMCID. However, if PubMed does not allow full text access to the publication, you will have to upload a PDF instead. The response to your submission will be a JSON object containing the metadata of the software. 

AZtools also supports email submission. You may email the PDF to `az.tools100@gmail.com` and expect a response in an attachment `response.json` and in a formatted table in the email body.

### Developers

To contribute to this repository, you will first need to set up the project. The instructions on how to do so are as follows:

`$ git clone https://github.com/ankurpapneja/AZtools.git` This may take a couple minutes.

`$ cd AZtools/`

`$ mkdir AZtools/src/main/java/extraction/summary/abs_to_summ/; cp AZtools/src/main/java/webapp/.Globs.java AZtools/src/main/java/webapp/Globs.java`

Uncomment all of the code in `AZtools/src/main/java/webapp/Globs.java` and fill in your email address and password in their respective fields.

If you do not hava IntelliJ or Java 8:
1) Install IntelliJ from [here](https://www.jetbrains.com/idea/download/)
2) Install JDK 8 from [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html). If you are on an Ubuntu system, you can instead follow the directions [here](http://www.wikihow.com/Install-Oracle-Java-on-Ubuntu-Linux).
3) Open IntelliJ and import `AZtools/` (the higher directory). Click the option to import recursively. Wait for dependencies to resolve. The progress bar can be found near the bottom of the screen.
4) Click the green '+' sign on the right side of the panel at File | Project Structure | Project Settings | Modules | Dependencies.
5) Select 'JARs and directories' and then select all of the JAR files in `AZtools/AZtools/lib/`. Be sure to search recursively for JAR files.
6) In IntelliJ, right click on `AZtools/AZtools/src/main/java/webapp/Application.java` and look for the green triangle. Click on `Run 'Application.main()'`
7) If there are any build errors, see the 'Troubleshoot' section below.

#### Different ways to test the application:
Go to `localhost:8092` in your browser
1) Upload any number of PDFs of total size <= 1 GB
2) Click Submit and wait for JSON response

Email a PDF to the email address you specified in `Globs.java` and wait for a response.

POST to `localhost:8092/` with a PDF (parameter name `file`) 
1) HTTP Response will contain JSON output of main metadata fields.
2) Also possible to submit multiple PDFs at once.

Congratulations, you have successfully set up AZtools for development!

#### Troubleshoot

Make sure the JDK version specified in `AZtools/pom.xml` matches the installed JDK on your machine.

Be sure to add all JAR files in `AZtools/lib` by going to File->Project Structure->Project Settings->Modules and clicking on the green "+" symbol.

Ensure that the paths specified in `AZtools/src/main/java/webapp/Globs.java` match your system's environment. Try listing the absolute path instead of the relative one that starts with `AZtools/`.

Change the port which is specified in `AZtools/src/main/resources/application.properties`. Default port is 8092.

### Microservices Implementation

To add in a new feature (another field of metadata extraction), create another package in `extraction` and create another instance variable in `Attributes.java` that is an instance of your new class.

Initialize the variable in `Attribute.java`'s constructor and create another get method for the variable in `Attributes.java`.

### Contact

Email `aztools.nih@gmail.com` for comments, questions, and bug reports.

Enjoy!
