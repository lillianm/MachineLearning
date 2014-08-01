import re
import sys,os
from argparse import ArgumentParser
#symbols = {"%hes":"err","%spk":"well","%int":"%laugh","noise":""}
symbols = {"%hes":"","%spk":"","%int":"","noise":""}
addition = {"--":" "}
# how to use those 


def parse_txt_file (fileName, outputPath, mode):
	file1 = open(fileName)
	outDir = os.path.dirname(outputPath)
	if outDir != '' and not os.path.exists(outDir):
		os.makedirs(outDir)
	outputName = outputPath.replace(outDir+"/","")
	print outputName
	
	file2 = open(outDir + "/" + mode + outputName + ".txt.text","a")
	file3 = open(outDir + "/" + mode + outputName + ".txt.feat","a")
	for line in file1:
		line_origin = line
		text = line.split("\t")[-1]
		feat = line.split("\t")[-2]
		# only extract the text part
		pattern = re.compile("[a-zA-Z\s\^'%()\-'<>]+")
		text = re.search(pattern, text)
		if text:
			text = text.group(0)
			# not useful tags 
			noUseTags = re.compile("<//noise>|\n|[()\^\-<>]|noise")
			text = re.sub(noUseTags , "", text)
			# delete space or tab in the beginning of the sentences
			format = re.compile("^\s+")
			for s in symbols:
				text = text.replace(s,symbols.get(s))
				# delete multiple spaces

			text = text.replace("\s{2,}", " ")
			text = re.sub(format, "",text)
			text.replace("%uhhuh","")
			if text:
				file2.write(text+"\n")
				bi_feat = 1;
				if feat=="driver":
					 bi_feat = 1;
				else:
					if feat == "copilot":
						bi_feat = 0;
				file3.write(str(bi_feat)+" "+str(bi_feat)+"\n")

def generate_vocabulary(fileName, mode, vocabulary):
	textFile = open(fileName)
	#vocab = open("vocab_" + mode + ".txt","a")

	for line in textFile:
		pattern = re.compile("[%<>\-/]z|\n")
		nline = re.sub(pattern, "", line)
		nline = nline.replace("\s{2,}"," ")
		words = set(nline.split(" "))
		vocabulary  = vocabulary | words
	return vocabulary

	
def write_vocab(fileName, vocab):
	vocab_file = open(fileName,"w+");
	v = list(vocab)
	v.sort()
	for word in v:
		if word:
			vocab_file.write(word+"\n")

def combine_subset(input_dir, test_dirName, outputPath):
	dir = "../CESAR_data"
	if input_dir:
		dir = input_dir
	fileName = 'data.txt'
	mode = "train";
	oPath = makeOutDirs(outputPath, mode)
	train_vocabulary = set([]);
	test_vocabulary = set([]);
	for root, dirs, files in os.walk(dir):
		for subDir in dirs:
			path = os.path.join(dir, subDir)
			if subDir == test_dirName:
				print "testfile"
				mode = "test"
				for r, d, files in os.walk(path):
					if fileName in files:
						print path

						#oPath = makeOutDirs(outputPath, mode)

						parse_txt_file(os.path.join(path,fileName), outputPath, mode)
						test_vocabulary = generate_vocabulary( oPath + ".txt.text", mode, test_vocabulary);
			else: 
				mode = "train"
				for r, d, files in os.walk(path):
					if fileName in files:
						print path

						#oPath = makeOutDirs(outputPath, mode)

						parse_txt_file(os.path.join(path,fileName), outputPath,mode)
						train_vocabulary = generate_vocabulary(oPath + ".txt.text", mode, train_vocabulary);

	outDir = os.path.dirname(outputPath)
	write_vocab(outDir+"/train_vocab.txt",train_vocabulary)
	write_vocab(outDir+"/test_vocab.txt",test_vocabulary)

def parse_seperate_file(input_dir):
	dir = "../../CESAR_data" 
	if input_dir != None:
		dir = input_dir
	fileName = 'data.txt'
	for root, dirs, files in os.walk(dir):
		for subDir in dirs:
			path = os.path.join(dir, subDir)
			for r, d, files in os.walk(path):
				if fileName in files:
					print path
					parse_txt_file(os.path.join(path,fileName), subDir,subDir); 

def makeOutDirs(outputPath, mode):
	outDir = os.path.dirname(outputPath)
	if outDir != '' and not os.path.exists(outDir):
		os.makedirs(outDir)
	outputName = outputPath.replace(outDir + "/","")
	return outDir + "/" + mode + outputName

#main	
#rewrite the previous files
def main():

	#parse commmand line argumens

	parser = ArgumentParser()

	parser.add_argument("--mode", type=str, choices=["all", "seperate"], 
		help="paser seperately or compile into whole training data", required=True)
	parser.add_argument("--outputfile", type=str, 
		help="output file name = 'output_name'.txt.text", required=True)
	parser.add_argument("--input_dir", type=str, 
		help="path of input directory", required=True)
	parser.add_argument("--testdata", type=str,
		help="the name of the test data set", required=False)

	open("train_cesar_data.txt.text","w+")
	open("test_cesar_data.txt.text","w+")
	open("train_cesar_data.txt.feat","w+")
	open("test_cesar_data.txt.feat","w+")

	# default values:
	test_dataset = "CESAR_May-Tue-29-13-06-47-2012"
	output_filename = "_cesar_data"

	args = parser.parse_args()
	if args.mode == "all":
		if args.testdata != None:
			test_dataset = args.testdata
		if args.outputfile != None:
			output_filename = args.outputfile
			print output_filename
		combine_subset(args.input_dir, test_dataset, output_filename)
		print """ The output files are: \n
			"train_cesar_data.txt.text"\n
			"test_cesar_data.txt.text"\n
			"train_cesar_data.txt.feat"\n
			"test_cesar_data.txt.feat"\n
			"""


	if args.mode == "seperate":
		parse_seperate_file(args.input_dir)
	#parse_seperate_file()

main()
# dir = "../CESAR_data"
# fileName = 'data.txt'
# for root, dirs, files in os.walk(dir):
# 	for subDir in dirs:
# 		path = os.path.join(dir, subDir)
# 		for r, d, files in os.walk(path):
# 			if fileName in files:
# 				print fileName
# 				parse_txt_file(os.path.join(path,fileName), subDir)
# 				generate_vocabulary(subDir + "cesar_data.txt.text", subDir)




# file1 = open("data.txt")
# file2 = open("cesar_data.txt.text","w+")
# file3 = open("cesar_data.txt.feat","w+")












