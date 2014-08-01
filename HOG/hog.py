import numpy as np
from scipy import sqrt, pi, arctan2, cos, sin
from PIL import Image

"""
DEBUG mode will allow you to print some variable
"""

DEBUG = False

"""
Default values are 8*8 cell and 2*2 block/cell
"""
def hog(image, orientations=9, pixels_per_cell=(8, 8),
        cells_per_block=(2, 2), visualise=True, normalise=False):


    if image.ndim < 2:
        raise ValueError(" Please input a 2D Image")

    if image.ndim > 3:
        raise ValueError("Currently only supports grey-level images")

    if normalise:
        image = sqrt(image)

    """
    Gradient Vector Calculation (top-bottom, right-left)
    The top, left, bottom and right line will be discarded, i.e Matrix of m*m
    will be [1,m-1] * [1,m-1]
    """
    grad_x = np.zeros(image.shape)
    grad_y = np.zeros(image.shape)

    grad_x = (image[:,2:] - image[:,:-2])[1:-1,:]
    grad_y = (image[:-2,:] - image[2:,:])[:,1:-1]

    magnitude = sqrt(grad_x ** 2 + grad_y ** 2)
    orientation = arctan2(grad_y, (grad_x + 1e-15)) * (180 / pi) #+ 90
    
    if DEBUG:
        print "The Orientation Vector is :" , orientation

    """
    HOG Feature 
    """

    """
    Calculate Histogram in each cell and split into a histogram of 9 bins
    """
    n_bins = orientations
  
    px, py = image.shape
    cx, cy = pixels_per_cell
    bx, by = cells_per_block

    # Number of cells in Image， axis-x and axis-y
    n_cellsx = int(np.floor(px / cx))  
    n_cellsy = int(np.floor(py / cy))  

    n_blocksx = n_cellsx - 1
    n_blocksy = n_cellsy - 1

    cell_hist = np.zeros((n_cellsx, n_cellsy, n_bins))
    block_hist = np.zeros((n_blocksx, n_blocksy, n_bins * bx * by))


    # Calculate Histogram in Cell
    for i in range(0,n_cellsx):
        for j in range(0,n_cellsy):

            a = np.zeros((1,n_bins))
            bins = [10,30,50,70,90,110,130,150,170]

            for cellx in range(0,cx):
                for celly in range(0,cy):
                    x = i * cx + cellx
                    y = j * cy + celly
                    gap = 180/n_bins
                    for k in range(0, n_bins-1): 
                        """ Split value as a weighted sum of the two nearest bin values"""
                        if orientation[x,y] >= bins[k] and orientation[x,y] <= bins[k+1]:
                            a[0, k] = a[0, k] + (bins[k+1]-orientation[x, y])/20.;
                            a[0, k+1] = a[0, k+1] +  (orientation[x, y]-bins[k])/20.

            #print a
            cell_hist[i,j,:] = a



    # Concatenate Histograms in Blocks and normalize the block
    for i in range(0, n_blocksx):
        for j in range(0, n_blocksy):

            block_hist[i, j, :] = np.concatenate((cell_hist[i,j,:], cell_hist[i+1,j,:] , cell_hist[i, j+1, :] , cell_hist[i+1,j+1,:]), axis=2)
            block_hist[i, j, :] = block_hist[i,j,:] / np.linalg.norm(block_hist[i, j, :])





    # now for each cell, compute the histogram
    #orientation_histogram = np.zeros((n_cellsx, n_cellsy, orientations))

    radius = min(cx, cy) // 2 - 1
    hog_image = None
    if visualise:
        hog_image = np.zeros((py, px), dtype=float)

    if visualise:
        from skimage import draw
        
        for x in range(n_cellsx):
            for y in range(n_cellsy):
                for o in range(0,n_bins):
                    centre = tuple([y * cy + cy // 2, x * cx + cx // 2])
                    dx = int(radius * cos(float(o) / orientations * np.pi))
                    dy = int (radius * sin(float(o) / orientations * np.pi))
                    rr, cc = draw.bezier_curve(centre[0]-dx, centre[0]-dy , centre[0], centre[1],
                    #                        centre[0] + dx, centre[1] + dy,2)
                    hog_image[x*cx:x*cx+8, y*cy : y*cy + 8] += cell_hist[x, y, o]



    """
    The final step collects the HOG descriptors from all blocks of a dense
    overlapping grid of blocks covering the detection window into a combined
    feature vector for use in the window classifier.
    """

    if visualise:
        return  cell_hist, hog_image
    else:
        return  cell_hist

