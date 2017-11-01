### Vizard

import viz
import vizact
import vizfx
import viztask

### Vizard
### User

import World
import BahnhofContent

### User
### Initialisierung

myWorld = World.World()
viz.move(-80, 2.5, -41) #Fraport
viz.collision(True)

### Initialisierung
### Scene static

hbf = vizfx.addChild('FFM-HBhf.osgb')
#hbf.setPosition(80, -2.5, 41)
hbf.anisotropy(8)

uhren = BahnhofContent.GleisUhren()
uhren.loadModel(hbf)
uhren.setTime(12, 20)

### Scene static
### Scene dynamic

BahnhofContent.addPeople()

### Scene dynamic