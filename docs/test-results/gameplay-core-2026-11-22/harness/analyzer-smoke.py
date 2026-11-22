import subprocess,json,time,sys
from pathlib import Path
root=Path(__file__).parent
def rpc(m,p=None):
 r=json.loads(subprocess.check_output([sys.executable,str(root/'bridge.py'),m,json.dumps(p or {})],text=True,encoding='utf-8'));print(m,json.dumps(r));return r
def cmd(s):
 rpc('chat.send',{'message':'/'+s});time.sleep(.35)

rpc('control.lookAt',{'x':.5,'y':80.1,'z':.5});rpc('interact.useItem');time.sleep(.5)
rpc('player.getInventory');cmd('data get block 0 80 0')
rpc('inventory.selectHotbar',{'slot':2})
for i in range(4):
 rpc('interact.useItem');time.sleep(.25)
cmd('data get block 0 80 0');rpc('inventory.selectHotbar',{'slot':8})
rpc('interact.breakBlock',{'x':0,'y':80,'z':0,'mode':'survival'});time.sleep(1)
cmd('tp @s 0.5 80 0.8');time.sleep(1);rpc('player.getInventory');cmd('data get entity @s Inventory');rpc('vision.screenshot')
cmd('setblock 2 80 2 cropgenetics:seed_analyzer');cmd('tp @s 2.5 80 4.5')
rpc('inventory.selectHotbar',{'slot':1});rpc('control.lookAt',{'x':2.5,'y':80.15,'z':2.5});rpc('interact.useItem');time.sleep(.7)
cmd('data get block 2 80 2');rpc('player.getInventory');rpc('vision.screenshot')
rpc('inventory.selectHotbar',{'slot':8});rpc('interact.useItem');time.sleep(.5);rpc('screen.get');rpc('vision.screenshot');rpc('screen.close')
cmd('cropgenetics testseed minecraft:wheat_plant 7 4 9');cmd('data get entity @s Inventory');rpc('player.getInventory');rpc('chat.getRecent',{'limit':14})
