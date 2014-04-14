import main


def print_map(map):
    for item in map.keys():
        print item + " " + str(map[item]) + "\n"

def increment_map_value(map, key):
    if key in map:
        map[key] += 1
    else:
        map[key] = 1

def update_feature_map(feature_map, word, item):
    #get all feature words
    sentence = item[main.prev]+" "+item[main.next]
    list_of_features = main.tokenize(sentence)
    #stem all words in the sentence
    for i in range(len(list_of_features)):
        list_of_features[i] = main.stem(list_of_features[i])
    #remove duplicates in the sentence
    list_of_features = list(set(list_of_features))
    for f in list_of_features:
        increment_map_value(feature_map, f)

#takes in prev word list and next word lists and window size
#the window size is actually half of the actual window size
#so it returns a list of (window_size) words from prev words
#and (window_size) words from next words
def words_in_window (list_of_features, window_size):
    index = list_of_features.index(main.target_token)
    prev_words = list_of_features[:index]
    next_words = list_of_features[index+1:]
    s1 = min(len(prev_words), window_size)
    s2 = min(len(next_words), window_size)
    return prev_words[len(prev_words)-s1:] + next_words[:s2]



def create_probability_map (data_dict):
    big_map = {}
    for word in data_dict.keys():
        word_data = data_dict[word]
        prob_map = {}
        for item in word_data:
            id = item[main.senseID]
            target = item[main.target]
            
            if id in prob_map:
                #increment the count of this sense
                count = (prob_map[id])[0] + 1
            
            else:
                count = 1
                prob_map[id] = (1, {}, {}) #the third the a map of {target, count}
            #update feature map for that sense
            feature_map = (prob_map[id])[1]
            update_feature_map(feature_map, word, item)
            
            target_map = (prob_map[id])[2]
            if target in target_map:
                target_map[target] += 1
            else:
                target_map[target] = 1

            #assign new tuple value
            prob_map[id] = (count, feature_map, target_map)
        '''
            length = len(word_data)
            
            for key in prob_map.keys():
            count = (prob_map[key])[0]
            feature_map = (prob_map[key])[1]
            for k in feature_map.keys():
            feature_map[k] = (float((feature_map[k]))) / count
            if (feature_map[k]) > 1:
            print "error"
            
            count = float(count) / length
            if count > 1:
            print "error"
            prob_map[key] = (count, feature_map)
            '''

        big_map[word] = prob_map
    return big_map


#data_dict = main.format_data("test.txt")


#data_dict = main.format_data("test.txt")
#print ("Finish data dict")
#map = create_probability_map(data_dict)
#print ("Finish map")
#print_map(map["affect"])
#print_map(map["area"])
#'''
