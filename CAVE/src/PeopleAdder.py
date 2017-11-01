'''
Created on 11.10.2017

@author: mac
'''
import viz
import vizact
import vizfx

import random

class PeopleAdder:
    
    fixedBottomHeight = 2.5
    
    fixedCoordinates = [([20, fixedBottomHeight ,78], [-90,0,0]),
                ([27, fixedBottomHeight,80], [90,0,0]),
                ([47, fixedBottomHeight,95], [-90,0,0]),
                ([54, fixedBottomHeight ,95], [90,0,0]),
                ([74, fixedBottomHeight ,95], [-90,0,0]),
                ([81, fixedBottomHeight ,95], [90,0,0]),
                ([113, fixedBottomHeight ,97], [-90,0,0]),
                ([120, fixedBottomHeight ,95], [90,0,0]),
                ([144, fixedBottomHeight ,95], [-90,0,0]),
                ([151, fixedBottomHeight ,95], [90,0,0]),
                ([180.5, fixedBottomHeight ,97], [-90,0,0]),
                ([187, fixedBottomHeight ,95], [90,0,0]),
                ([222.5, fixedBottomHeight ,95], [-90,0,0]),
                ([229, fixedBottomHeight ,95], [90,0,0]),
                ([126, fixedBottomHeight ,131], [180,0,0]),
                ([106, fixedBottomHeight ,131], [180,0,0]),
                ([69, fixedBottomHeight ,131], [180,0,0]),
                ([125, fixedBottomHeight + 0.3,80], [-90,0,0]),
                ([131, fixedBottomHeight + 0.3,81], [90,0,0])]
    
    def __init__(self, mapManager):
        self.mapManager = mapManager

    
    def addPeopleAtfixedPosition(self):
        for pos, euler in self.fixedCoordinates:
            avatar = 'vcc_male.cfg'
            if((int)(random.random() * 10)) % 3 == 0:
                avatar = 'vcc_female.cfg'
            if((int)(random.random() * 10)) % 3 == 1:
                avatar = 'vcc_male2.cfg'
                
            person = vizfx.addAvatar(avatar)
            person.setPosition(pos)
            person.setEuler(euler)
            person.state(1)

    """
    Pick random directory and step in that direction for
    distance [m]. Goal is reached if distance can either be
    completely walked or a blocked field is found.
    """
    def findNextGoal(self, start, distance):

        distance = distance * self.mapManager.mapper.squaresPerMeter

        nextPos = [(int) ((start[0] + self.mapManager.offsetX) *  self.mapManager.mapper.squaresPerMeter),start[1],(int) ((start[2] + self.mapManager.offsetY) *  self.mapManager.mapper.squaresPerMeter)]

        size = self.mapManager.mapper.matrix.shape
    
        """
        Determine random direction. If picked direction is blocked
        check next. This iteration might be done for each direction.
        """
        check = False
    
        cntr = 0
        moveDir = random.randint(0, 3) % 4
        
        while cntr < 4 and check == False:
            if moveDir == 0 and nextPos[0] + 1 < size[0] and size[0] and self.mapManager.mapper.matrix[nextPos[0] + 1][nextPos[2]] == 0:
                check = True
            elif moveDir == 1 and nextPos[0] - 1 >= 0 and self.mapManager.mapper.matrix[nextPos[0] - 1][nextPos[2]] == 0:
                check = True
            elif moveDir == 2 and nextPos[2] + 1 < size[1] and size[1] and self.mapManager.mapper.matrix[nextPos[0]][nextPos[2] + 1] == 0:
                check = True
            elif moveDir == 3 and nextPos[2] - 1 >= 0 and self.mapManager.mapper.matrix[nextPos[0]][nextPos[2] - 1] == 0:
                check = True
            else:
                moveDir = (moveDir + 1)  % 4
                cntr = cntr + 1
        
        if check == False:
            print ("can not move")
            return start
            
        # Find max distance
        while distance > 0:
            if moveDir == 0 and nextPos[0] + 1 < size[0] and self.mapManager.mapper.matrix[nextPos[0] + 1][nextPos[2]] == 0:
                nextPos[0] = nextPos[0] + 1
            elif moveDir == 1 and nextPos[0] - 1 >= 0 and self.mapManager.mapper.matrix[nextPos[0] - 1][nextPos[2]] == 0:
                nextPos[0] = nextPos[0] - 1
            elif moveDir == 2 and nextPos[2] + 1 < size[1] and self.mapManager.mapper.matrix[nextPos[0]][nextPos[2] + 1] == 0:
                nextPos[2] = nextPos[2] + 1
            elif moveDir == 3 and nextPos[2] - 1 >= 0 and self.mapManager.mapper.matrix[nextPos[0]][nextPos[2] - 1] == 0:
                nextPos[2] = nextPos[2] - 1
            else:
                break
                
            distance = distance-1

        
        # Create space for turns
        if moveDir == 0:
            nextPos[0] = nextPos[0] - 1
        elif moveDir == 1:
            nextPos[0] = nextPos[0] + 1
        elif moveDir == 2:
            nextPos[2] = nextPos[2] - 1
        elif moveDir == 3:
            nextPos[2] = nextPos[2] + 1

        
        nextPos = [(int) (nextPos[0] /  self.mapManager.mapper.squaresPerMeter - self.mapManager.offsetX), start[1], (int) (nextPos[2] /  self.mapManager.mapper.squaresPerMeter - self.mapManager.offsetY)]
    
        return nextPos
        
        
        
    """
    Add people non-blocked fields based on the Mapper's bitmap
    Afterwards, random routes are calculated. Therefore, a random
    distance is taken and findNextGoal... is called 5 times per Person.
    """
    def addPeople(self):
        
        ### Add people with random route
        for i in range(0,60):
            
            avatar = 'vcc_male.cfg'
            if((int)(random.random() * 10)) % 3 == 0:
                avatar = 'vcc_female.cfg'
            if((int)(random.random() * 10)) % 3 == 1:
                avatar = 'vcc_male2.cfg'
            
            person = vizfx.addAvatar(avatar)
            
            x = self.eliminateRoundingError(random.randint(20, 250))
            y = self.eliminateRoundingError(random.randint(20, 150))
            pos = [x, 2.5, y]
            
            while self.mapManager.mapper.matrix[(int) ((pos[0] + self.mapManager.offsetX) *  self.mapManager.mapper.squaresPerMeter)][(int) ((pos[2] + self.mapManager.offsetY) *  self.mapManager.mapper.squaresPerMeter)]== 1:
                x = self.eliminateRoundingError(random.randint(20, 250))
                y = self.eliminateRoundingError(random.randint(20, 150))
                pos = [x, 2.5, y]
    
            person.setPosition(pos)
            
            actions = []
            actions.append(pos)
            
            for j in range(0,5):
                nextPos = self.findNextGoal(pos, random.randint(10, 30))
            
                if pos != nextPos:
                    actions.append(vizact.walkTo(nextPos))
                
                pos = nextPos
            
            
            # Add ways back
            
            a = actions
            for act in reversed(a):
                actions.append(act)
                
            actions.pop()
            
            if len(actions) > 1:
                person_seq = vizact.sequence(actions, viz.FOREVER)
                person.runAction(person_seq)
            else:
                person.state(1)
                
                
    def eliminateRoundingError(self, x):
        return x + x % self.mapManager.mapper.squaresPerMeter