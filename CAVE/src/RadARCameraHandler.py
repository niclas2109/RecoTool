'''
Created on 11.09.2017

@author: uk012025
'''

import viz

class RadARCameraHandler( viz.CameraHandler ): 

    def _camMouseDown( self, e ): 
        if e.button == viz.MOUSEBUTTON_LEFT: 
            #move view down 
            e.view.move( 0, -1, 0 ) 
        elif e.button == viz.MOUSEBUTTON_RIGHT: 
            #move view up 
            e.view.move( 0, 1, 0 ) 