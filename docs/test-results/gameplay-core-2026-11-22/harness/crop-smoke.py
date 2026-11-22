import subprocess,json,time,sys
from pathlib import Path
root=Path(__file__).parent
def rpc(m,p=None):
 r=json.loads(subprocess.check_output([sys.executable,str(root/'bridge.py'),m,json.dumps(p or {})],text=True,encoding='utf-8'));print(m,json.dumps(r));return r
def cmd(s):
 rpc('chat.send',{'message':'/'+s});time.sleep(.35)
cmd('execute if block 0 80 0 cropgenetics:crop_sticks[cross=false,planted=false] run say CHECK_SINGLE_EMPTY_OK')
cmd('tp @s 0.5 80 0.8');time.sleep(1);rpc('player.getInventory')
cmd('tp @s 0.5 80 3.5')
cmd('item replace entity @s hotbar.1 with minecraft:wheat_seeds 2')
rpc('inventory.selectHotbar',{'slot':1});rpc('control.lookAt',{'x':.5,'y':80.3,'z':.5});rpc('interact.useItem');time.sleep(.7)
cmd('data get block 0 80 0');rpc('player.getInventory');rpc('vision.screenshot')
cmd('item replace entity @s hotbar.2 with minecraft:bone_meal 16');rpc('inventory.selectHotbar',{'slot':2})
for i in range(8):
 rpc('interact.useItem');time.sleep(.25)
cmd('data get block 0 80 0');rpc('player.getInventory');rpc('vision.screenshot')
rpc('inventory.selectHotbar',{'slot':8});rpc('interact.useItem');time.sleep(.7)
cmd('data get block 0 80 0');cmd('tp @s 0.5 80 0.8');time.sleep(1);rpc('player.getInventory')
cmd('tp @s 0.5 80 3.5');rpc('control.lookAt',{'x':.5,'y':80.3,'z':.5});rpc('interact.useItem');time.sleep(.5)
cmd('data get block 0 80 0');rpc('vision.screenshot');rpc('chat.getRecent',{'limit':22})
