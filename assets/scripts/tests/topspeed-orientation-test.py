# This script tests the speed limit and the orientation lock API calls.
# Created by Toni Sagrista

from py4j.java_gateway import JavaGateway, GatewayParameters

gateway = JavaGateway(gateway_parameters=GatewayParameters(auto_convert=True))
gs = gateway.entry_point

gs.disableInput()
gs.cameraStop()
gs.maximizeInterfaceWindow()
gs.expandGuiComponent("CameraComponent")

# Camera speed limit

gs.setCameraSpeedLimit(0)
gs.sleep(1)
gs.setCameraSpeedLimit(1)
gs.sleep(1)
gs.setCameraSpeedLimit(3)
gs.sleep(1)
gs.setCameraSpeedLimit(4)
gs.sleep(1)
gs.setCameraSpeedLimit(5)
gs.sleep(1)
gs.setCameraSpeedLimit(6)
gs.sleep(1)
gs.setCameraSpeedLimit(7)
gs.sleep(1)
gs.setCameraSpeedLimit(8)
gs.sleep(1)
gs.setCameraSpeedLimit(9)
gs.sleep(1)
gs.setCameraSpeedLimit(10)
gs.sleep(1)
gs.setCameraSpeedLimit(11)
gs.sleep(1)
gs.setCameraSpeedLimit(12)
gs.sleep(1)
gs.setCameraSpeedLimit(13)
gs.sleep(2)

# Orientation lock
gs.setCameraOrientationLock(True)
gs.sleep(1)
gs.setCameraOrientationLock(False)
gs.sleep(1)

gs.enableInput()

gateway.close()