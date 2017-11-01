"""
Das Skript dient zur Steuerung der Inhalte im Frankfurt/Main Hauptbahnhof
"""

import viz
import vizact
import vizfx

import datetime
import random

class GleisUhren:

	def __init__(self):
		self.seconds = 0
		self.minutes = 0
		self.hours = 0

	def loadModel(self, hbf):

		"""
		Alle Uhrzeiger im Modell hier referenzieren
		Reihenfolge muss mit updateTime übereinstimmen
		"""
		uhr1 = [
			hbf.getChild('SekundenzeigerVorn'),
			hbf.getChild('SekundenzeigerHinten'),
			hbf.getChild('MinutenzeigerVorn'),
			hbf.getChild('MinutenzeigerHinten'),
			hbf.getChild('StundenzeigerVorn'),
			hbf.getChild('StundenzeigerHinten')]

		uhr2 = [
			hbf.getChild('SekundenzeigerVorn.001'),
			hbf.getChild('SekundenzeigerHinten.001'),
			hbf.getChild('MinutenzeigerVorn.001'),
			hbf.getChild('MinutenzeigerHinten.001'),
			hbf.getChild('StundenzeigerVorn.001'),
			hbf.getChild('StundenzeigerHinten.001')]

		uhr3 = [
			hbf.getChild('SekundenzeigerVorn.002'),
			hbf.getChild('SekundenzeigerHinten.002'),
			hbf.getChild('MinutenzeigerVorn.002'),
			hbf.getChild('MinutenzeigerHinten.002'),
			hbf.getChild('StundenzeigerVorn.002'),
			hbf.getChild('StundenzeigerHinten.002')]

		uhr4 = [
			hbf.getChild('SekundenzeigerVorn.003'),
			hbf.getChild('SekundenzeigerHinten.003'),
			hbf.getChild('MinutenzeigerVorn.003'),
			hbf.getChild('MinutenzeigerHinten.003'),
			hbf.getChild('StundenzeigerVorn.003'),
			hbf.getChild('StundenzeigerHinten.003')]

		uhr5 = [
			hbf.getChild('SekundenzeigerVorn.004'),
			hbf.getChild('SekundenzeigerHinten.004'),
			hbf.getChild('MinutenzeigerVorn.004'),
			hbf.getChild('MinutenzeigerHinten.004'),
			hbf.getChild('StundenzeigerVorn.004'),
			hbf.getChild('StundenzeigerHinten.004')]

		uhr6 = [
			hbf.getChild('SekundenzeigerVorn.005'),
			hbf.getChild('SekundenzeigerHinten.005'),
			hbf.getChild('MinutenzeigerVorn.005'),
			hbf.getChild('MinutenzeigerHinten.005'),
			hbf.getChild('StundenzeigerVorn.005'),
			hbf.getChild('StundenzeigerHinten.005')]

		uhr7 = [
			hbf.getChild('SekundenzeigerVorn.006'),
			hbf.getChild('SekundenzeigerHinten.006'),
			hbf.getChild('MinutenzeigerVorn.006'),
			hbf.getChild('MinutenzeigerHinten.006'),
			hbf.getChild('StundenzeigerVorn.006'),
			hbf.getChild('StundenzeigerHinten.006')]

		uhr8 = [
			hbf.getChild('MinutenzeigerVorn.007'),
			hbf.getChild('MinutenzeigerHinten.007'),
			hbf.getChild('SekundenzeigerVorn.007'),
			hbf.getChild('SekundenzeigerHinten.007'),
			hbf.getChild('StundenzeigerVorn.007'),
			hbf.getChild('StundenzeigerHinten.007')]

		uhr9 = [
			hbf.getChild('SekundenzeigerVorn.008'),
			hbf.getChild('SekundenzeigerHinten.008'),
			hbf.getChild('MinutenzeigerVorn.008'),
			hbf.getChild('MinutenzeigerHinten.008'),
			hbf.getChild('StundenzeigerVorn.008'),
			hbf.getChild('StundenzeigerHinten.008')]

		uhr10 = [
			hbf.getChild('SekundenzeigerVorn.009'),
			hbf.getChild('SekundenzeigerHinten.009'),
			hbf.getChild('MinutenzeigerVorn.009'),
			hbf.getChild('MinutenzeigerHinten.009'),
			hbf.getChild('StundenzeigerVorn.009'),
			hbf.getChild('StundenzeigerHinten.009')]

		uhr11 = [
			hbf.getChild('SekundenzeigerVorn.010'),
			hbf.getChild('SekundenzeigerHinten.010'),
			hbf.getChild('MinutenzeigerVorn.010'),
			hbf.getChild('MinutenzeigerHinten.010'),
			hbf.getChild('StundenzeigerVorn.010'),
			hbf.getChild('StundenzeigerHinten.010')]

		self.uhren = [uhr1,uhr2,uhr3,uhr4,uhr5,uhr6,uhr7,uhr8,uhr9,uhr10,uhr11]

		viz.callback(viz.TIMER_EVENT, self.onTimer)
		viz.starttimer(1, 1, viz.PERPETUAL)

	def setCurrentTime(self):
		now = datetime.datetime.now()
		self.setTime(now.hour, now.minute, now.second)

	# Setzen der Uhrzeit in der Simulation
	def setTime(self, hour = 0, minute = 0, second = -1):
		
		if second < 0:
			now = datetime.datetime.now()
			second = now.second
		
		self.seconds = second
		self.minutes = minute
		self.hours = hour

	def getTime(self):
		return [self.hours, self.minutes, self.seconds]

	# Vizard Callback
	def onTimer(self, timer):
		if timer == 1:
			self.onSecond()
			self.updateTime()

	# Abhängig vom Timer in regelmäßigen Abständen aktualisiert
	def updateTime(self):

		secRota = [self.seconds*6, 0, 0]
		minRota = [self.minutes*6+self.seconds*6/60, 0, 0]
		hourRota = [self.hours*30+self.minutes*6/12, 0, 0]
		# Rotation auf Rückseite erfolgt entgegengesetzt
		secRotaNeg = [-self.seconds*6, 0, 0]
		minRotaNeg = [-self.minutes*6-self.seconds*6/60, 0, 0]
		hourRotaNeg = [-self.hours*30-self.minutes*6/12, 0, 0]

		for uhr in self.uhren:
			uhr[0].setEuler(secRotaNeg)
			uhr[1].setEuler(secRota)
			uhr[2].setEuler(minRotaNeg)
			uhr[3].setEuler(minRota)
			uhr[4].setEuler(hourRotaNeg)
			uhr[5].setEuler(hourRota)

	def onSecond(self):
		self.seconds += 1
		if self.seconds > 59:
			self.seconds = 0
			self.onMinute()

	def onMinute(self):
		self.minutes += 1
		if self.minutes > 59:
			self.minutes = 0
			self.onHour()

	def onHour(self):
		self.hours += 1
		if self.hours > 23:
			self.hours = 0




def findNextGoal(start, distance, mapManager):

	distance = distance * mapManager.mapper.squaresPerMeter

	next = [(int) (start[0] *  mapManager.mapper.squaresPerMeter - mapManager.offsetX),start[1],(int) (start[2] *  mapManager.mapper.squaresPerMeter - mapManager.offsetY)]


	size = mapManager.mapper.matrix.shape
	
	# determine direction
	check = False
	
	cntr = 0
	dir = random.randint(0, 4) % 4
	
	while cntr < 4 and check == False:
		if dir == 0 & next[0] + 1 < size[0] & mapManager.mapper.matrix[next[0] + 1][next[2]] == 0:
			check = True
		elif dir == 1 & next[0] - 1 >= 0 & mapManager.mapper.matrix[next[0] - 1][next[2]] == 0:
			check = True
		elif dir == 2 & next[2] + 1 < size[1] & mapManager.mapper.matrix[next[0]][next[2] + 1] == 0:
			check = True
		elif dir == 3 & next[2] - 1 >= 0 & mapManager.mapper.matrix[next[0]][next[2] - 1] == 0:
			check = True
		else:
			print("next: " + str(dir))
			dir = (dir + 1)  % 4
			cntr = cntr + 1
	
	print("****\ndir: " + str(dir) + "\n------")	
	
	if check == False:
		print ("can not move")
		return start
		
	# walk distance
	while distance > 0:
		if dir == 0 & next[0] + 1 < size[0]:# & mapManager.mapper.matrix[next[0] + 1][next[2]] == 0:
			next[0] = next[0] + 1
		elif dir == 1 & next[0] - 1 >= 0:# & mapManager.mapper.matrix[next[0] - 1][next[2]] == 0:
			next[0] = next[0] - 1
		elif dir == 2 & next[2] + 1 < size[1]:# & mapManager.mapper.matrix[next[0]][next[2] + 1] == 0:
			next[2] = next[2] + 1
		elif dir == 3 & next[2] - 1 >= 0:# & mapManager.mapper.matrix[next[0]][next[2] - 1] == 0:
			next[2] = next[2] - 1
		else:
			print("------no change----")
			print(str(next[0] + 1) + " < " + str(size[0]))
			print(str(next[0] - 1) + " >= 0")
			print(str(next[2] + 1) + " < " + str(size[1]))
			print(str(next[2] - 1) + " >= 0")
			print("dir: " + str(dir))
			print("start: " + str(start))
			print("end: " + str(next))
			print("dist: " + str(distance))
			print("--------------")
				
			break
			
		distance = distance-1

	next[0] = next[0] + distance
	next = [(int) (next[0] /  mapManager.mapper.squaresPerMeter) + mapManager.offsetX, start[1], (int) (next[2] /  mapManager.mapper.squaresPerMeter + mapManager.offsetY)]

	return next
		
### Scene dynamic
def addPeople(mapManager):


	### Add people with random route
	for i in range(0,50):
		
		avatar = 'vcc_male.cfg'
		if((int)(random.random() * 10)) % 2 == 0:
			avatar = 'vcc_female.cfg'
		
		person = vizfx.addAvatar(avatar)
		
		pos = [random.randint(20, 200), 2.5, random.randint(80, 120)]
		
		while mapManager.mapper.matrix[(int) (pos[0] *  mapManager.mapper.squaresPerMeter - mapManager.offsetX)][(int) (pos[2] *  mapManager.mapper.squaresPerMeter - mapManager.offsetY)]== 1:
			pos = [random.randint(20, 200),2.5,random.randint(80, 120)]

		person.setPosition(pos)
		
		actions = []
		actions.append(pos)
		
		for j in range(0,2):
			
			nextPos = findNextGoal(pos, random.randint(10, 30), mapManager)
			
			if pos != nextPos:
				actions.append(vizact.walkTo(nextPos))
				
			pos = nextPos
			
		if len(actions) > 1:
			person_seq = vizact.sequence(actions, viz.FOREVER)
			person.runAction(person_seq)
		else:
			print("removed avatar")
			person.remove()
			
	
	return
	
	### Information
	info1 = vizfx.addAvatar('vcc_male.cfg')
	info1.setPosition([3.62353, 2.55017, -64.36298])
	info1.setEuler([180, 0, 0])
	info1.state(1)

	info2 = vizfx.addAvatar('vcc_female.cfg')
	info2.setPosition([0.07517, 2.55017, -64.61992])
	info2.setEuler([180, 0, 0])
	info2.state(1)

	info3 = vizfx.addAvatar('vcc_male2.cfg')
	info3.setPosition([-2.45477, 2.55017, -64.54278])
	info3.setEuler([180, 0, 0])
	info3.state(1)
	### Information

	### Shops
	female1 = vizfx.addAvatar('vcc_female.cfg')
	female1.setPosition([-107.63577, 2.60017, -63.46337])
	female1.setEuler(-90, 0, 0)
	female1.state(1)

	male9 = vizfx.addAvatar('vcc_male2.cfg')
	male9.setPosition([-100.42088, 2.60017, -64.16325])
	male9.setEuler(90, 0, 0)
	male9.state(1)

	female2 = vizfx.addAvatar('vcc_female.cfg')
	female2.setPosition([-80.69553, 2.60017, -46.70128])
	female2.setEuler(-90, 0, 0)
	female2.state(1)

	male17 = vizfx.addAvatar('vcc_male.cfg')
	male17.setPosition([-73.44943, 2.60017, -45.32741])
	male17.setEuler(90, 0 ,0)
	male17.state(1)

	female3 = vizfx.addAvatar('vcc_female.cfg')
	female3.setPosition([-53.71975, 2.60017, -45.82802])
	female3.setEuler(-90, 0, 0)
	female3.state(1)

	male18 = vizfx.addAvatar('vcc_male2.cfg')
	male18.setPosition([-46.34661, 2.60017, -46.12560])
	male18.setEuler(90, 0, 0)
	male18.state(1)

	female4 = vizfx.addAvatar('vcc_female.cfg')
	female4.setPosition([-15.02875, 2.60017, -45.91469])
	female4.setEuler(-90, 0, 0)
	female4.state(1)

	male19 = vizfx.addAvatar('vcc_male.cfg')
	male19.setPosition([-8.12945, 2.60017, -45.98362])
	male19.setEuler(90, 0, 0)
	male19.state(1)

	female5 = vizfx.addAvatar('vcc_female.cfg')
	female5.setPosition([15.97678, 2.60017, -46.24681])
	female5.setEuler(-90, 0, 0)
	female5.state(1)

	male20 = vizfx.addAvatar('vcc_male.cfg')
	male20.setPosition([22.90181, 2.60017, -46.19674])
	male20.setEuler(90, 0, 0)
	male20.state(1)

	male21 = vizfx.addAvatar('vcc_male2.cfg')
	male21.setPosition([52.47180, 2.60017, -46.11211])
	male21.setEuler(-90, 0, 0)
	male21.state(1)

	female6 = vizfx.addAvatar('vcc_female.cfg')
	female6.setPosition([59.88219, 2.60017, -46.48273])
	female6.setEuler(90, 0, 0)
	female6.state(1)

	female7 = vizfx.addAvatar('vcc_female.cfg')
	female7.setPosition([94.47379, 2.60017, -45.87137])
	female7.setEuler(-90, 0, 0)
	female7.state(1)

	female8 = vizfx.addAvatar('vcc_female.cfg')
	female8.setPosition([101.72762, 2.60017, -46.35223])
	female8.setEuler(90, 0, 0)
	female8.state(1)

	male21 = vizfx.addAvatar('vcc_male.cfg')
	male21.setPosition([-58.85641, 2.60000, -11.10135])
	male21.setEuler([-180, 0, 0])
	male21.state(1)

	male21 = vizfx.addAvatar('vcc_male2.cfg')
	male21.setPosition([-21.21277, 2.60000, -10.96846])
	male21.setEuler(-180, 0, 0)
	male21.state(1)

	female8 = vizfx.addAvatar('vcc_female.cfg')
	female8.setPosition([-2.55035, 2.60000, -10.95346])
	female8.setEuler(-180, 0, 0)
	female8.state(1)
	### Shops

		
### Scene dynamic