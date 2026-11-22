import subprocess,json,time,sys
from pathlib import Path
root=Path(__file__).parent

def rpc(method,params=None):
 out=subprocess.check_output([sys.executable,str(root/'bridge.py'),method,json.dumps(params or {})],text=True,encoding='utf-8')
 r=json.loads(out);print(method,json.dumps(r,ensure_ascii=True));return r

def cmd(s):
 r=rpc('chat.send',{'message':'/'+s});time.sleep(.3);return r
for s in ['gamerule randomTickSpeed 0','time set day','weather clear','fill -2 79 -2 6 79 5 minecraft:stone','fill -2 80 -2 6 85 5 minecraft:air','setblock 0 79 0 minecraft:farmland[moisture=7]','setblock 1 79 0 minecraft:water','tp @s 0.5 80 3.5','gamemode survival','item replace entity @s hotbar.0 with cropgenetics:crop_sticks 4']:
 cmd(s)
rpc('inventory.selectHotbar',{'slot':0})
rpc('interact.placeBlock',{'x':0,'y':79,'z':0,'face':'up'});time.sleep(.5)
rpc('world.getBlock',{'x':0,'y':80,'z':0});rpc('player.getInventory')
rpc('control.lookAt',{'x':.5,'y':80.3,'z':.5});rpc('interact.useItem');time.sleep(.5)
rpc('world.getBlock',{'x':0,'y':80,'z':0});rpc('player.getInventory')
rpc('inventory.selectHotbar',{'slot':8});rpc('interact.useItem');time.sleep(.5)
rpc('world.getBlock',{'x':0,'y':80,'z':0});rpc('player.getInventory');rpc('vision.screenshot');rpc('chat.getRecent',{'limit':12})
