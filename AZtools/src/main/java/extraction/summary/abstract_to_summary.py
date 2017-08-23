import time

print "Loading libraries..."
start = time.time()
import os
import sys
import time
from nltk.tokenize import sent_tokenize, word_tokenize
from sklearn import svm
from sklearn.externals import joblib
end = time.time()
print "took", (end-start), "seconds"

directory = str(os.path.abspath(__file__)).replace("\\\\", "\\")[:-1*len("abstract_to_summary.py")]
WORD_LIST = directory + "complete_word_list.txt"
MODEL_PKL = directory + "modl_ls.pkl"
LABELED_SENTENCES_DIR = directory + "abs_to_summ"

def get_word_list():
    output_words = []
    with open(WORD_LIST, "rb") as input_file:
        input_words = input_file.readlines()
        for word in input_words:
            output_words.append(word.strip("\r\n"))
    return output_words

def create_feature_vector_from_raw_sent(sentence, features):
    f_vector = []
    for i in range(len(features)):
        f_vector.append(0)
    tok_words = word_tokenize(sentence)
    for word in tok_words:
        if word in features:
            index = features.index(word)
            f_vector[index] = 1
    return f_vector

def svm_predict(lin_clf, test_sample):
    return lin_clf.predict([test_sample])

def get_files():
    list_files = []
    for file in os.listdir(LABELED_SENTENCES_DIR):
        if file.endswith(".txt"):
            list_files.append(str(LABELED_SENTENCES_DIR) + "/" + str(file))
    return list_files

word_list = get_word_list()
model = joblib.load(MODEL_PKL)

while (True):
    try:
        print "Looking for new abstracts to process..."
        list_filenames = get_files()
        for filename in list_filenames:
            a_index = filename.find("_abstract.txt")
            if a_index >= 0:
                s_filename = filename[:a_index] + "_summary.txt"
                if s_filename not in list_filenames:
                    summary = ""
                    print "Found unsummarized abstract in", filename
                    with open(filename, "rb") as input_file, open(s_filename, "w+") as output_filename:
                        data = input_file.readlines()
                        tool_name = data[1]
                        paragraph = data[0]
                        sentences = sent_tokenize(paragraph.encode('utf-8'))
                        for sent in sentences:
                            fv = create_feature_vector_from_raw_sent(sent, word_list)
                            if int(svm_predict(model, fv)[0]) == 0 or tool_name.lower() in sent.lower():
                                summary = summary + sent + " "
                        summary = summary[:-1]
                        output_filename.write(summary)
                    print "Summarized", filename
                else:
                    continue # summary file exists
            else:
                continue # looking at a summary file
        # end for
        time.sleep(0.5)
    except Exception as ex:
        print "Error"
        continue
# will never get here
