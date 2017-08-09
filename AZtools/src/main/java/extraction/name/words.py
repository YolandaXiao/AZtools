# ----------------------------------------
# words.py
# supports findTitle() in Attributes.java
# ----------------------------------------

# Figures out whether a word is defined
# Uses NLTK corpus + PubMed MeSH database
# June 2017

import sys
import urllib.request
#import urllib2
import requests

#from nltk.corpus import words

def find_word(word):
	
	# print words.readme()
	# print "Looking through", len(words.words()), "words!"

	formatted_phrase = word.replace(" ", "+")

	mesh_api = "https://www.ncbi.nlm.nih.gov/mesh/?term=" + formatted_phrase
	#results = urllib2.urlopen(mesh_api).read()
	
	#r = requests.get(mesh_api)
	#results = r.text

	response = urllib.request.urlopen(mesh_api)
	results = response.read()

	with open("en.txt") as file:
		data = file.readlines()
	nltk_words = [line.strip() for line in data]

	#inEnglish = word in words.words()
	inEnglish = word in nltk_words
	inMedical = results.find("The following term was not found in MeSH") == -1
	
	if inEnglish:
		print "Is an english word!"
	if inMedical:
		print "Is a medical term!"

	if not inEnglish and not inMedical:
		print "Word not found!"
		return 1
	else:
		return 0

if __name__ == '__main__':
	if len(sys.argv) != 2:
		print "One argument needed!"
		sys.exit(-1)

	print "Running", sys.argv[0], "to determine whether \"", sys.argv[1], "\" exists and is defined!"
	a = find_word(sys.argv[1])
	
	#print "Exit status is", a
	#raw_input("Press any key to continue....")
	
	sys.exit(a)
	