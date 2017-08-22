# AZtools

![AZtools](https://aztec.bio/public/images/bd2k.png)

AZtools is a Java web service that supports [AZtec](http://aztec.bio/) in extracting metadata and content of software tools from PDF files containing academic publications.

The code is licensed under GNU Affero General Public License version 3.

## Using AZtools

$ `mv AZtools/AZtools/src/main/java/upload/.Properties.java AZtools/AZtools/src/main/java/upload/Properties.java`

Uncomment all the code in Properties.java

$ `cd AZtools/AZtools`

$ `mvn spring-boot:run`

Go to `localhost:8080`

1. Upload PDFs of total size <= 1 GB

2. Click Submit

3. JSON response will appear in browser

## REST service

Another possibility is to POST to `/` with a PDF (parameter name "file") 

HTTP Response will contain JSON output of main metadata fields.

## Troubleshoot

Make sure the JDK version specified in AZtools/AZtools/pom.xml matches the installed JDK on your machine.

If you are using IntelliJ IDEA, be sure the add AZtools/AZtools/lib and AZtools/AZtools/lib/stanford-ner-2017-06-09/classifiers (as "Classes") as modules by going to File->Project Structure->Project Settings->Modules and clicking on the green "+" symbol.

## Contact

Email `aztools.nih@gmail.com` for comments, questions, and bug reports.

Enjoy!
