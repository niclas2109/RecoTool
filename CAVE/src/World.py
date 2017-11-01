import viz
import vizcam
import vizfx
import vizconnect
import vizcave

from vizconnect.util import view_collision

class World():

	def __init__(self):
		vizconnect.go('vizconnect_config_cave_art.py')
		
		_startPos = [28, 2.6, 160]
		
		### Collision
		### Desktop
		#viz.collision(viz.ON)
		viz.MainView.setPosition(_startPos)
		
		### Cave
		ac = view_collision.AvatarCollision()
		ac.setCollideList([vizconnect.AVATAR_HEAD])
		ac.setTransport(vizconnect.getTransport().getNode3d())
		
		#viz.MainView.gravity(9.81)
		
		self.view = vizconnect.getGroup('cave_manual_configuration').getNode3d()
		#self.setCameraPosition([120, 3.3, 87])
		self.setCameraPosition(_startPos, viz.ABS_GLOBAL)
		#self.view.setEuler([-90,0,7],viz.ABS_LOCAL)


		
		
		### Scene Lighting
		self.setupLighting()

		# Disable mouse movement
		# viz.ON = disabled
		# comment / viz.OFF = enabled
		#viz.mouse.setOverride(viz.ON)
		#cam = vizcam.FlyNavigate()
		#cam.sensitivity(2.0, 1.0)

	def setCameraPosition(self, pos, euler=[0,0,0]):
		self.view.setPosition(pos,viz.ABS_GLOBAL)
		self.view.setEuler(euler,viz.ABS_LOCAL)
		print("changed camera position to "+str(pos)+"\n"+str(euler))

	def setupLighting(self):
		viz.MainView.getHeadLight().remove()

		# Create Sky
		viz.clearcolor(viz.SKYBLUE)
		sky = viz.add('sky_day.osgb')
		sky.setScale(2, 2, 2)
		sky.setPosition(0, 0, 0)

		lightColor = [0.8,0.7,0.6]

		# Create directional lights
		sun = vizfx.addDirectionalLight(euler=([-120.00000, 24.50000, 0.00000]), color = lightColor)

		# Adjust ambient color
		vizfx.setAmbientColor([0.95,0.95,0.95])