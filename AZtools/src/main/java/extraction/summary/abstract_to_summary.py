import os
import sys
import time

from nltk.tokenize import sent_tokenize, word_tokenize
from sklearn import svm
from sklearn.externals import joblib

directory = str(os.path.abspath(__file__)).replace("\\\\", "\\")[:-1*len("abstract_to_summary.py")]
WORD_LIST = directory + "complete_word_list.txt"
MODEL_PKL = directory + "modl_ls.pkl"

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

# def create_fvs_from_sentences(sentence, features):
#     f_vector = []
#     for i in range(len(features)):
#         f_vector.append(0)
#     tok_words = word_tokenize(sentence)
#     for word in tok_words:
#         if word in features:
#             index = features.index(word)
#             f_vector[index] = 1
#     return f_vector

def svm_predict(lin_clf, test_sample):
    return lin_clf.predict([test_sample])


start = time.time()

if len(sys.argv) != 3:
    print "Incorrect call to script.\nUsage: 'python abstract_to_summary.py file_name.pdf tool_name'"
    sys.exit(1)

# if len(sys.argv) != 3:
#     print "Incorrect call to script.\nUsage: 'python abstract_to_summary.py paper_abstract tool_name'"
#     sys.exit(1)

word_list = get_word_list()
model = joblib.load(MODEL_PKL)
summary = ""

filename = sys.argv[1]
tool_name = sys.argv[2]

input_filename = directory + "/abs_to_summ/" + filename + "_abstract.txt"
output_filename = directory + "/abs_to_summ/" + filename + "_summary.txt"

mid1 = time.time()
print "start to mid1", mid1-start

with open(input_filename, "rb") as input_file, open(output_filename, "w+") as output_filename:
    mid2 = time.time()
    paragraph = input_file.readlines()[0]
    mid25 = time.time()
    sentences = sent_tokenize(paragraph)
    mid3 = time.time()
    print ": mid2 to 25", mid25-mid2    
    print ": mid2 to 3", mid3-mid2
    for sent in sentences:
        mid4 = time.time()
        fv = create_feature_vector_from_raw_sent(sent, word_list)
        mid5 = time.time()
        print ":: mid4 to 5", mid5-mid4
        if int(svm_predict(model, fv)[0]) == 0 or tool_name.lower() in sent.lower():
            summary = summary + sent + " "
        mid6 = time.time()
        print ":: mid5 to 6", mid6-mid5
    mid7 = time.time()
    print ": mid3 to 7", mid7-mid3
    summary = summary[:-1]
    output_filename.write(summary)
    mid8 = time.time()
    print ": mid7 to 8", mid8-mid7

end = time.time()
print "mid1 to end", end-mid1
print "start to end", end-start
# sentences = sent_tokenize(sys.argv[1])
# for sent in sentences:
#     fv = create_feature_vector_from_raw_sent(sent, word_list)
#     if int(svm_predict(model, fv)[0]) == 0 or tool_name.lower() in sent.lower():
#         summary = summary + sent + " "
# summary = summary[:-1]
# print summary