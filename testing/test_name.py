import json
import os
import requests
import subprocess
import urllib2

#subprocess.check_call(["mkdir", "temp_dir"])
#subprocess.check_call(["chdir", "temp_dir"])
metadata = []
num_pdfs = 20
link = "http://dev.aztec.io:8983/solr/BD2K/select?q=publicationDOI%3A*&rows=" + str(num_pdfs) + "&wt=json&indent=true"
r = requests.get(link, timeout=5)
json_response = json.loads(r.text)
pdfs_metadata = json_response['response']['docs']
with open("output.txt", "w+") as output_file:
	if r.status_code != 200:
		print "Bad status code to dev.aztec.io:8983"
	for pdf_info in pdfs_metadata:
		try:
			name = pdf_info['name']
			dois = None
			try:
				dois = pdf_info['publicationDOI']
			except KeyError:
				print "No DOI available for PDF", name
				continue
			successful = False
			if (len(dois) >= 1):
				doi_link = "http://doi.org/" + dois[0]
				straight_download = False
				pdf_link = None
				#if ".pdf" in dois[0]:
				#	doi_link = dois[0]
				#	pdf_link = doi_link
				#	straight_download = True
				#if "http" in dois[0]:
				#	continue
					# deal with different format later
					# i.e. "http"
				'''
				try:
					r2 = requests.get(doi_link, timeout=5)
				except requests.exceptions.ConnectionError:
					print "Bad DOI link", doi_link
					continue;
				if r2.status_code != 200:
					continue			
				txt_response = r2.text
				
				if straight_download:
					#print "Downloading PDF from", pdf_link
					try:
						subprocess.check_call(["wget", pdf_link])
					except subprocess.CalledProcessError:
						print "Failed to get PDF"# from", pdf_link
						continue;
				else:
				'''
				try:
					response_doi = urllib2.urlopen(doi_link)
				except urllib2.HTTPError:
					print "Unable to get PDF from DOI", doi_link
					continue
				txt_response = response_doi.read()
				doi_ret_link = response_doi.geturl()

				string1 = '<li class="toolbar-item item-pdf">'
				pos1 = txt_response.find(string1)
				startPos = txt_response.find("href=\"", pos1) + len("href=\"")
				endPos = txt_response.find("\"", startPos + 1)
				path = txt_response[startPos:endPos]

				#domain_name_startPos = r2.url.find("://") + len("://")
				#domain_name_endPos = r2.url.find("/", domain_name_startPos + 3)
				#domain_name = r2.url[startPos:endPos]

				domain_name_startPos = doi_ret_link.find("://") + len("://")
				domain_name_endPos = doi_ret_link.find("/", domain_name_startPos + 3)
				if domain_name_endPos == -1:
					domain_name_endPos = doi_ret_link.find("!", domain_name_startPos + 3)
				domain_name = doi_ret_link[domain_name_startPos : domain_name_endPos]

				pdf_link = "http://" + domain_name + path
				#print "Downloading PDF from", pdf_link
				try:
					subprocess.check_call(["wget", pdf_link])
				except subprocess.CalledProcessError:
					#print "Failed to get PDF from", pdf_link
					continue;

				pdf_name_pos = pdf_link.rfind("/") + 1
				pdf_file_name = pdf_link[pdf_name_pos : len(pdf_link)]
				
				files = {'file': open(pdf_file_name, 'rb')}
				print "Getting AZtools response..."
				try:
					r3 = requests.post("http://localhost:8080", files=files, timeout=20)
				except requests.exceptions.ReadTimeout:
					print "No response from AZtools for 20s, continuing to next."
				print "Got response!"
				
				str_response = r3.text.encode('utf-8')
				modified_txt_response = str_response.replace("\"{", "{").replace("}\"" ,"}")
				json_response3 = json.loads(modified_txt_response)
				print pdf_file_name
				AZtooled_name = json_response3['data'][pdf_file_name]['name']
				
				print "Got name '" + AZtooled_name + "' from AZtools"
				print "Name from test data was", name
				if AZtooled_name.lower() == name.lower() or AZtooled_name.lower().contains(name.lower()) or name.lower().contains(AZtooled_name.lower()):
					successful = True
				output_list = [name, AZtooled_name, doi_link, pdf_link, successful]
				metadata.append(output_list)
				output_line = ",".join(output_list) 
				output_file.write(output_line)
				output_file.flush()
		except:
			continue;
		
print metadata
counter = 0
for item in metadata:
	if item[4]:
		counter += 1
print "Accuracy:", counter / num_pdfs * 100;


#subprocess.check_call(["rm", "-rf", "temp_dir"])
