import sys, os
import xml.etree.ElementTree as ET
from itertools import islice
import re

def parseSingleLogFile(filename):

	file = open(filename, "r")
	ofile = open('../'+filename+'parse.txt',"w+") 
	count = 0
	xmlparse = 0
	noUseTags = re.compile("<q>|</q>");
	#xmlstring = '<iteration>'
	lines = file.readlines();
	for line in lines:
		if line.startswith('-'):
			count = count+1;
			xmlparse = 1
			continue
			# still adding the last sentence
		if xmlparse == 1 and line.startswith('<q>'):
			line = re.sub(noUseTags, line) + '\n';
			print line
			ofile.write(count +':' + line)
	print count;
		




parseSingleLogFile("node1_magpark.2012-05-27.log")
