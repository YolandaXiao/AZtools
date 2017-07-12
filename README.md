# AZtools

AZtools is a Java web service that ([http://aztec.bio](http://aztec.bio/)) for extracting metadata
and content of software tools from PDF files containing academic publications.

The code is licensed under GNU Affero General Public License version 3.

## Using AZtools

$ cd cermine-impl/

$ mvn clean install

$ cd ../web_service/

$ mvn eclipse:eclipse

$ mvn spring-boot:run

Go to `localhost:8080`

Upload a PDF of size <= 10,000 KB
Click Submit
JSON response will appear in browser

## Creating an executable JAR

$ cd web_service/
$ mvn compile assembly:single

## REST service

Another possibility is to POST to `localhost:8080/` with a PDF (parameter name "file") 
HTTP Response will contain JSON output of main metadata fields.

## Contact

Email `aztools.nih@gmail.com` for comments, questions, and bug reports.

Enjoy!