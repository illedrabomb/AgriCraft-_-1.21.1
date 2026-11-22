import subprocess,json,time,sys
from pathlib import Path
root=Path(__file__).parent
def rpc(m,p=None):
 r=json.loads(subprocess.check_output([sys.executable,str(root/'bridge.py'),m,json.dumps(p or {})],text=True,encoding='utf-8'));print(m,json.dumps(r));return r
def cmd(s):
 rpc('chat.send',{'message':'/'+s});time.sleep(.35)

cmd('item replace entity @s hotbar.0 with cropgenetics:crop_sticks 4')
for x,z,seed in [(5,0,'pumpkin_seeds'),(5,2,'melon_seeds')]:
 cmd(f'setblock {x} 79 {z} minecraft:farmland[moisture=7]')
 cmd('setblock 4 79 1 minecraft:water');cmd(f'tp @s {x+.5} 80 {z+2.5}')
 rpc('inventory.selectHotbar',{'slot':0});rpc('interact.placeBlock',{'x':x,'y':79,'z':z,'face':'up'});time.sleep(.3)
 cmd(f'item replace entity @s hotbar.6 with minecraft:{seed} 2');rpc('inventory.selectHotbar',{'slot':6});rpc('interact.placeBlock',{'x':x,'y':80,'z':z,'face':'up'});time.sleep(.3)
 rpc('inventory.selectHotbar',{'slot':2})
 for i in range(7):
  rpc('interact.placeBlock',{'x':x,'y':80,'z':z,'face':'up'});time.sleep(.2)
 cmd(f'data get block {x} 80 {z}')
cmd('gamerule randomTickSpeed 512');time.sleep(15);cmd('gamerule randomTickSpeed 0')
for x,z in [(5,0),(5,2)]:
 cmd(f'tp @s {x+.5} 80 {z+2.5}');cmd(f'data get block {x} 80 {z}');rpc('inventory.selectHotbar',{'slot':8});rpc('interact.placeBlock',{'x':x,'y':80,'z':z,'face':'up'});time.sleep(.4);cmd(f'data get block {x} 80 {z}')
rpc('vision.screenshot');rpc('chat.getRecent',{'limit':15})
cmd('gamerule randomTickSpeed 512');time.sleep(15);cmd('gamerule randomTickSpeed 0')
for x,z in [(5,0),(5,2)]:
 cmd(f'tp @s {x+.5} 80 {z+2.5}');cmd(f'data get block {x} 80 {z}');rpc('inventory.selectHotbar',{'slot':8});rpc('interact.placeBlock',{'x':x,'y':80,'z':z,'face':'up'});time.sleep(.4);cmd(f'data get block {x} 80 {z}')
rpc('vision.screenshot');rpc('chat.getRecent',{'limit':15})
