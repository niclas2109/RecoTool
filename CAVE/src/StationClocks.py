"""
Das Skript dient zur Steuerung der Inhalte im Frankfurt/Main Hauptbahnhof
"""

import viz
import vizact
import vizfx

import datetime
import random

class StationClocks:

	def __init__(self, trainManager):
		self.seconds = 0
		self.minutes = 0
		self.hours = 0
		
		self.trainManager = trainManager

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
			
		# send/order train for station dynamic
		idx = random.randint(0, len(self.trainManager.trainObj)-1)
		self.trainManager.sendTrainByIdx(idx)
			
		idx = random.randint(0, len(self.trainManager.trainObj)-1)
		self.trainManager.orderTrainByIdx(idx)



	def onHour(self):
		self.hours += 1
		if self.hours > 23:
			self.hours = 0