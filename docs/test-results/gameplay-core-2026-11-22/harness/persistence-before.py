import subprocess,json,time,sys
from pathlib import Path
root=Path(__file__).parent
def rpc(m,p=None):
 r=json.loads(subprocess.check_output([sys.executable,str(root/'bridge.py'),m,json.dumps(p or {})],text=True,encoding='utf-8'));print(m,json.dumps(r));return r
def cmd(s):
 rpc('chat.send',{'message':'/'+s});time.sleep(.35)

# Recover the already analyzed seed, then analyze the actual block-break drop.
rpc('inventory.selectHotbar',{'slot':8});rpc('control.setInput',{'sneak':True});time.sleep(.3)
rpc('interact.placeBlock',{'x':2,'y':80,'z':2,'face':'up'});time.sleep(.5);rpc('control.stop')
rpc('inventory.selectHotbar',{'slot':4});rpc('interact.placeBlock',{'x':2,'y':80,'z':2,'face':'up'});time.sleep(.6)
cmd('data get block 2 80 2');rpc('vision.screenshot')
# Repeat second-layer test and explicitly try planting while crossed.
cmd('tp @s 0.5 80 3.5');rpc('inventory.selectHotbar',{'slot':0})
rpc('interact.placeBlock',{'x':0,'y':79,'z':0,'face':'up'});time.sleep(.4)
rpc('interact.placeBlock',{'x':0,'y':80,'z':0,'face':'up'});time.sleep(.4)
cmd('execute if block 0 80 0 cropgenetics:crop_sticks[cross=true,planted=false] run say CROSS_READY')
rpc('inventory.selectHotbar',{'slot':1});rpc('player.getInventory');rpc('interact.placeBlock',{'x':0,'y':80,'z':0,'face':'up'});time.sleep(.5)
cmd('execute if block 0 80 0 cropgenetics:crop_sticks[cross=true,planted=false] run say CROSS_REJECTS_SEED_OK');rpc('player.getInventory');rpc('vision.screenshot')
rpc('inventory.selectHotbar',{'slot':8});rpc('interact.placeBlock',{'x':0,'y':80,'z':0,'face':'up'});time.sleep(.4)
rpc('inventory.selectHotbar',{'slot':1});rpc('interact.placeBlock',{'x':0,'y':80,'z':0,'face':'up'});time.sleep(.5)
cmd('data get block 0 80 0');cmd('data get entity @s Inventory')
cmd('save-all flush');cmd('reload');time.sleep(2);cmd('data get block 0 80 0');cmd('data get entity @s Inventory');rpc('chat.getRecent',{'limit':12});rpc('vision.screenshot')
cmd('save-all flush');cmd('stop')
