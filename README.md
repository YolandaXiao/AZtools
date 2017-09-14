# AZtools 

[![Build Status](https://travis-ci.org/dwyl/esta.svg?branch=master)](https://github.com/ankurpapneja/AZtools)

**[AZtools](http://dev.aztec.io:8092) is a service that supports [AZtec](http://aztec.bio/) in extracting the metadata of software** developed by the biomedical research community. Biomedical software may consist of algorithms, databases, visualizations, or any other codebase.

[Funded](https://www.nih.gov/news-events/news-releases/nih-commits-24-million-annually-big-data-centers-excellence) by the National Institutes of Health, this project was developed at the [Heart BD2K Center of Excellence](https://commonfund.nih.gov/bd2k). The code is licensed under GNU Affero General Public License version 3.

### Usage

AZtools takes in as input one of the following:
1) Academic Publication(s) (PDFs) that describe the tool. [Here](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC5210563/pdf/gkw1083.pdf) is an example for ChimerDB 3.0, a database for fusion genes.
2) [PubMed Central](https://www.ncbi.nlm.nih.gov/pmc/) Reference Number (PMCID), a unique identification number for works in PubMed Central's digital archive of scientific literature.

**AZtools is at [http://dev.aztec.io:8092](http://dev.aztec.io:8092)**. You may submit any number of PDFs such that the total size is less than 1 GB. There is also a field for the PMCID. *However, if PubMed does not allow full text access to the publication, you will have to upload a PDF instead*. The response to your submission will be a JSON object containing the metadata of the software. 

AZtools also supports email submission. You may email the PDF to `az.tools100@gmail.com` and expect a response in an attachment `response.json` and in a formatted table in the email body.

### Developers and Contributors

To contribute to this repository, you will first need to **set up the project**. The instructions on how to do so are as follows:

1) `$ git clone https://github.com/ankurpapneja/AZtools.git`

2) `$ cd AZtools/; mkdir AZtools/src/main/java/extraction/summary/abs_to_summ/`

3) `$ cp AZtools/src/main/java/webapp/.Globs.java AZtools/src/main/java/webapp/Globs.java`

4) Uncomment all of the code in `AZtools/src/main/java/webapp/Globs.java` and fill in your email address and password in their respective fields.

5) *If you do not have IntelliJ or Java 8:*
a) Install IntelliJ from [here](https://www.jetbrains.com/idea/download/)
b) Install JDK 8 from [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html). If you are on an Ubuntu system, you can instead follow the directions [here](http://www.wikihow.com/Install-Oracle-Java-on-Ubuntu-Linux).

6) Open IntelliJ and import `AZtools/` (*the higher directory*). ***Select the option to import recursively***. Wait for dependencies to resolve. The progress bar can be found near the bottom of the screen.

7) Click the green '+' sign on the right side of the panel at *File | Project Structure | Project Settings | Modules | Dependencies*.

8) Select 'JARs and directories' and then select ***all of the JAR files*** in `AZtools/AZtools/lib/`. Be sure to search recursively for JAR files.

9) In IntelliJ, right click on `AZtools/AZtools/src/main/java/webapp/Application.java` and look for the green triangle. Click on `Run 'Application.main()'`. Once you see `Waiting for a response...` in the IntelliJ console log, proceed to the next section.

10) If there are any build errors, see the **'Troubleshoot'** section below.

#### Testing the application:
Go to `localhost:8092` in your browser
1) a. Upload any number of PDFs of total size <= 1 GB. OR b. Enter a PMC ID of a work in PubMed Central
2) Click Submit and wait for JSON response

Email a PDF to the email address you specified in `Globs.java` (instruction #4 in the 'Developers' section) 
1) Simply wait for a response email. If there are not too many documents in the queue, then it should not take long.

POST to `localhost:8092/` with a **PDF** (parameter name `file`) 
1) HTTP Response will contain a JSON output of the main metadata fields.
2) Also possible to submit multiple PDFs at once.

POST to `localhost:8092/pmc_id` with a **String** (parameter name `pmc_id`) 
1) If PubMed allows full text access to the work specified by the unique ID, the HTTP Response will contain a JSON output of the main metadata fields. Otherwise, you will receive a String that describes the problem.

If the four methods of using the application above work, then congratulations, you have successfully set up AZtools for development!

Once you've added in a new feature or improved AZtools, submit a pull request and it will be answered soon.

#### Troubleshoot

Make sure the JDK version specified in `AZtools/pom.xml` matches the installed JDK on your machine. You may check your current version of Java with the command `$ java -version`

Be sure to ***add all JAR files*** in `AZtools/lib` by going to *File | Project Structure | Project Settings | Modules | Dependencies* and clicking on the green "+" symbol. Otherwise the program will not build successfully.

Ensure that the paths specified in `AZtools/src/main/java/webapp/Globs.java` match your system's environment. This means that if the relative paths don't work, try replacing them with absolute ones.

Change the port that is specified in `AZtools/src/main/resources/application.properties`. The default port is 8092.

### Code Organization and Structure

There are two main packages, `webapp` and `extraction`. `webapp` deals with all high-level process/thread related activities and `extraction` deals with specific and individual metadata field value extractions.

The Spring Boot Application starts in `AZtools/src/main/java/webapp/Application.java` just after an instance of `AppThread.java` is created. One of the threads checks and processses submissions sent in through email and the other thread listens for GET/POST requests. A separate machine learning python script is started in `AppThread.java`'s constructor.

`Globs.java` contains a set of paths and machine/user-specific information. `ProcessEmail.java` deals with checking the mailbox, getting a response from processing a pdf, and sending a response to the sender of the email. `ProcessPDF.java` takes in PDFs and in its constructor fills in its variables that store the metadata and processing stats for the PDFs.

### Microservices Implementation

To add in a new feature (another field of metadata extraction), create another package in `extraction` with a new class and create another instance variable in `Attributes.java` that is an instance of your new class.

Initialize the variable in `Attribute.java`'s constructor and create another get method for the variable in `Attributes.java`.

### Deployment

This section concerns those who have access to the AZtec's production server and wish to deploy AZtools to [dev.aztec.io](http://dev.aztec.io).

AZtools will be deployed as a **JAR file**. To create one from IntelliJ, follow the instructions [here](https://www.jetbrains.com/help/idea/packaging-a-module-into-a-jar-file.html).

SSH into the server and execute the JAR file in a tmux session. The current version of AZtools is running in a session named 'aztools_prod'. You may create another session 'aztools_dev' to test new implementations, but make sure to change the port before packaging the code into a JAR file.

### Contact

Email `aztools.nih@gmail.com` for any comments, questions, and bug reports.

Enjoy!
