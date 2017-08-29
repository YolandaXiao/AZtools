import json
import os
import requests
import subprocess
import time
import urllib2

metadata = []
link = "http://dev.aztec.io:8983/solr/BD2K/select?q=fundingAgencies%3A*+AND+publicationDOI%3A*&start=0&rows=100&wt=json&indent=true"
r = requests.get(link, timeout=5)
json_response = json.loads(r.text)
pdfs_metadata = json_response['response']['docs']
unknown_domains = []

start_time = time.time()

num_unknown_domains = 0
num_DOI_bad_process_link = 0
num_no_access = 0
num_correct = 0
num_incorrect = 0

if r.status_code != 200:
	print "Bad status code to dev.aztec.io:8983"
else:
	print "Gained access to AZtec."
dev_null = open(os.devnull, 'w')

print "Looking for tools...."

with open("funding_results_new3.txt", "w+") as output_file:
	count = 1
	for pdf_info in pdfs_metadata:
		print "---------------------------------------------------------------"
		name = pdf_info['name']
		funding = pdf_info['fundingAgencies']
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
					unknown_domains.append((domain_name, doi_link))
					num_unknown_domains += 1
					continue

		
				path = txt_response[startPos:endPos]

				print "Path found:", path

				pdf_link = "http://" + domain_name + path
			else:
				pdf_link = doi

			pdf_name_pos = pdf_link.rfind("/") + 1
			pdf_file_name = pdf_link[pdf_name_pos : len(pdf_link)]

			if os.path.isfile(pdf_file_name):
				print "File exists in directory, no need to download."
			else:
				print "Downloading PDF from '" + pdf_link + "'"
				try:
					subprocess.check_call(["wget", pdf_link], stdout=dev_null, stderr=dev_null)
				except:
					print "Failed to download PDF"
					num_DOI_bad_process_link += 1
					continue;		


			#test pdf through AZtools
			try:
				files = {'file': open(pdf_file_name, 'rb')}
			except:
				print "Could not open PDF", pdf_file_name

			print "Calling AZtools..."
			r3 = None
			try:
				r3 = requests.post("http://localhost:8080", files=files, timeout=60)
			except:
				print "No response from AZtools for 60s, continuing to next."
				continue

			str_response = None
			modified_txt_response = None
			json_response3 = None
			AZtooled_name = None
			try:
				str_response = r3.text.encode('utf-8')
				modified_txt_response = str_response.replace("\"{", "{").replace("}\"" ,"}")
				json_response3 = json.loads(modified_txt_response)
				AZtooled_funding = json_response3['data'][pdf_file_name]['funding']
				AZtooled_funding_section = json_response3['data'][pdf_file_name]['funding_section']
			except:
				print "Could not process response from AZtools."
				print json_response3
				continue	

			#print funding section
			print "AZtools says funding section is '" + AZtooled_funding_section + "'"
			#print AZtool results
			if(len(AZtooled_funding)>0):
				for i in range(len(AZtooled_funding)):
					if AZtooled_funding[i]['agency']!=None:
						print "AZtools says funding agency is '" + AZtooled_funding[i]['agency'] + "'"
					else:
						print "AZtools says funding agency is '" + "None" + "'"
					# if AZtooled_funding[i]['license']!=None:
					# 	print "AZtools says license is '" + AZtooled_funding[i]['license'] + "'"
					# else:
					# 	print "AZtools says license is '" + "None" + "'"
			else:
				print "AZtools says funding is '" + "NONE" + "'"
			#Print funding result
			if(len(funding)>0):
				for i in range(len(funding)):
					print "Test data says funding agency: '" + funding[i] + "'"
			else:
				print "Test data says funding agency: '" + "NONE" + "'"

			#write to file
			#output_list = [funding.encode('utf-8'), AZtooled_funding.encode('utf-8'), AZtooled_funding_section.encode('utf-8'), doi_link.encode('utf-8')]

			output_file.write(str(count)+': ' +doi_link.encode('utf-8')+'\n')
			output_file.write('\n')

			output_file.write(AZtooled_funding_section.encode('utf-8')+'\n')
			output_file.write('\n')

			AZtooled_funding_list = []
			for i in range(0,len(AZtooled_funding)):
				if AZtooled_funding[i]['agency']!=None:
					AZtooled_funding_list.append(AZtooled_funding[i]['agency'].encode('utf-8'))
			AZtooled_funding_line = " , ".join(AZtooled_funding_list)
			output_file.write(AZtooled_funding_line+'\n') 
			output_file.write('\n')
			
			funding_line = " , ".join(funding)
			output_file.write(funding_line+'\n') 
			output_file.write("--------------------------------------------")
			output_file.write('\n')
			output_file.flush()
			count+=1

			time.sleep(0.5)
