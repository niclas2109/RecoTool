'''
Created on 11.09.2017

@author: mac
'''
from math import sqrt
from heapq import heappush, heappop

class AStar:

    cancel = False
    running = False
    
    callbackObj = None
    
    # Opened nodes
    oheap = []

    def __init__(self):
        self.goal = None


    """
    euclidean distance
    """
    def heuristic(self, a, b):
        return sqrt((b[0] - a[0]) ** 2 + (b[1] - a[1]) ** 2)
    
    def astar(self, array, start, goal):

        print("started astar...")
        
        
        self.running = True

        self.goal = goal

        neighbors = [(0, 1), (0, -1), (1, 0), (-1, 0), (1, 1), (1, -1), (-1, 1), (-1, -1)]
    
        close_set = set()
        came_from = {}
        gscore = {start:0}
        fscore = {start:self.heuristic(start, self.goal)}
       
        heappush(self.oheap, (fscore[start], start))
        
        while self.oheap:
            current = self.oheap.pop(0)[1]
    
            if self.cancel:
                print("canceled route calculation...")
                self.cancel = False
                self.running = False
                
                self.oheap = []
                
                if self.callbackObj == None:
                    print("null object")
                else:
                    self.callbackObj.navigationCanceledCallback()
                    
                return None
    
            # arrived at destination
            if current == self.goal:
                print("reached goal...")
                data = []
                while current in came_from:
                    data.insert(0, current)
                    current = came_from[current]
                    
                self.running = False
                self.oheap = []
                
                return data
    
            close_set.add(current)
               
               
            # expand node
            for i, j in neighbors:
                neighbor = current[0] + i, current[1] + j
                
                if neighbor in close_set:
                    continue
                
                # check, if neighbor is a wall
                if 0 <= neighbor[0] < array.shape[0]:
                    if 0 <= neighbor[1] < array.shape[1]:                
                        if array[neighbor[0]][neighbor[1]] == 1:
                            continue
                    else:
                        # array bound y walls
                        continue
                else:
                    # array bound x walls
                    continue
                   
                   
                tentative_g_score = gscore[current] + self.heuristic(current, neighbor)

                idx = self.getIndexOfNode(neighbor)
                    
                # ignore node, if contained and bigger g-score
                if idx >= 0 and tentative_g_score >= gscore.get(neighbor, 0):
                    continue
                   
                came_from[neighbor] = current
                gscore[neighbor] = tentative_g_score
                fscore[neighbor] = tentative_g_score + self.heuristic(neighbor, self.goal)                   

                if  idx < 0:
                    self.oheap.append((fscore[neighbor], neighbor))
                else:
                    self.oheap[idx] = (fscore[neighbor], neighbor)
                                                                    

                try:
                    self.oheap.sort(key=lambda tup: tup[0])
                except Exception as e:
                    print(str(e))
                    break
                         
                         
        self.running = False                                                                   

        self.oheap = []

        print("no route found...")

        return False
        
        
    def getIndexOfNode(self, node):
        idx = -1
        cnt = 0
        
        for s, n in self.oheap:
            if n[0] == node[0] and n[1] == node[1]: 
                idx = cnt
                break
                
            cnt = cnt + 1
            
        return idx
        
        
    def cancelCalculation(self, obj):
                
        if self.running:
            
            print("AStar cancelCalculation")
        
            self.callbackObj = obj
            self.cancel = True
            
        return self.running