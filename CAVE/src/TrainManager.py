import viz
import vizact
import vizfx

import random

"""
This class copies a given train Object to given coordinates.
Afterwards, it randomly sets these trains to offPosition and hides
them or sets them to their start position in the station.
You can order or send trains by using respective functions as
described as follows.
"""


class TrainManager:
	
	# z-Goal of trains leaving the station
	offPosition = 300
	
	# start Coordinates of added trains
	trainCoordinates = [(20.9, 0), (38.7, 0), (57.6, 0), (70, 0), (89.4, 0), (95.9, 0), (126.5, 0), (146.4, 0), (126.1, 0), (165.2, 0), (183.1, 0), (205.7, 6)]
	
	# List of trains that are on the run
	travellingTrains = []
	
	# List of trains that are forced to stay
	forcedTrains = []
	
	def __init__(self, mapManager, referenceNode):
		self.mapManager = mapManager
		self.referenceNode = referenceNode
		
		self.trainObj = []
		

	"""
	Create a copy of the given train for each coordinate
	given in trainCoordinates
	"""
	def addTrains(self):
		if self.referenceNode == None:
			return
		
		for (x, y) in self.trainCoordinates:
			obj = self.referenceNode.copy()
			
			if random.randint(0, 1) % 2 == 0:
				y = self.offPosition
				obj.visible(viz.OFF)
			
			obj.setPosition([x,0,y], viz.ABS_GLOBAL)
			self.trainObj.append(obj)


	def startTrainDynamic(self):
		if len(self.trainObj) == 0:
			return
			
		print("start movin")
		
		cntr = 0
		for obj in self.trainObj:			
			pos = obj.getPosition(viz.ABS_GLOBAL)
			
			if pos[2] == self.offPosition:
				pos[2] = pos[2] - self.offPosition
			else:
				pos[2] = pos[2] + self.offPosition

			obj.runAction(vizact.moveTo(pos, obj.getPosition(), 3))
			
			
			
	# Start a train animation to come to station
	def orderTrainByIdx(self, idx, force = False):
		self.updateOrderList()
	
		if idx < len(self.trainObj) and idx < len(self.trainCoordinates) and (self.trainObj[idx] not in self.travellingTrains or force == True):
			pos = [self.trainCoordinates[idx][0], 0, self.trainCoordinates[idx][1]]
			self.trainObj[idx].runAction(vizact.moveTo(pos, self.trainObj[idx].getPosition(), 4, 50, vizact.easeInOutQuadratic))
			
			self.trainObj[idx].visible(viz.ON)
							
			self.travellingTrains.append(self.trainObj[idx])
		else:
			print("TrainManager.orderTrainByIdx: " + str(idx) + " no index in " + str(len(self.trainCoordinates)))
			
			
	# Find train for given position by minimizing x-coordinate difference
	def orderFinalTrain(self, pos):
		dist = 200
		i = 0
		for (x, y) in self.trainCoordinates:
			if (pos["longitude"] - x) < dist:
				idx = i
				dist = pos["longitude"] - x
				
			i = i + 1
			
		self.trainObj.append(self.trainObj[idx])
		self.orderTrainByIdx(idx, True)
			
	# Start a train animation to leave station
	def sendTrainByIdx(self,idx):
		self.updateOrderList()
		
		if idx < len(self.trainObj) and idx < len(self.trainCoordinates) and self.trainObj[idx] not in self.travellingTrains and self.trainObj[idx] not in self.forcedTrains:
			pos = [self.trainCoordinates[idx][0], 0, self.offPosition]
			self.trainObj[idx].runAction(vizact.moveTo(pos, self.trainObj[idx].getPosition(), 4, 50, vizact.easeInOutQuadratic))
			
			self.travellingTrains.append(self.trainObj[idx])
		else:
			print("TrainManager.sendTrainByIdx: " + str(idx) + " no index in " + str(len(self.trainCoordinates)))
			
			
	# Check which trains are currently involved in animations
	def updateOrderList(self):
		for obj in self.travellingTrains:
			if obj.getPosition()[2] == 0 or obj.getPosition()[2] == 6:
				self.travellingTrains.remove(obj)
			elif obj.getPosition()[2] == self.offPosition:
				self.travellingTrains.remove(obj)
				obj.visible(viz.OFF)
				
				
	def freeForcedTrains(self):
		self.forcedTrains = []