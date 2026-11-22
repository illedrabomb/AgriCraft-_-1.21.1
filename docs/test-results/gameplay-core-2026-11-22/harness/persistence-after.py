import subprocess,json,time,sys
from pathlib import Path
root=Path(__file__).parent
def rpc(m,p=None):
 r=json.loads(subprocess.check_output([sys.executable,str(root/'bridge.py'),m,json.dumps(p or {})],text=True,encoding='utf-8'));print(m,json.dumps(r));return r
def cmd(s):
 rpc('chat.send',{'message':'/'+s});time.sleep(.35)

cmd('data get block 0 80 0');cmd('data get block 2 80 2');cmd('data get entity @s Inventory')
cmd('reload');time.sleep(2);cmd('data get block 0 80 0');cmd('data get block 2 80 2');cmd('data get entity @s Inventory');rpc('chat.getRecent',{'limit':8});rpc('vision.screenshot')
# Carrot Stage 0 and maturation through real bone-meal interactions.
cmd('setblock 3 79 0 minecraft:farmland[moisture=7]');cmd('tp @s 3.5 80 3.5')
rpc('inventory.selectHotbar',{'slot':0});rpc('interact.placeBlock',{'x':3,'y':79,'z':0,'face':'up'});time.sleep(.4)
cmd('item replace entity @s hotbar.6 with minecraft:carrot 2');rpc('inventory.selectHotbar',{'slot':6});rpc('interact.placeBlock',{'x':3,'y':80,'z':0,'face':'up'});time.sleep(.4)
cmd('data get block 3 80 0');rpc('control.lookAt',{'x':3.5,'y':80.3,'z':.5});time.sleep(.3);rpc('vision.screenshot')
cmd('item replace entity @s hotbar.2 with minecraft:bone_meal 32');rpc('inventory.selectHotbar',{'slot':2})
for i in range(7):
 rpc('interact.placeBlock',{'x':3,'y':80,'z':0,'face':'up'});time.sleep(.2)
cmd('data get block 3 80 0');rpc('vision.screenshot')
# Explicit double-harvest checks on the persisted wheat.
cmd('tp @s 0.5 80 3.5');rpc('control.lookAt',{'x':.5,'y':80.3,'z':.5});time.sleep(.3)
for i in range(7):
 rpc('interact.placeBlock',{'x':0,'y':80,'z':0,'face':'up'});time.sleep(.2)
cmd('data get block 0 80 0');rpc('inventory.selectHotbar',{'slot':8})
rpc('interact.placeBlock',{'x':0,'y':80,'z':0,'face':'up'});time.sleep(.5);cmd('data get block 0 80 0')
cmd('execute as @e[type=minecraft:item,distance=..6] run data get entity @s Item')
rpc('interact.placeBlock',{'x':0,'y':80,'z':0,'face':'up'});time.sleep(.4)
cmd('data get block 0 80 0');cmd('execute as @e[type=minecraft:item,distance=..6] run data get entity @s Item')
cmd('item replace entity @s hotbar.7 with minecraft:stick');rpc('inventory.selectHotbar',{'slot':7});rpc('interact.placeBlock',{'x':0,'y':80,'z':0,'face':'up'});time.sleep(.4)
cmd('data get block 0 80 0');cmd('execute as @e[type=minecraft:item,distance=..6] run data get entity @s Item');rpc('vision.screenshot');rpc('chat.getRecent',{'limit':22})
