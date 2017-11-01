'''
Created on 11.09.2017

@author: mac
'''
import math
import numpy

from PIL import Image

class Mapper(object):

    def __init__(self, widthInMeter, squaresPerMeter=1):
        self.widthInMeter = float(widthInMeter)
        self.squaresPerMeter = squaresPerMeter
        
        self.pixelPerMeter = None
        
        self.threshhold = (250, 250, 250);
        self.matrix = numpy.array([]);
        
       
    """
    Create a binary matrix from a picture with given original width [m] to
    calculate the respective scale (pixelPerMeter)
    White areas are considered as free and 0 is inserted into the matrix
    Areas whose color is below self.threshhold are considered as blocked
    and marked with 1.
    """
        
    def createMatrix(self, imageurl, createBitMap = False):
        img = Image.open(imageurl)
        
        # Divide image in sections
        width, height = img.size
        print(str(width) + "x" + str(height))
    
        self.pixelPerUnit = int(width / (self.widthInMeter * self.squaresPerMeter))
        print("Pixel per unit: " + str(self.pixelPerUnit))

        size = (int(math.ceil(width / self.pixelPerUnit)), int(math.ceil(height / self.pixelPerUnit)))      
        print("Matrix size: " + str(size))
    
        # fill matrix with zeros
        self.matrix = numpy.zeros(size, dtype=numpy.int)

        for i in range(size[0]):
            for j in range(size[1]):
                crop_rectangle = (i *  self.pixelPerUnit, (size[1] - 2 - j) *  self.pixelPerUnit, (i + 1) *  self.pixelPerUnit, (size[1] - 1 - j) *  self.pixelPerUnit)
                cropped_img = img.crop(crop_rectangle)
                
                if self.readImage(cropped_img):
                    self.matrix[i][j] = 1  
             
                        
        self.matrix = numpy.rot90(self.matrix, 2)
                    
        if createBitMap:
            fi = open("created-bitmap.txt", 'w')
            for arr in self.matrix:
                for j in arr:
                        fi.write(str(j))
                fi.write("\n")    
            fi.close()
                           
        return self.matrix
        
        
        
    def readImage(self, img):

        # put a higher value if there are many colors in your image
        colors = img.getcolors(256)
        
        color = self.getMostFrequentColor(colors)
        
        if color < self.threshhold:
            return True
        
        return False
        
        
    def getMostFrequentColor(self, colors):
        
        max_occurence, most_present = 0, 0
        try:
            for c in colors:
                if c[0] > max_occurence:
                    (max_occurence, most_present) = c
            return most_present
        except TypeError:
            raise Exception("Too many colors in the image")
