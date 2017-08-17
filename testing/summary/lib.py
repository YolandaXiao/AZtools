import json
import nltk.classify
import operator
import os
import random
import requests

from nltk.corpus import stopwords
from nltk.stem import PorterStemmer
from nltk.tokenize import sent_tokenize, word_tokenize
from sklearn import svm
from textblob.classifiers import NLTKClassifier

ps = PorterStemmer()
SIZE_WORD_FEATURE_VECTOR = 50
AZTOOLS_TIMEOUT = 120

TRAINING_PDFS_DIRECTORY = "training_pdfs"
LABELED_SENTENCES_DIR = "training_data/labeled_articles"
RAW_SENTENCS = "sentences.txt"
WORD_LIST = "complete_word_list.txt"
TESTING_DATA = "testing_data.txt"


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
                filtered_words = words_wstop  # [w for w in words_wstop if not w in stop_words]
                stemmed_words = []
                for word in filtered_words:
                    stemmed_words.append(ps.stem(word))
                final_words.append(stemmed_words)
                output_line = ""
                for i in final_words:
                    output_line += i
                    output_line += ","
                output_line = output_line[:-1]
                testing_file.write(output_line)
            processed_abstracts.append([a[0], final_words])
    return processed_abstracts


def process_sentences_to_words_csv():
    sentences = []
    with open(RAW_SENTENCS, "rb") as sentences_file:
        data = sentences_file.readlines()
        for line in data:
            sentences.append(line)
    ts_data = []
    with open(TESTING_DATA, "w+") as testing_file:
        for sent in sentences:
            tok_words = word_tokenize(sent)
            line = ""
            s_sent = []
            for word in tok_words:
                try:
                    s_word = ps.stem(str(word).lower())
                    s_sent.append(s_word)
                    line += s_word
                    line += ","
                except Exception as ex:
                    continue
            if line is not None:
                line = line[:-1]
                testing_file.write(line + "\n")
                ts_data.append([s_sent, sent])
            else:
                continue
    return ts_data


def get_popular_words(processed_abstracts):
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


def get_word_list():
    output_words = []
    with open(WORD_LIST, "rb") as input_file:
        input_words = input_file.readlines()
        for word in input_words:
            output_words.append(word.lower().strip("\r\n"))
    unique_words = list(set(output_words))
    return unique_words


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
            list_files.append(str(LABELED_SENTENCES_DIR) + "/" + str(file))
    return list_files


def combine_training_sentences(list_files):
    with open("training_data.txt", "w+") as output_file:
        for file in list_files:
            with open(file, "rb") as input_file:
                data = input_file.readlines()
                for line in data:
                    if line[0] == "#":
                        continue
                    info = line.split("\t")
                    if len(info) <= 1:
                        continue
                    class_id = 0
                    class_val = info[0].strip(" ")
                    if class_val == "AIMX" or class_val == "OWNX":
                        class_id = 0
                    elif class_val == "BASE" or class_val == "CONT":
                        class_id = 1
                    elif class_val == "MISC":
                        class_id = 2
                    else:
                        print "Could not identify:", class_val
                        continue
                    tok_sent = word_tokenize(info[1])
                    ts_words = []
                    output_line = str(class_id) + ","
                    for word in tok_sent:
                        s_word = str(ps.stem(word.lower()))
                        ts_words.append(s_word)
                        output_line += s_word
                        output_line += ","
                    output_file.write(output_line[:-1] + "\n")

def get_all_labeled_sentences():
    training_sentences = []
    testing_sentences = []
    with open('training_data.txt', "rb") as input_file:
        data = input_file.readlines()
        for line in data:
            info = line.split(",")
            class_id = info[0]
            words = info[1:-1]
            if random.random() >= 0.9:
                testing_sentences.append([class_id, words])
            else:
                training_sentences.append([class_id, words])
    return training_sentences, testing_sentences


def get_all_labeled_sentences_not_tok():
    training_sentences = []
    testing_sentences = []
    with open('final_dataset.txt', "rb") as input_file:
        data = input_file.readlines()
        for line in data:
            info = line.split("\t")
            class_id = info[0]
            if len(info) <= 1:
                print info
                continue
            sentence = info[1]
            if random.random() >= 0.9:
                testing_sentences.append([class_id, sentence])
            else:
                training_sentences.append([class_id, sentence])
    return training_sentences, testing_sentences


def svm_train(x, y):
    lin_clf = svm.LinearSVC(C=.01)
    lin_clf.fit(x, y)
    return lin_clf


def svm_predict(lin_clf, test_sample):
    return lin_clf.predict([test_sample])


def clean_complete_dataset():
    with open("complete_training_dataset.txt", "rb+") as input_file, open("final_dataset.txt", "w+") as output_file:
        data = input_file.readlines()
        for line in data:
            if line[1] == " ":
                output_file.write(line[0] + "\t" + line[2:])
            elif line[1:3] == "--":
                output_file.write(line[0] + "\t" + line[3:])
            else:
                output_file.write(line)


if __name__ == '__main__':

    clean_complete_dataset()

    # get features
    word_list = get_word_list()

    average_accuracy = 0
    for i in range(100):
        
        # structure data
        training_labeled_data, testing_labeled_data = get_all_labeled_sentences_not_tok()
        # print "num training", len(training_labeled_data)
        # print "total", len(training_labeled_data) + len(testing_labeled_data)

        # train model
        X = []
        y = []
        for case in training_labeled_data:
            X.append(create_feature_vector_from_raw_sent(case[1], word_list))
            y.append(case[0])
        model = svm_train(X, y)

        # test model
        num_correct = 0
        total = 0
        for case in testing_labeled_data:
            X = (create_feature_vector_from_raw_sent(case[1], word_list))
            y = case[0]
            prediction = svm_predict(model, X)[0]
            if y == prediction:
                num_correct += 1
            total += 1
        accuracy = num_correct * 100.0 / total
        print "Accuracy: " + str(accuracy) + "%"
        average_accuracy += accuracy

    print "--------------------------"
    average_accuracy /= 100
    print "Average Accuracy: " + str(average_accuracy) + "%"

    # # apply model to abstracts
    # sents = process_sentences_to_words_csv()
    # features = []
    # for sent in sents:
    #     fv = create_feature_vector_from_tok_sent(sent[0], word_list)
    #     features.append([fv, sent[0]])
    # with open("output.txt", "w+") as output_file:
    #     for i in range(len(features)):
    #         output_line = str(svm_predict(model, features[i][0])[0]) + "\t" + str(sents[i][1]) + "\n"
    #         output_file.write(output_line)
