'''
Created on 11.09.2017

@author: uk012025
'''
import viz
import vizact
import vizfx

import base64
import json
import time

import mutex
import Queue
from thread import start_new_thread
import threading


from math import fabs
from math import sqrt
from math import pow

from StationClocks import StationClocks
import World

from AStar import AStar
from Mapper import Mapper
from PeopleAdder import PeopleAdder
from RadARCameraHandler import RadARCameraHandler
from TrainManager import TrainManager


class MapManager:

    RECOMMENDER_ABIDANCE_MODE     = "abidanceMode"
    RECOMMENDER_EFFICIENCY_MODE   = "efficiencyMode"

    # 3D Objects
    POI_SYSMBOL = '../resources/poi.osgb'
    NAVIGATION_SYMBOL = '../resources/arrow.osgb'
    CASH_MACHINE = '../resources/model/cashmachine.osgb'
    SNACK_MACHINE = '../resources/model/snackautomat.osgb'
    POST_BOX = '../resources/model/postbox.osgb'

    # Height of cameria
    fixedCameraHeight = 2.45
    
    # Height of POI objects
    fixedPOIHeight = 4.5
        
    # Height of navigation objects
    fixedBottomHeight = 2.5
    
    
    # Threshold in [m] before sending new User position
    positionUpdateThreshold = 2
    finishedNavigationThreshold = 3
    
    restartNavigationThreshold = 20
    
    
    navigationEnabled = False
    cancelNav = False
    
    # List of current items
    items = []
    
    # Storage for POI Symbols
    poiObjects = []
    
    # Storage for productivity items' 3D models
    productivityItemObjects = []
    
    # Storage for added navigation objcts
    navigationObjects = []
        
            
                
    # list of signs for texturing
    signList = []
    shopList = []
    platformShopList = []
    
    
    # user position from where route calculation was started
    navigationStartPos = []
    
    navigationRoute = Queue.Queue()
    
    mqttConnector = None
    
    def __init__(self, groundPlotUrl, widthInMeter, mapUrl, squaresPerMeter=1, offsetX=0, offsetY=0):

        self.offsetX = offsetX
        self.offsetY = offsetY

         # add map
        self.world = World.World()
        #viz.collision(True) 
        self.station = vizfx.addChild(mapUrl)
        self.station.anisotropy(8)
        
        self.initSignList()

        # transform user position according to mapper matrix 
        self.currentUserPosition = viz.MainView.getPosition()
        self.currentUserPosition[0] = self.currentUserPosition[0]
        self.currentUserPosition[2] = self.currentUserPosition[2]

        # ## Scene dynamic
        viz.cam.setHandler(RadARCameraHandler)
        viz.callback(viz.UPDATE_EVENT, self.updatePosition)
        viz.callback(viz.EXIT_EVENT, self.onExit)
        
        # create a matrix to detext obstacles for navigation calculaton
        self.mapper = Mapper(widthInMeter, squaresPerMeter)
        self.mapper.createMatrix(groundPlotUrl)       
        print("Image was mapped to matrix");
 
        self.trainManager = TrainManager(self, self.station.getChild("Train"))
        self.trainManager.addTrains()

        # Add running clocks
        self.clock = StationClocks(self.trainManager)
        self.clock.loadModel(self.station)
        self.clock.setCurrentTime()
        
        # add people into station model
        self.peopleAdder = PeopleAdder(self)
        self.peopleAdder.addPeople()
        self.peopleAdder.addPeopleAtfixedPosition()
        
        
        self.trainManager.sendTrainByIdx(11)
        self.trainManager.sendTrainByIdx(10)
        
        # Necessary for navigation
        self.aStar = AStar()
        
        # Navigation objects
        self.navigationObj = viz.addChild(self.NAVIGATION_SYMBOL)      
        self.poiObj = viz.addChild(self.POI_SYSMBOL)    
        
        # Productivity item objects
        self.cashMachineObject = viz.addChild(self.CASH_MACHINE)
        self.snackMachineObject = viz.addChild(self.SNACK_MACHINE)
        self.postBoxObject = viz.addChild(self.POST_BOX)
        
                     
        
        self.mode = self.RECOMMENDER_ABIDANCE_MODE
        self.navigateTo = None
        
        viz.playSound('../resources/Hauptbahnhof.wav', viz.LOOP)

        
    def setMQTTConnector(self, mqttConnector):
        self.mqttConnector = mqttConnector
        
        
    """
    is called by MQTT message with action=quit
    """
    def onExit(self):
        if self.mqttConnector != None:
            self.mqttConnector.disconnect();


    """
    Fills all nodes and textures that might be used during evaluation in a list.
    These textures might be changed with respect to given items.
    There are 3 sizes of textures used in the model. Because of that three lists
    are filled.
    """
    def initSignList(self):
        
        sign = [21,22,23,24,25,26,27,28,29,30,31,32,45,46]
        shop = [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,36,37,38,39,40,41,42,43]
        backshop = [33,34,35]
                  
        
        for i in sign:
            n = self.station.getChild('Shop' + str(i) + '-Schild', viz.CHILD_REPLACE_TRANSFORM)
            
            self.signList.append((
                i,
                n,
                n.getPosition()[0],
                n.getPosition()[2]))

          
        for i in shop:
            n = self.station.getChild('Shop' + str(i), viz.CHILD_REPLACE_TRANSFORM)
            
            self.shopList.append((
                i,
                n,
                n.getPosition()[0],
                n.getPosition()[2]))

        for i in backshop:
            n = self.station.getChild('Shop' + str(i), viz.CHILD_REPLACE_TRANSFORM)
            
            self.platformShopList.append((
                i,
                n,
                n.getPosition()[0],
                n.getPosition()[2]))
                
        
    
    """
    Replaces the texture of a node with respect to given item.
    Therefore, the distance between each node and the item coordiantes
    is calculated. The texture of the node with minimum distance to item
    coordinates is changed.
    """
    def setTexture(self, item):

        # current minimum distance between item and texture node
        d = 1000
        texture = None
        node = None
        folder = ""
        
        for i,n,x,z in self.signList:
            if n == None:
                print("node not found: Shop" + str(i) + "-Schild")
                continue
            
            nD = sqrt(pow(x - item["geoposition"]["latitude"],2) + pow(z - item["geoposition"]["longitude"],2))
     
            if  nD < d:
                node = n
                texture = 'Shop' + str(i) + '-Schild 2'
                folder = 'sign'
                d = nD
               
        
        # if minimum distance d is smaller than 2 m,the respective node is assumed as best matching node
        # otherwise check dtistances again
        
        if d > 2:
           for i,n,x,z in self.shopList:

                if n == None:
                    print("node not found: Shop" + str(i))
                    continue
                
                nD = sqrt(pow(x - item["geoposition"]["latitude"],2) + pow(z - item["geoposition"]["longitude"],2))
                
                if nD < d:
                    node = n
                    texture = 'Shop' + str(i)
                    folder = 'shop'
                    d = nD
                    
        if d > 2:           
            for i,n,x,z in self.platformShopList:
                
                if n == None:
                    print("node not found: Shop" + str(i))
                    continue
                
                nD = sqrt(pow(x - item["geoposition"]["latitude"],2) + pow(z - item["geoposition"]["longitude"],2))
                
                if nD < d:
                    node = n
                    texture = 'Shop' + str(i) + '-Schild 2'
                    folder = 'platform'
                    d = nD
         
        print ("------- item id: " +str(item["id"]) + "   -------")
        try:
            pic = viz.addTexture('../resources/item-textures/'+folder+'/'+str(item["id"])+'.jpg')
            node.texture(pic, texture)
        except Exception as e:
            print(e)
     

    """
    Sends current user position to controller and manages navigation updates
    """
    def updatePosition(self, e):
        
        if self.mqttConnector == None:
            return
        
        # current position
        cP = viz.MainView.getPosition()
        cP[0] = cP[0] + self.offsetX
        cP[2] = cP[2] + self.offsetY
        
        dx = pow(self.currentUserPosition[0] - cP[0], 2)
        dy = pow(self.currentUserPosition[2] - cP[2], 2)
        
        # distance moved since last measure
        d = sqrt(dx + dy)
        
        # send new position to server, if user moved more than given threshold
        if d > self.positionUpdateThreshold:
            self.currentUserPosition = viz.MainView.getPosition()
            self.currentUserPosition[0] = cP[0]
            self.currentUserPosition[2] = cP[2]

            s = json.dumps({"action":self.mqttConnector.CURRENT_USER_POSITION, "value" : {"latitude": self.currentUserPosition[0], "longitude": self.currentUserPosition[2]}}) 
            self.mqttConnector.publish(self.mqttConnector.SERVER_TOPIC, s)
            

        if self.navigationRoute.empty() == False:
            self.addNavigationObjects(self.navigationRoute.get())
        elif not self.aStar.running and self.navigationEnabled == True and self.navigationRoute.empty() == True:

            # Remove unnecessary navigation objects
            dist = sqrt(pow(self.aStar.goal[0] / self.mapper.squaresPerMeter + self.offsetX - self.currentUserPosition[0], 2) + pow(self.aStar.goal[1] / self.mapper.squaresPerMeter + self.offsetY - self.currentUserPosition[2], 2))
            
            # mark navigation asfinished
            if dist < self.finishedNavigationThreshold:
                self.stopNavigation()
                msg = json.dumps({"action" : "finishedNavigation", "item" : self.navigateTo})
                self.mqttConnector.publish(self.mqttConnector.SERVER_TOPIC, msg)
                return
                
            if d > self.positionUpdateThreshold:
                
                # find minimum distance to route
                # this distance is added to current user position to check
                # whether an object should be removed or not in order to
                # compensate deviations when following these objects
                tmpDistance = -1
                closestDistance = -1
                idx = -1
                
                cP = viz.MainView.getPosition()
                
                for obj in self.navigationObjects:
                    tmpDistance = sqrt(pow(cP[0] - obj.getPosition()[0], 2) + pow(cP[2] - obj.getPosition()[2], 2))
                    if tmpDistance < closestDistance or idx == -1:
                        closestDistance = tmpDistance
                        idx = idx + 1
                    else:
                        break;
                
                if closestDistance > self.restartNavigationThreshold:
                     # recalculate route, if user is more than 15 m distant from route
                    print("recalculate route. distance to route is " + str(closestDistance) + "m")
                    
                    if self.stopNavigation() == True:
                        start_new_thread(self.startNavigation, (self.navigateTo, ))
                
                    return
                elif closestDistance > 5:
                    # do not delete navigation symbols, if user is more than 5 m distant from route
                    return
                
                # remove navigation objects, if user is closer to destination
                for i in range(0, idx + 1):
                    obj = self.navigationObjects[0]
                    self.navigationObjects.remove(obj)
                    obj.remove();
        
        
        elif self.aStar.running and self.navigationEnabled == False and self.navigateTo != None and self.cancelNav == False:
            
            dx = pow(self.navigationStartPos[0] - cP[0], 2)
            dy = pow(self.navigationStartPos[2] - cP[2], 2)
                
            # distance moved since route calculation started
            d = sqrt(dx + dy)
            
            # restart navigation calculation with current user position
            if d > self.restartNavigationThreshold:
                print("updatePosition: cancelNavigation")
                self.stopNavigation()


    """
    Sets camera position
    """
    def setStartPosition(self, pos):
                
        self.stopNavigation(True)
        
        coordinates = [pos["latitude"],self.fixedCameraHeight,pos["longitude"]]
        self.world.setCameraPosition(coordinates)


    """
    Called from MQTTConnector for action=recommendations
    Update textures for current items and add 3D models for productivity items
    """
    def updateItemsCallBack(self, items):
        
        self.stopNavigation()
        
        for obj in self.poiObjects:
            obj.remove()
            
        for obj in self.productivityItemObjects:
            obj.remove();
        
        self.items = items
                
        # Show item POIs in map
        for item in items:
            
            if item["isProductivityItem"]:
                obj = self.poiObj.copy()
                obj.setPosition([item["geoposition"]["latitude"], self.fixedPOIHeight + 0.3, item["geoposition"]["longitude"]])
                obj.visible(viz.OFF)
                    
                pic = None
                
                try:
                    if 'domain' in item and 'image' in item['domain'] and item['domain']['image'] != None:
                        pic = self.addEmbeddedTexture(item["domain"]["image"])
                    else:
                        pic = viz.addTexture("../resources/item-textures/sign/4.jpg")            
                        
                    obj.texture(pic, "attribute-icon-txtr")
                            
                except Exception as e:
                    print(e)
                    
                spin = vizact.spin(0,1,0,90,viz.FOREVER)
                obj.addAction(spin)
                
                self.poiObjects.append(obj)
                
                
                # Add respective 3D model
                itemObj = None
        
                if "EC Automat" == item["name"]:
                    itemObj = self.cashMachineObject.copy()
                elif "Briefkasten" == item["name"]:
                    itemObj = self.postBoxObject.copy()
                else:
                    itemObj = self.snackMachineObject.copy()
                
                itemObj.setPosition([item["geoposition"]["latitude"], self.fixedBottomHeight - 0.5, item["geoposition"]["longitude"]], viz.ABS_GLOBAL)
                
                if item["geoposition"]["longitude"] < 105:
                    itemObj.setEuler([180,0,0])
                    
                self.productivityItemObjects.append(itemObj)
                    
            else:
                self.setTexture(item)
            
        
        if self.mode == self.RECOMMENDER_EFFICIENCY_MODE:
            self.switchRecommenderMode(self.mode, self.navigateTo)
            
        print('set num of items ' + str(len(items)))
          
          
    """
    Must run on MainThread!
    """
    def addNavigationObjects(self, route):
        
        f = self.mapper.squaresPerMeter
            
        # necessary to save both object and position because of rounding errors
        lastObj = None
        lastPos = None
            
        for p in route:

            if lastObj != None and lastPos != None:
                rot = 0
                
                if lastPos[0] < p[0] and p[1] == lastPos[1]:
                    rot = 180
                elif p[1] != lastPos[1]:
                    if lastPos[0] > p[0]:
                        rot = 45
                    elif lastPos[0] < p[0]:
                        rot = 135
                    else:
                        rot = 90
                            
                    if lastPos[1] > p[1]:
                        rot *= -1

                lastObj.setEuler([rot,0,0], viz.ABS_LOCAL)

            try:
                obj = self.navigationObj.copy()
                obj.setPosition([p[0] / float(f), self.fixedBottomHeight, p[1] / float(f)])
                self.navigationObjects.append(obj)
                    
                lastObj = obj
                lastPos = p
            except Exception as e:
                print("ERROR:\n" + str(e))
                
            
        self.navigationObjects.pop().remove();

        # set attribute icon into POI symbol
        pic = None
        if 'domain' in self.navigateTo and 'image' in self.navigateTo['domain'] and self.navigateTo['domain']['image'] != None:
            pic = self.addEmbeddedTexture(self.navigateTo["domain"]["image"])
        else:
            pic = viz.addTexture("../resources/item-textures/sign/4.jpg")
    
        obj = self.poiObj.copy()
        obj.setPosition([p[0] / float(f), self.fixedPOIHeight, p[1] / float(f)])
        obj.texture(pic, "attribute-icon-txtr")

        spin = vizact.spin(0,1,0,90,viz.FOREVER)
        obj.addAction(spin)
            
        self.navigationObjects.append(obj)

        self.navigationEnabled = True
            
        print("Navigation objects added...")
          
          
          
    # Navigation
    
    """
    Start navigation
    Stop current navigation and delete its objects
    Add ne route to queue
    Navigation objects will be added after updatePosition was called
    """
    def startNavigation(self, item):

        print("Start navigation to "+item["name"])
        
        # clear from possible current Navigation
        if self.stopNavigation() == False:
            return
            
        
        self.navigateTo = item
        
        self.navigationStartPos = self.currentUserPosition
                
        #calculate route
        route = self.calculateRoute(self.navigateTo)
        
        
        # send confirmation to controller
        if self.mqttConnector != None and route != None:
            
            msg = json.dumps({"action" : "calculatedRoute", "item" : self.navigateTo})
            
            if route == False:
                msg = json.dumps({"action" : "calculatedRoute", "item" : None})
           
            self.mqttConnector.publish(self.mqttConnector.SERVER_TOPIC, msg)
            
        if route:
            # show navigation objects
            self.navigationRoute.put(route)
            print("Route added to queue...")
        elif route == None:
            self.navigationEnabled = False
            print("Route calculation canceled...")
        else:
            self.navigationEnabled = False
            print("Sorry, there is no way...")
   
        print("Thank you for using A*!\n") 
        
        
    
    # stop navigation
    def stopNavigation(self, reset=False):

        if self.navigationEnabled:
            self.navigationEnabled = False
            # remove navigation symbols
            for obj in self.navigationObjects:
                obj.remove()
                
            self.navigationObjects = []

            self.navigationEnabled = False
        
        if self.aStar.running:
            self.cancelNavigation()
            return False
            
        return True
        
    # Cancel route calculation
    def cancelNavigation(self, reset=False):
        print("cancelNavigation: cancel current calculation")
        self.aStar.cancelCalculation(self)
        self.cancelNav = True
        
        if reset:
            self.navigateTo = None

    def navigationCanceledCallback(self):
        self.cancelNav = False
        
        print("navigationCanceledCallback: Navigation canceled")
        
        if self.navigateTo != None:
            print("navigationCanceledCallback: restart Navigation")
            start_new_thread(self.startNavigation, (self.navigateTo, ))

        

    # calculate route to destination by using AStar class
    # returns False or found path
    def calculateRoute(self, item):
        
        f = self.mapper.squaresPerMeter
        iX = int((item["geoposition"]["latitude"] + self.offsetX) * f + 0.5 * f)
        iY = int((item["geoposition"]["longitude"] + self.offsetY) * f + 0.5 * f)
   
        uX = int(self.currentUserPosition[0] * f)
        uY = int(self.currentUserPosition[2] * f)
   
        print("Current Position: " + str(uX) + ", " + str(uY))
        print("Destination: " + str(iX) + ", " + str(iY) + "\n")
   
        if self.mapper.matrix[iX][iY] == 1:
            if iY > 0 & self.mapper.matrix[iX][iY - 1] == 0:
                iY = iY - 1
            
            if iX > 0 & self.mapper.matrix[iX - 1][iY] == 0:
                iY = iY - 1
            
            if iX > 0 & iY > 0 & self.mapper.matrix[iX][iY - 1] == 0:
                iY = iY - 1
   
        return self.aStar.astar(self.mapper.matrix, (uX, uY), (iX, iY))


    def calculateDistance(self, item):
        
        route = self.calculateRoute(item)
        
        if route:
            distance = len(route) / self.mapper.squaresPerMeter
            msg = json.dumps({"action" : "estimatedDistance", "value" : distance})
            self.mqttConnector.publish(self.mqttConnector.SERVER_TOPIC, msg)
            print("Found a route!")
        else:
            msg = json.dumps({"action" : "estimatedDistance", "value" : -1})
            self.mqttConnector.publish(self.mqttConnector.SERVER_TOPIC, msg)
            print("Sorry, no route found...")


    """
    When RECOMMENDER_EFFICIENCY_MODE is active, POI symbols of
    all productivity items are diplayed
    """
    def switchRecommenderMode(self, mode, item):
        
        self.mode = mode
        self.navigateTo = item
        
        print("current mode: " + mode)
        print("navigateTo: " + str(item))     
        
        if mode == self.RECOMMENDER_EFFICIENCY_MODE and self.navigateTo != None:

            self.trainManager.orderFinalTrain(item["geoposition"])
            
            # show productivity poi symbols
            for i in range(len(self.poiObjects)):
                self.poiObjects[i].visible(viz.ON)
               
            if self.stopNavigation():
                start_new_thread(self.startNavigation, (self.navigateTo, ))
        else:
            self.trainManager.freeForcedTrains()
            
            for i in range(len(self.poiObjects)):
                self.poiObjects[i].visible(viz.OFF)
                
        
            self.stopNavigation()

        
    
    def addEmbeddedTexture(self,image,**kw):
        if image["filename"] == None or image["image"] == None:
            return None
            
        return viz.addTextureFromBuffer(image["filename"], base64.b64decode(image["image"]), **kw)


    """
    Update clocks in VR
    """

    def setSimulatedTime(self, value):
        self.clock.setTime(value["hour"], value["minute"])

    """
    This method sets basic objects according to the created bitmap
    0 is pigeon
    1 is navigation object
    """
    def calibrationHelp(self):
        f = 1.0 / self.mapper.squaresPerMeter

        squareMax = 100 * self.mapper.squaresPerMeter
        
        k = squareMax
        for i in range(50, len(self.mapper.matrix)):
            
            l = squareMax
                
            for j in range(45, len(self.mapper.matrix[i])):
                
                pos = [i * f  - self.offsetX + 0.5 * f, self.fixedBottomHeight, j * f - self.offsetY + 0.5 * f]
                
                if self.mapper.matrix[i][j] == 1:
                    obj = self.navigationObj.copy()   
                    obj.setPosition(pos)
                else:
                    obj = self.poiObj.copy()   
                    obj.setPosition(pos)
                                 
                l = l - 1 
                                    
                if l <= 0:
                    break
                  
            k = k - 1
            
            if k <= 0:
                break
