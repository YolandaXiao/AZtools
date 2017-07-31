import requests
import json
import os
from pathlib import *
import codecs

# RPOCESS MULTIPLE PDFs

# files = [('file', open('../../possible_pdf_layouts/chimera.pdf', 'rb')), ('file', open('../../possible_pdf_layouts/genomeHubs.pdf', 'rb'))]
# r = requests.post("http://localhost:8080/batch",files=files)
# pdfs = json.loads(r.text)
# for i in range(len(pdfs)):
# 	filename = pdfs[i]["filename"]
# 	print "\n"+filename
# 	funding = pdfs[i]["funding"]
# 	for j in range(len(funding)):
# 		print funding[j]["agency"]
# 		print funding[j]["license"]


# # PROCESS ONE PDF
# # get all pdf files
# dir_path = Path('/Users/yinxuexiao/Documents/Computer_Science/pdf_test')
# pdf_files = list(dir_path.glob('*.pdf'))
# print "Tesing files:\n"
# print pdf_files
# # input file for POST requests
# for pdffile in pdf_files:
# 	url = pdffile.as_uri().split("file://")[1]
# 	files = {'file': open(url, 'rb')}
# 	r = requests.post("http://localhost:8080",files=files)
# 	# put json into dictionary
# 	pdf = json.loads(r.text)
# 	filename = pdf["filename"]
# 	funding = pdf["funding"]
# 	print "\n"+filename
# 	# get list of funding info
# 	for i in range(len(funding)):
# 		print funding[i]["agency"]
# 		print funding[i]["license"]

#################################################

# PROCESS ONE PDF WRITE TO FILE

# get all pdf files
dir_path = Path('/Users/yinxuexiao/Documents/Computer_Science/pdf_test')
pdf_files = list(dir_path.glob('*.pdf'))
# write to file
outputfile = codecs.open("funding_info.txt", "w", "utf-8")
# input file for POST requests
for pdffile in pdf_files:
	url = pdffile.as_uri().split("file://")[1]
	files = {'file': open(url, 'rb')}
	r = requests.post("http://localhost:8080",files=files)
	# put json into dictionary
	pdf = json.loads(r.text)
	filename = pdf["filename"]
	funding = pdf["funding"]
	print "\n"+filename
	outputfile.write(filename+"\n")
	# get list of funding info
	for i in range(len(funding)):
		print funding[i]["agency"]
		print funding[i]["license"]
		outputfile.write(funding[i]["agency"]+"\n")
		outputfile.write(funding[i]["license"]+"\n")
	outputfile.write("\n")

