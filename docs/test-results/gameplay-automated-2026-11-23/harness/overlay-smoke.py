from live import *

deadline = time.time() + 45
while True:
    try:
        rpc('player.getState')
        break
    except Exception:
        if time.time() >= deadline:
            raise
        time.sleep(2)

cmd('gamemode survival')
plant(0, 30, 'wheat', 5)
position(0, 30)
rpc('control.lookAt', {'x': 0.5, 'y': 100.5, 'z': 30.5})
time.sleep(2)
photo = rpc('vision.screenshot')['saved']
save_result({
    'id': 'UI-13',
    'trial': 'identify-start-and-crop-interaction',
    'status': 'partial',
    'actions': ['load Identify separately', 'join core server', 'create and mature genetic wheat', 'look at Crop Sticks'],
    'serverProof': state(0, 30),
    'screenshots': [photo],
    'failureOrBlocker': 'Screenshot/log review required for overlay content and privacy; startup and crop interaction completed.',
    'rollback': 'isolated world'
})
