import json
import nltk.classify
import operator
import os
import random
import requests
import sys

from nltk.corpus import stopwords
from nltk.stem import PorterStemmer
from nltk.tokenize import sent_tokenize, word_tokenize
from sklearn import svm
from textblob.classifiers import NLTKClassifier

ps = PorterStemmer()
SIZE_WORD_FEATURE_VECTOR = 250
AZTOOLS_TIMEOUT = 120

C = 0.1  # for linear svm
# large value -> smaller margin hyperplane if less misclassifications
# small value -> larger margin hyperplane even when misclassifications

directory = str(os.path.abspath(__file__)).replace("\\\\", "\\")[:-1*len("lib.py")]
TRAINING_PDFS_DIRECTORY = directory + "training_pdfs"
LABELED_SENTENCES_DIR = directory + "training_data\labeled_articles"
RAW_SENTENCS = directory + "sentences.txt"
WORD_LIST = directory + "complete_word_list.txt"
TESTING_DATA = directory + "testing_data.txt"
TRAINING_DATA = directory + "training_data.txt"
STOP_WORDS = directory + "stopwords.txt"
COMPLETE_TRAINING_SET = directory + "complete_training_dataset.txt"
FINAL_DATASET = directory + "final_dataset.txt"
OUTPUT_FILE = directory + "output.txt"

def get_all_pdf():
    print "Grabbing all PDFs from directory"
    list_files = []
    for file in os.listdir(TRAINING_PDFS_DIRECTORY):
        if file.endswith(".pdf"):
            list_files.append(str(TRAINING_PDFS_DIRECTORY) + "/" + str(file))
    return list_files

def get_abstracts(list_files):
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
            modified_txt_response = str_response.replace("\"{", "{").replace("}\"", "}")
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
    return abstracts

def process_abstracts_to_words_csv(abstracts):
    # output: testing_data.txt. each sentence on a separate line, each word separated by comma, words are stemmed
    print "Processing raw abstracts"

    processed_abstracts = []
    # list of abstracts, each abstract is a list of sentences, each sentence is a list of tokenized words

    # stop_words = set(stopwords.words("english"))

    with open(TESTING_DATA, "w+") as testing_file:
        for a in abstracts:
            abstract = a[1]

            tokenized_sents = sent_tokenize(abstract)
            # list of tokenized sentences

            final_words = []
            # specific to an abstract. list of sentences, each sentence is list of tokenized and stemmed words

            for tok_sent in tokenized_sents:
                words_wstop = word_tokenize(tok_sent)
                final_words.append(words_wstop)
                output_line = ""
                for i in final_words:
                    output_line += i
                    output_line += ","
                output_line = output_line[:-1]
                testing_file.write(output_line)
            processed_abstracts.append([a[0], final_words])
    return processed_abstracts

def get_sentences():
    sentences = []
    with open(TESTING_DATA, "rb") as sentences_file:
        data = sentences_file.readlines()
        for line in data:
            sentences.append(line)
    return sentences

def get_popular_words_from_abstracts(processed_abstracts):
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

def get_popular_words_from_sentences(sentences):
    print "Getting all possible words"
    all_words = dict()    
    stop_words = []
    nltk_stopwords = set(stopwords.words('english'))
    common_words = ['.', ',', ':', ')', '('] 
    with open(STOP_WORDS, "rb") as stopwords_file:
        data = stopwords_file.readlines()
        for line in data:
            stop_words.append(line.strip('\n'))
        for s in sentences:
            tok_words = word_tokenize(s)
            for word in tok_words:
                if word in stop_words or word in common_words:
                    continue
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

def get_word_list():
    output_words = []
    with open(WORD_LIST, "rb") as input_file:
        input_words = input_file.readlines()
        for word in input_words:
            output_words.append(word.strip("\r\n"))
    return output_words

def create_feature_vector_from_raw_sent(sentence, features):
    # second paramter can be get_popular_words() or get_word_list()
    f_vector = []
    for i in range(len(features)):
        f_vector.append(0)
    tok_words = word_tokenize(sentence)
    for word in tok_words:
        if word in features:
            index = features.index(word)
            f_vector[index] = 1
    return f_vector

def create_feature_vector_from_tok_sent(sentence, features):  
    # first parameter is a list of tokenized and stemmed words
    # list of sents can be obtained from process_sentences_to_words_csv()
    f_vector = []
    for i in range(len(features)):
        f_vector.append(0)
    for word in sentence:
        if word in features:
            index = features.index(word)
            f_vector[index] = 1
    return f_vector

def get_training_txt_files():
    list_files = []
    for file in os.listdir(LABELED_SENTENCES_DIR):
        if file.endswith(".txt"):
            list_files.append(str(LABELED_SENTENCES_DIR) + "\\" + str(file))
    return list_files

def combine_training_sentences(list_files):
    with open(TRAINING_DATA, "w+") as output_file:
        for file in list_files:
            with open(file, "rb") as input_file:
                data = input_file.readlines()
                for line in data:
                    new_line = None
                    if line[0] == "#":
                        continue
                    elif line[4] == " ":
                        new_line = line[:4] + "\t" + line[5:]
                    elif line[4:6] == "--":
                        new_line = line[:4] + "\t" + line[6:]
                    else:
                        new_line = line
                    info = new_line.split("\t")
                    if len(info) <= 1:
                        print "Less than = to size 1"
                        continue
                    class_id = 0
                    class_val = info[0].strip(" ")
                    if class_val == "AIMX" or class_val == "OWNX":
                        class_id = 0
                    elif class_val == "BASE" or class_val == "CONT" or class_val == "MISC":
                        class_id = 1
                    else:
                        print "Could not identify:", class_val
                        continue
                    line_to_write = str(class_id) + "\t" + info[1]
                    output_file.write(line_to_write)

def clean_complete_dataset():
    with open(COMPLETE_TRAINING_SET, "rb") as input_file, open(FINAL_DATASET, "w+") as output_file:
        data = input_file.readlines()
        for line in data:
            if line[1] == " ":
                if line[0] == 2:
                    output_file.write(str(1) + "\t" + line[2:])
                else:
                    output_file.write(line[0] + "\t" + line[2:])    
            elif line[1:3] == "--":
                if line[0] == 2:
                    output_file.write(str(1) + "\t" + line[3:])
                else:
                    output_file.write(line[0] + "\t" + line[3:])
            else:
                if line[0] == 2:
                    output_file.write(str(1) + line[1:])
                else:
                    output_file.write(line)

def get_all_labeled_sentences(percent_training, file_name):
    training_sentences = []
    testing_sentences = []
    total = []
    with open(file_name, "rb") as input_file:
        data = input_file.readlines()
        for line in data:
            info = line.split("\t")
            class_id = info[0]
            if len(info) <= 1:
                print info
                continue
            sentence = info[1]
            total.append([class_id, sentence])
    random.shuffle(total)
    thres = int(len(total) * percent_training)
    training_sentences = total[:thres]
    testing_sentences = total[thres:]
    return training_sentences, testing_sentences

def svm_train(x, y):
    lin_clf = svm.LinearSVC(C=C)
    lin_clf.fit(x, y)
    return lin_clf

def svm_predict(lin_clf, test_sample):
    return lin_clf.predict([test_sample])

def svm_get_accuracy(lin_clf, X, y):
    return lin_clf.score(X, y)

def train_and_test_on_main():

    # # Option 1: (training data 1)
    # print "Cleaning dataset"
    # clean_complete_dataset()

    # Option 2: (training data 2)
    print "Getting training data"
    files = get_training_txt_files()
    combine_training_sentences(files)

    #---------- May use option one or two

    # get features
    print "Getting word list"
    word_list = get_popular_words_from_sentences(get_sentences())#get_word_list()

    total_num_sents = 0
    average_accuracy = 0
    for i in range(100):
        # structure data

        # if want to use 1st dataset for training (the same), use file name 'final_dataset.txt' and uncomment option 1
        training_labeled_data, testing_labeled_data = get_all_labeled_sentences(.9, TRAINING_DATA)
        total_num_sents = len(training_labeled_data) + len(testing_labeled_data)
        
        # train model
        X = []
        y = []
        for case in training_labeled_data:
            X.append(create_feature_vector_from_raw_sent(case[1], word_list))
            y.append(case[0])
        model = svm_train(X, y)
        
        # test model
        X = []
        y = []
        for case in testing_labeled_data:
            X.append(create_feature_vector_from_raw_sent(case[1], word_list))
            y.append(case[0])
        acc = svm_get_accuracy(model, X, y) * 100
        print "Accuracy " + str(i+1) + str(": %.2f" % round(acc,2)) + "%"
        average_accuracy += acc
    print "--------------------------"
    average_accuracy /= 100
    print "Average Accuracy: " + str("%.2f" % round(average_accuracy,2)) + "%"
    print "C = " + str(C)
    print "Total sentences: " + str(total_num_sents) + "\n90% trained, 10% tested"

def apply_model_to_sentences():
    # # Option 1:
    # print "Cleaning dataset"
    # clean_complete_dataset()

    # Option 2:
    print "Getting training data"
    files = get_training_txt_files()
    combine_training_sentences(files)

    #---------- May use option one or two
    # get features
    print "Getting word list"
    word_list = get_popular_words_from_sentences(get_sentences())#get_word_list()

    # train model
    print "Training model"
    X = []
    y = []

    # if want to use 1st dataset for training (the same), use file name 'final_dataset.txt' and uncomment option 1
    training_labeled_data, testing_labeled_data = get_all_labeled_sentences(1, TRAINING_DATA)
    for case in training_labeled_data:
        X.append(create_feature_vector_from_raw_sent(case[1], word_list))
        y.append(case[0])
    model = svm_train(X, y)

    # apply model on abstracts' sentences
    print "Applying model to sentences from abstracts"
    sents = get_sentences()
    features = []
    for sent in sents:
        fv = create_feature_vector_from_raw_sent(sent, word_list)
        features.append([fv, sent])
    with open(OUTPUT_FILE, "w+") as output_file:
        for i in range(len(features)):
            output_line = str(svm_predict(model, features[i][0])[0]) + "\t" + str(sents[i]) + "\n"
            output_file.write(output_line)

    print "See output.txt for results"

def get_summary_from_abstract():

    if len(sys.argv) != 2:
        print "Bad exit code!"
        return 1

    # # Option 1:
    # print "Cleaning dataset"
    # clean_complete_dataset()

    # Option 2:
    #print "Getting training data"
    files = get_training_txt_files()
    combine_training_sentences(files)

    #---------- May use option one or two

    # get features
    #print "Getting word list"
    word_list = get_popular_words_from_sentences(get_sentences())#get_word_list()

    # train model
    #print "Training model"
    X = []
    y = []
    # if want to use 1st dataset for training (the same), use file name 'final_dataset.txt' and uncomment option 1
    training_labeled_data, testing_labeled_data = get_all_labeled_sentences(1, TRAINING_DATA)
    for case in training_labeled_data:
        X.append(create_feature_vector_from_raw_sent(case[1], word_list))
        y.append(case[0])
    model = svm_train(X, y)

    filename = sys.argv[1]
    summary = ""
    
    input_filename = directory + "/abs_to_summ/" + filename + "_abstract.txt"
    output_filename = directory + "/abs_to_summ/" + filename + "_summary.txt"

    with open(input_filename, "rb") as input_file, open(output_filename, "w+") as output_filename:
    	paragraph = input_file.readlines()[0]
    	sentences = sent_tokenize(paragraph)
    	# print "Abstract:\n-------------------"
    	# print paragraph
    	summary_sents = []
    	nonsummary_sents = []
    	for sent in sentences:
	        fv = create_feature_vector_from_raw_sent(sent, word_list)
	        if int(svm_predict(model, fv)[0]) == 0:
	        	output_filename.write(sent + "\n")
	        	summary_sents.append(sent)
	        else:
	        	nonsummary_sents.append(sent)
    	# print "-------------------"
    	# print "Summary:\n-------------------"
    	# for i in range(len(summary_sents)):
    	# 	print str(i + 1) + ". " + summary_sents[i]
    	# print "-------------------"
    	# print "Non-summary:\n-------------------"
    	# for i in range(len(nonsummary_sents)):
    	# 	print str(i + 1) + ". " + nonsummary_sents[i]
    	# print "-------------------"

if __name__ == '__main__':

    # apply_model_to_sentences()
    # train_and_test_on_main()
    get_summary_from_abstract()
    