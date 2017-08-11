import json
import nltk.classify
import operator
import os
import requests

from nltk.corpus import stopwords
from nltk.tokenize import sent_tokenize, word_tokenize
from sklearn.svm import LinearSVC    
from textblob.classifiers import NLTKClassifier

SIZE_WORD_FEATURE_VECTOR = 50
AZTOOLS_TIMEOUT = 120
DIRECTORY = "training_pdfs"

print "Grabbing all PDFs from directory"
list_files = []
for file in os.listdir(DIRECTORY):
    if file.endswith(".pdf"):
        list_files.append(str(DIRECTORY) + "/" + str(file))

print "Getting all abstracts from AZtools"
abstracts = []
for file in list_files:
	request = None
	files = None
	file_name = None
	try:
		files = {'file': open(file, 'rb')}
		name = files['file'].name
		file_name = name[len(DIRECTORY) + 1:]
		print "Waiting on AZtools for", file_name
		request = requests.post("http://localhost:8080", files=files, timeout=AZTOOLS_TIMEOUT)
	except Exception as ex:
		print str(ex)
		continue
	print "Processing response"
	str_response = None
	modified_txt_response = None
	json_response = None
	name = None
	abstract = None
	try:
		str_response = request.text.encode('utf-8')
		modified_txt_response = str_response.replace("\"{", "{").replace("}\"" ,"}")
		json_response = json.loads(modified_txt_response)
		for key, value in json_response['data'][file_name].iteritems():
			if key == 'name':
				name = value
			elif key == 'abstract':
				abstract = value
			if name and abstract:
				break
		abstracts.append([name, abstract])
		print "Finished processing"
	except Exception as ex:
		print str(ex)
		continue
print "Completed all PDFs"

# display stats from AZtools
print len(list_files), ": number of PDFs found"
print len(abstracts), ": number of abstracts retrieved"
print 100.0 * len(abstracts) / len(list_files), "% PDFs processed through AZtools"

print "Removing stop words and placing sentences in file"
tokenized_abstracts = [] # list of abstracts, each abstract is a list of sentences, each sentence is a list of tokenized words
simple_sentences = []
stop_words = set(stopwords.words("english"))
with open("training_data.txt", "w+") as training_file:
	for a in abstracts:
		abstract = a[1]
		tokenized_sents = sent_tokenize(abstract)	# list of tokenized sentences
		tokenized_words = []						# specific to an abstract. list of sentences, each sentence is list of tokenized words
		for tok_sent in tokenized_sents:
			training_file.write(tok_sent + "\n")
			training_file.flush()
			words_wstop = word_tokenize(tok_sent)
			filtered = [w for w in words_wstop if not w in stop_words]
			tokenized_words.append(filtered)
		tokenized_abstracts.append([a[0], tokenized_words])

print "Getting all possible words"
all_words = dict()
for a in tokenized_abstracts:
	abstract = a[1]
	for sentence in abstract:
		for word in sentence:
			if all_words.has_key(word):
				all_words[word] += 1
			else:
				all_words[word] = 1

print "Finding", SIZE_WORD_FEATURE_VECTOR, "most popular words"
sorted_words = sorted(all_words.items(), key=operator.itemgetter(1))
pop_words = []
for word in reversed(sorted_words):
	pop_words.append(word[0])
	if len(pop_words) >= SIZE_WORD_FEATURE_VECTOR:
		break

def create_feature_vector(sentence):
	fv = []
	for i in range(len(pop_words)):
		fv.append(0)
	tok_words = word_tokenize(sentence)
	for word in tok_words:
		if word in pop_words:
			index = pop_words.index(word)
			fv[index] = 1
	return fv

class SVMClassifier(NLTKClassifier):
    """Class that wraps around nltk.classify module for SVM Classifier"""
    nltk_class = nltk.classify.SklearnClassifier(LinearSVC())
