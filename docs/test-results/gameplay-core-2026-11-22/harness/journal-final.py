import subprocess,json,time,sys
from pathlib import Path
root=Path(__file__).parent
def rpc(m,p=None):
 r=json.loads(subprocess.check_output([sys.executable,str(root/'bridge.py'),m,json.dumps(p or {})],text=True,encoding='utf-8'));print(m,json.dumps(r));return r
def cmd(s):
 rpc('chat.send',{'message':'/'+s});time.sleep(.35)

cmd('tp @s 5.5 80 1.5');time.sleep(1);rpc('player.getInventory');cmd('execute as @e[type=minecraft:item,distance=..6] run data get entity @s Item')
cmd('tp @s 2.5 80 4.5');cmd('item replace entity @s hotbar.6 with cropgenetics:seed_journal');rpc('inventory.selectHotbar',{'slot':6});rpc('control.setInput',{'sneak':True});time.sleep(.3)
rpc('interact.placeBlock',{'x':2,'y':80,'z':2,'face':'up'});time.sleep(.5);rpc('control.stop');cmd('data get block 2 80 2');rpc('player.getInventory')
cmd('item replace entity @s hotbar.6 with cropgenetics:seed_journal');rpc('control.setInput',{'sneak':True});time.sleep(.3)
rpc('interact.placeBlock',{'x':2,'y':80,'z':2,'face':'up'});time.sleep(.5);rpc('control.stop');rpc('player.getInventory');cmd('data get block 2 80 2')
cmd('cropgenetics testseed minecraft:wheat_plant 3 5 7');rpc('player.getInventory');rpc('inventory.selectHotbar',{'slot':1});rpc('interact.placeBlock',{'x':2,'y':80,'z':2,'face':'up'});time.sleep(.5);rpc('player.getInventory');cmd('data get block 2 80 2');rpc('screen.get');rpc('vision.screenshot');rpc('screen.close')
rpc('chat.getRecent',{'limit':10});cmd('save-all flush');cmd('stop')
