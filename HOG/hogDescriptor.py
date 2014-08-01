import sys, os
from PIL import Image
import numpy as np
from numpy import array
from hog import hog
from argparse import ArgumentParser

"""
RGB to Gray
"""
def rgb2gray(rgb):
    return np.dot(rgb[...,:3], [0.299, 0.587, 0.144])
 
"""
Wrapper for HOG
"""
def generateHOG(input_dir, output_dir, pixels_per_cell, cells_per_block, v, n):
	filename =input_dir

	# convert to grayimage 
	img = Image.open(filename).convert('L').resize((514,514))
	#arr = rgb2gray(array(img))
	arr = array(img)
	#print arr.shape

	(hogarray, hogimg) = hog(arr, pixels_per_cell=pixels_per_cell, cells_per_block=cells_per_block,visualise=v, normalise=n)
	#print hogimg.shape
	img = Image.fromarray(hogimg);
	img.show()


"""
Parse CommandLine
"""
def main():
	parser = ArgumentParser()

	parser.add_argument("--mode", type=str, choices=["training", "single"],
		help="single to test only one image per time", required = True)

	parser.add_argument("--inputdir", type=str,  
		help="please input dataset directory", required=True)

	parser.add_argument("--outputdir", type=str, 
		help="please input output directory", required=True)

	parser.add_argument("--pixels_per_cell", type=int, 
		help="pixel height and width of each cell", required=False)

	parser.add_argument("--cells_per_block", type = int,
		help="how many cells does one block contains", required=False)

	parser.add_argument("visualise", type= bool,
		#help="whether to visualise image", required=False)

	parser.add_argument("normalise", type=bool,
		#help="whether the normalise the image first", required=False)

	
	 """
	 Default Values
	 """
	input_dir = "../hog/train_data"
	output_dir = "../hog/output"
	pixels_per_cell = (8,8)
	cells_per_block = (2,2)
	visualise = True
	normalise = False

	args = parser.parse_args()

	if args.inputdir!=None:
		input_dir = args.inputdir
	if args.outputdir != None:
		output_dir = args.outputdir
	if args.pixels_per_cell!=None:
		pixels_per_cell = (args.pixels_per_cell, args.pixels_per_cell)
		print pixels_per_cell
	if args.cells_per_block!=None:
		cells_per_block = (args.cells_per_block, args.cells_per_block)
		print type(cells_per_block)

	generateHOG(input_dir, output_dir, pixels_per_cell, cells_per_block, visualise, normalise)


if __name__ == '__main__':
	main()