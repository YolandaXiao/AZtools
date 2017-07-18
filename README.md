# AZtools

![AZtools](https://aztec.bio/public/images/bd2k.png)

AZtools is a Java web service that supports [AZtec](http://aztec.bio/) in extracting metadata and content of software tools from PDF files containing academic publications.

The code is licensed under GNU Affero General Public License version 3.

## Using AZtools

$ cd AZtools/spring-app

$ mvn spring-boot:run

Go to `localhost:8080`

1. Upload a PDF of size <= 10,000 KB

2. Click Submit

3. JSON response will appear in browser

## REST service

Another possibility is to POST to `/` with a PDF (parameter name "file") 

HTTP Response will contain JSON output of main metadata fields.

## Contact

Email `aztools.nih@gmail.com` for comments, questions, and bug reports.

Enjoy!
