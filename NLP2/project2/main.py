import xml.etree.ElementTree as ET
import nltk
from nltk.stem import *
from nltk.stem.wordnet import WordNetLemmatizer

pos = "pos"
senseID = "senseID"
prev = "prev"
target = "target"
next = "next"
sense = "sense"
num = "num"
lst = "lst"
wordnet = "wordnet"
gloss = "gloss"
examples = "examples"
id = "senseID"
item = "item"
word_tag = "word"
target_token = "TARGET"

# returns a sentence in lowercase
def lower_sentence(sentence):
    sentence_list = sentence.split(" ")
    sentence_out = ""
    for i in range(len(sentence_list)):
        sentence_out += " " + sentence_list[i].lower()
    return sentence_out[1:]

# parses a data file into the following format:
# {word -> [{pos -> pos, senseID -> sense id, prev -> prev content, next -> next content, target -> target word}]}
def format_data(dataFile):
    output_dict = {}
    f = open(dataFile, "r")
    for line in f:
        lineArr = line.split(" | ");
        wordDotPos = lineArr[0].split(".")
        if not wordDotPos[0] in output_dict:
            output_dict[wordDotPos[0]] = []
        wordMap={}
        wordMap[pos] = wordDotPos[1]
        wordMap[senseID] = lineArr[1]
        # split the third element into the prev content, target word, and next content
        # NOTE: prev or next may not always be there
        prevTargetNext = lineArr[2].split(" %% ") 
        if len(prevTargetNext) == 3:
            wordMap[prev] = prevTargetNext[0]
            wordMap[target] = prevTargetNext[1].lower()
            wordMap[next] = prevTargetNext[2]
        else:
            if lineArr[2].index("%%") > float(len(lineArr[2]))/2.0: # second half: we have a prev but no next
                wordMap[prev] = prevTargetNext[0]
                wordMap[target] = prevTargetNext[1].lower()
                wordMap[next] = ""
            else:
                wordMap[prev] = ""
                wordMap[target] = prevTargetNext[0].lower()
                wordMap[next] = prevTargetNext[1]
        # convert to lowercase
        wordMap[prev] = lower_sentence(wordMap[prev])
        wordMap[next] = lower_sentence(wordMap[next])
        lst = output_dict[wordDotPos[0]]
        lst.append(wordMap)
        output_dict[wordDotPos[0]] = lst
    return output_dict

# parses a data file into the following format (in a list in order):
# {[{word_tag -> word, pos -> pos, senseID -> sense id, prev -> prev content, next -> next content, target -> target word}]}
def format_data_to_list(dataFile):
    output_list = []
    f = open(dataFile, "r")
    for line in f:
        lineArr = line.split(" | ");
        wordDotPos = lineArr[0].split(".")
        wordMap={}
        wordMap[word_tag] = wordDotPos[0]
        wordMap[pos] = wordDotPos[1]
        wordMap[senseID] = lineArr[1]
        # split the third element into the prev content, target word, and next content
        # NOTE: prev or next may not always be there
        prevTargetNext = lineArr[2].split(" %% ")
        if len(prevTargetNext) == 3:
            wordMap[prev] = prevTargetNext[0]
            wordMap[target] = prevTargetNext[1].lower()
            wordMap[next] = prevTargetNext[2]
        else:
            if lineArr[2].index("%%") > float(len(lineArr[2]))/2.0: # second half: we have a prev but no next
                wordMap[prev] = prevTargetNext[0]
                wordMap[target] = prevTargetNext[1].lower()
                wordMap[next] = ""
            else:
                wordMap[prev] = ""
                wordMap[target] = prevTargetNext[0].lower()
                wordMap[next] = prevTargetNext[1]
        # convert to lowercase
        wordMap[prev] = lower_sentence(wordMap[prev])
        wordMap[next] = lower_sentence(wordMap[next])
        output_list.append(wordMap)
    return output_list

def format_list_to_map(data_list):
    output_dict = {}
    for item in data_list:
        wordName = item[word_tag]
        if not wordName in output_dict:
            output_dict[wordName] = []
        lst = output_dict[wordName]
        lst.append(item)
        output_dict[wordName] = lst
    return output_dict



# replace nested double quotes with single quotes to make it readable by the xml parser
# NOTE: removed this from the "drug" word b/c it messed up the parsing (see piazza post 192) (ALSO UPDATED THE NUMBER IN THE HEADER (originally <lexelt item="drug.n" num="3"> but changed "3" to "2"))
# <sense id="1&&2" wordnet="" gloss="Senses 1 and 2" examples="Some drugs, both prescription and non-prescription, are frequently abused. | All my drugs are smuggled in from Canada. | Some drugs can interact with one another." />
def fix_xml():
    f = open("files/dictionary.xml","r")
    t = open("files/theo.xml", "w")
    for line in f:
        if "sense id=" in line:
            example_start = line.index("examples=\"")
            example = line[example_start+len("examples=\""):len(line)-5]
            if "\"" in example:
                example = example.replace("\"", "'")
                modifiedLine = line[:example_start+(len("examples=\""))] + example + "\" />"
                t.write(modifiedLine)
            else:
                t.write(line)
        else:
            t.write(line)
    f.close()
    t.close()

# parse the dictionary into the format {word -> {senseID, num, [wordnet, gloss, [examples]]]}
# NOTE: assumes fix_xml was called first
def parse_xml():
    fix_xml()
    tree = ET.parse("files/theo.xml")
    root = tree.getroot()
    top_level = root.findall('.')
    lexelts = root.findall("./lexelt")
    word_dictionary = {}
    for lex in lexelts:
        wordDotSense = lex.get(item)
        wordDotSense = wordDotSense.split(".")
        word = wordDotSense[0].lower()
        dotSense = wordDotSense[1]
        inner_dict = {}
        inner_dict[sense] = dotSense
        inner_dict[num] = lex.get(num)
        inner_dict[lst] = []
        for example in lex:
            senseID = example.get(id)
            wordNet = example.get(wordnet)
            Gloss = example.get(gloss)
            Examples = example.get(examples)
            split_examples = Examples.split(" | ")
            # convert to lowercase
            for i in range(len(split_examples)):
                split_examples[i] = lower_sentence(split_examples[i])
            d = {}
            d[id] = senseID
            d[wordnet] = wordNet
            d[gloss] = Gloss
            d[examples] = split_examples
            list = inner_dict[lst]
            list.append(d)
            inner_dict[lst] = list
        word_dictionary[word] = inner_dict
    return word_dictionary 

# stems a word
def stem(word):
    stemmer = PorterStemmer()
    return stemmer.stem(word)

# tokenizes sentences, returning a single list of tokens for all sentencees
def tokenize(sentences):
    sent_detector = nltk.data.load('tokenizers/punkt/english.pickle')
    sentences = sent_detector.tokenize(sentences.strip())
    lst = []
    for sentence in sentences:
        tokens = nltk.word_tokenize(sentence)
        for t in tokens:
            lst.append(t)
    return lst

# lemmatizes a word
# run "sudo python -m nltk.downloader -d /usr/share/nltk_data all" to install the word net corpus
def lemmatize(word, pos=""):
    lmtzr = WordNetLemmatizer()
    if len(pos) == 0:
        return lmtzr.lemmatize(word)
    return lmtzr.lemmatize(word, pos)

