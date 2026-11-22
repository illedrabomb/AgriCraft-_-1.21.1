from live import *
cmd('gamerule randomTickSpeed 0');cmd('cropgenetics harvestmultiplier reset');cmd('save-all flush')
out=cmd('execute as @e[type=minecraft:item,x=40.5,y=100,z=60.5,distance=..4] run data get entity @s')
(ROOT/'evidence/pumpkin-missing-drop-audit.json').write_text(json.dumps(out),encoding='utf-8')
for s in out:print(s[:2600])
