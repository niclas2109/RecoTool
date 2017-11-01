'''
Created on 08.09.2007

@author: mac
'''

from MapManager import MapManager
from MQTTConnector import MQTTConnector

mapManager = MapManager('../resources/hbf.png', 266, '../resources/model/FFM-HBhf.osgb',2,0,1)

c = MQTTConnector("192.168.12.6", 1883, mapManager)
c.connect()