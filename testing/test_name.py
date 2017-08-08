import json
import os
import requests
import subprocess
import time
import urllib2

metadata = []
num_pdfs = 20
link = "http://dev.aztec.io:8983/solr/BD2K/select?q=publicationDOI%3A*&rows=" + str(num_pdfs) + "&wt=json&indent=true"
r = requests.get(link, timeout=5)
json_response = json.loads(r.text)
pdfs_metadata = json_response['response']['docs']

num_unknown_domains = 0
num_DOI_bad_process_link = 0
num_no_access = 0

if r.status_code != 200:
	print "Bad status code to dev.aztec.io:8983"
else:
	print "Gained access to AZtec."
dev_null = open(os.devnull, 'w')

print "Looking for tools...."

with open("output.txt", "w+") as output_file:
	for pdf_info in pdfs_metadata:
		print "---------------------------------------------------------------"
		name = pdf_info['name']
		print "Name of tool:", name
		dois = None
		try:
			dois = pdf_info['publicationDOI']
		except:
			print "No DOI available for PDF"
			continue
		successful = False

		if (len(dois) >= 1):
			doi = dois[0]
			pdf_link = None
			if "http" not in doi:
				doi_link = "http://doi.org/" + dois[0]
				print "DOI link:", doi_link

				r = None
				doi_ret_link = None
				txt_response = None
				code = None
				old_url = None

				try:
					r = urllib2.urlopen(doi_link)
					doi_ret_link = r.geturl()
					txt_response = r.read()
					code = r.getcode()
				except:
					print "Unable to access DOI link"
					continue

				print "Return status code:", code
				print "DOI return link:", doi_ret_link
				
				domain_name_startPos = doi_ret_link.find("://") + len("://")
				domain_name_endPos = doi_ret_link.find("/", domain_name_startPos + 3)
				if domain_name_endPos == -1:
					domain_name_endPos = doi_ret_link.find(".com", domain_name_startPos + 3) + 3
					if domain_name_endPos == -1:
						domain_name_endPos = doi_ret_link.find(".edu", domain_name_startPos + 3) + 3
						if domain_name_endPos == -1:
							domain_name_endPos = doi_ret_link.find(".gov", domain_name_startPos + 3) + 3
							if domain_name_endPos == -1:
								domain_name_endPos = doi_ret_link.find(" ", domain_name_startPos + 3) + 3
							else:
								print "Could not find link to PDF from DOI"

				domain_name = doi_ret_link[domain_name_startPos : domain_name_endPos]			

				if "link.springer" in domain_name:
					link_springer = "<div class=\"cta-button-container u-hide-two-col\">"
					pos1 = txt_response.find(link_springer)
					startPos = txt_response.find("href=\"", pos1) + len("href=\"")
					endPos = txt_response.find("\"", startPos + 1) - 1	
				elif "academic.oup.com" in domain_name:
					if not "<span>PDF</span>" in txt_response:
						print "PDF only available to subscribers"
						num_no_access += 1
						continue
					academic_oup = '<li class="toolbar-item item-pdf">'
					pos1 = txt_response.find(academic_oup)
					startPos = txt_response.find("href=\"", pos1) + len("href=\"")
					endPos = txt_response.find("\"", startPos + 1)
				elif "linkinghub.elsevier.com" in domain_name:
					linkinghub_elsevier = "<a class=\"pdf-download-btn-link"
					pos1 = txt_response.find(linkinghub_elsevier)
					startPos = txt_response.find("href=\"", pos1) + len("href=\"")
					endPos = txt_response.find("\"", startPos + 1)
				elif "ieeexplore.ieee.org" in domain_name:
					ieeexplore_ieee = "<a class=\"doc-actions-link stats-document-lh-action-downloadPdf_2"
					pos1 = txt_response.find(ieeexplore_ieee)
					startPos = txt_response.find("href=\"", pos1) + len("href=\"")
					endPos = txt_response.find("\"", startPos + 1)
				elif "mdpi.com" in domain_name:
					mdpi_com = "<i class=\"fa fa-file-pdf-o fa-lg\">"
					pos1 = txt_response.find(mdpi_com)
					startPos = txt_response.find("href=\"", pos1) + len("href=\"")
					endPos = txt_response.find("\"", startPos + 1)
				else:
					print "Unknown domain name", domain_name
					num_unknown_domains += 1
					continue
		
				path = txt_response[startPos:endPos]

				print "Path found:", path

				pdf_link = "http://" + domain_name + path
			else:
				pdf_link = doi

			print "Downloading PDF from '" + pdf_link + "'"
			try:
				subprocess.check_call(["wget", pdf_link], stdout=dev_null, stderr=dev_null)
			except:
				print "Failed to download PDF"
				num_DOI_bad_process_link += 1
				continue;

			pdf_name_pos = pdf_link.rfind("/") + 1
			pdf_file_name = pdf_link[pdf_name_pos : len(pdf_link)]
			
			try:
				files = {'file': open(pdf_file_name, 'rb')}
			except:
				print "Could not open PDF", pdf_file_name

			print "Calling AZtools..."
			r3 = None
			try:
				r3 = requests.post("http://localhost:8080", files=files, timeout=45)
			except:
				print "No response from AZtools for 45s, continuing to next."
				continue

			str_response = None
			modified_txt_response = None
			json_response3 = None
			AZtooled_name = None
			try:
				str_response = r3.text.encode('utf-8')
				modified_txt_response = str_response.replace("\"{", "{").replace("}\"" ,"}")
				json_response3 = json.loads(modified_txt_response)
				AZtooled_name = json_response3['data'][pdf_file_name]['name']
			except:
				print "Could not process response from AZtools."
				print json_response3
				continue
			
			print "AZtools says name is '" + AZtooled_name + "'"
			print "Test data says name: '" + name + "'"
			temp1 = name.lower()
			temp2 = AZtooled_name.lower()
			if temp1 == temp2 or temp1 in temp2 or temp2 in temp1:
				successful = True
				print "AZtools is correct in predicting name."
			else:
				print "AZtools incorrectly predicted name."
			output_list = [name.encode('utf-8'), AZtooled_name.encode('utf-8'), doi_link.encode('utf-8'), pdf_link.encode('utf-8'), str(successful), pdf_file_name.encode('utf-8')]

			metadata.append(output_list)
			output_line = " , ".join(output_list) 
			print output_list
			output_file.write(output_line)
			output_file.write("\n")
			output_file.flush()
			time.sleep(0.5)
		
print "\n-------------------------------"
print "Cleaning up and analyzing results..."
for item in metadata:
	subprocess.check_call(["rm", item[5]], stdout=dev_null)
	
counter = 0
for item in metadata:
	if item[4] == "True":
		counter += 1

print "-------------------------------"
print "Incorrect Predictions:"
for item in metadata:
	if not item[4]:
		print "True name:", item[0] 
		print "Retrieved name:", item[1]

print "-------------------------------"
print "Summary:"

if len(metadata) != 0 and num_pdfs != 0:
	print "Number of tools tried to get PDFs for:", num_pdfs
	print "Number PDFs innaccessible to non-subscribers:", num_no_access
	print "Number unknown domains:", num_unknown_domains
	print "Number of DOIs for which could not get PDF:", num_DOI_bad_process_link
	print "Number of PDFs succesfully retrieved:", len(metadata)
	print "Number of correct predictions:", counter
	print "Accuracy for PDFs retrieved:", counter / len(metadata) * 100, "%"
else:
	print "Could not analyze anything"


