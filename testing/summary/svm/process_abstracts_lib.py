import json
import nltk.classify
import operator
import os
import requests

from nltk.corpus import stopwords
from nltk.stem import PorterStemmer
from nltk.tokenize import sent_tokenize, word_tokenize
from sklearn.svm import LinearSVC    
from textblob.classifiers import NLTKClassifier

ps = PorterStemmer()
SIZE_WORD_FEATURE_VECTOR = 50
AZTOOLS_TIMEOUT = 120
TRAINING_PDFS_DIRECTORY = "training_pdfs"
RAW_WORD_LIST = "word_list.txt"
STEMMED_WORD_LIST = "stemmed_words.txt"

def getAllPDFs():	
	print "Grabbing all PDFs from directory"
	list_files = []
	for file in os.listdir(TRAINING_PDFS_DIRECTORY):
	    if file.endswith(".pdf"):
	        list_files.append(str(TRAINING_PDFS_DIRECTORY) + "/" + str(file))
	return list_files

def getAbstracts(list_files):
	print "Getting all abstracts from AZtools"
	abstracts = []
	for file in list_files:
		request = None
		files = None
		file_name = None
		try:
			files = {'file': open(file, 'rb')}
			name = files['file'].name
			file_name = name[len(TRAINING_PDFS_DIRECTORY) + 1:]
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
	return abstracts

	# display stats from AZtools
	print len(list_files), ": number of PDFs found"
	print len(abstracts), ": number of abstracts retrieved"
	print 100.0 * len(abstracts) / len(list_files), "% PDFs processed through AZtools"

def processAbstracts(abstracts):
	# output: training_data.txt. each sentence on a separate line, each word separated by comma, words are stemmed 
	print "Processing raw abstracts"

	processed_abstracts = [] 
	# list of abstracts, each abstract is a list of sentences, each sentence is a list of tokenized words

	#stop_words = set(stopwords.words("english"))

	with open("training_data.txt", "w+") as training_file:
		for a in abstracts:
			abstract = a[1]
			
			tokenized_sents = sent_tokenize(abstract)	
			# list of tokenized sentences
			
			final_words = []						
			# specific to an abstract. list of sentences, each sentence is list of tokenized and stemmed words
			
			for tok_sent in tokenized_sents:
				words_wstop = word_tokenize(tok_sent)
				filtered_words = words_wstop #[w for w in words_wstop if not w in stop_words]
				stemmed_words = []
				for word in filtered_words:
					stemmed_words.append(ps.stem(word))
				final_words.append(stemmed_words)
				output_line = ""
				for i in final_words:
					output_line += i
					output_line += ","
				output_line = output_line[:-1]
				training_file.write(output_line)
			processed_abstracts.append([a[0], final_words])
	
	return processed_abstracts

def getPopularWords(processed_abstracts):
	print "Getting all possible words"
	all_words = dict()
	for a in processed_abstracts:
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
	return pop_words

def stemAndGetParticularWords():
	output_words = []
	with open(WORD_LIST, "rb") as input_file:
		input_words = input_file.readlines()
		for word in input_words:
			output_words.append(ps.stem(word.lower()))
	with open(STEMMED_WORD_LIST, "w+") as output_file:
		for word in output_words:
			output_file.write(word + "\n")
	return output_words

def create_feature_vector_from_raw_sent(sentence, features): # second paramter can be getPopularWords() or getParticularWords()
	f_vector = []
	for i in range(len(features)):
		f_vector.append(0)
	tok_words = word_tokenize(sentence)
	for word in tok_words:
		if word in features:
			index = features.index(ps.stem(word))
			f_vector[index] = 1
	return f_vector

def create_feature_vector_from_tok_stem_sent(sentence, features): # first parameter is a list of tokenized and stemmed words with stop words removed
	f_vector = []
	for i in range(len(features)):
		f_vector.append(0)
	for word in sentence:
		if word in features:
			index = features.index(word)
			f_vector[index] = 1
	return f_vector

class SVMClassifier(NLTKClassifier):
    """Class that wraps around nltk.classify module for SVM Classifier"""
    nltk_class = nltk.classify.SklearnClassifier(LinearSVC())
