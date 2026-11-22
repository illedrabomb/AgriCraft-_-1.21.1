from pathlib import Path
import json,urllib.request,datetime,time,base64,shutil,re,sys,zipfile
ROOT=Path(__file__).parent
CFG=json.loads((ROOT/'client/config/mcpfabric.config.json').read_text())
LAST_ID=0

def rpc(method,params=None):
 params=params or {};start=datetime.datetime.now(datetime.timezone.utc).isoformat()
 req=urllib.request.Request('http://127.0.0.1:25599/rpc',data=json.dumps({'method':method,'params':params}).encode(),headers={'Content-Type':'application/json','Authorization':'Bearer '+CFG['token']})
 r=json.load(urllib.request.urlopen(req,timeout=25))
 if method=='vision.screenshot' and r.get('ok'):
  b=r['result'].pop('base64',None)
  if b:
   name='shot-'+datetime.datetime.now().strftime('%H%M%S%f')+'.png';(ROOT/'evidence'/name).write_bytes(base64.b64decode(b));r['result']['saved']=name
 with (ROOT/'evidence/rpc.jsonl').open('a',encoding='utf-8') as f:f.write(json.dumps({'utc':start,'method':method,'params':params,'response':r})+'\n')
 if not r.get('ok'):raise RuntimeError(method+': '+json.dumps(r))
 return r['result']

def chat():
 global LAST_ID
 ms=rpc('chat.getRecent',{'limit':500}).get('messages',[])
 out=[m['data'].get('text','') for m in ms if m['id']>LAST_ID]
 if ms:LAST_ID=max(m['id'] for m in ms)
 return out

def cmd(command):
 chat();rpc('chat.send',{'message':'/'+command});time.sleep(.3);out=chat()
 with (ROOT/'evidence/commands.jsonl').open('a',encoding='utf-8') as f:f.write(json.dumps({'command':command,'feedback':out})+'\n')
 return out

def inventory():return rpc('player.getInventory')
def state(x,z):
 messages=cmd(f'data get block {x} 100 {z}')
 text='\n'.join(messages)
 def num(k):
  m=re.search(r'\b'+k+r':\s*(-?\d+)',text);return int(m[1]) if m else None
 return {'stage':num('GrowthStage'),'ready':num('FruitReady'),'gain':num('gain'),'growth':num('growth'),'strength':num('strength'),'text':text}

def click(x,z):
 r=rpc('interact.placeBlock',{'x':x,'y':100,'z':z,'face':'up'});time.sleep(.18);return r

def backup(label):
 before=inventory();cmd('gamerule randomTickSpeed 0');cmd('save-all flush');cmd('save-off')
 try:
  time.sleep(1)
  folder=ROOT/'server/isolated-core-world'
  with zipfile.ZipFile(ROOT/('backup-'+label+'-'+datetime.datetime.now().strftime('%H%M%S')+'.zip'),'w',zipfile.ZIP_DEFLATED) as z:
   for f in folder.rglob('*'):
    if f.is_file() and f.name!='session.lock':z.write(f,f.relative_to(folder.parent))
 finally:cmd('save-on')
 return before

def drops(x,z,tag):
 ms=cmd(f'execute as @e[type=minecraft:item,x={x+.5},y=100,z={z+.5},distance=..3,tag=!{tag}] run data get entity @s Item')
 rows=[]
 for s in ms:
  a=re.search(r'count:\s*(\d+)',s);b=re.search(r'\bid:\s*"([^"]+)"',s)
  if a and b:rows.append({'id':b[1],'count':int(a[1])})
 return rows

def position(x,z):
 cmd(f'tp @s {x+.5} 100 {z+3.5}');time.sleep(.25)

def plant(x,z,species,gain,short=False):
 position(x,z);cmd('clear @s');cmd(f'setblock {x} 99 {z} minecraft:farmland[moisture=7]');cmd(f'setblock {x+1} 99 {z} minecraft:water')
 cmd('item replace entity @s hotbar.0 with cropgenetics:crop_sticks');rpc('inventory.selectHotbar',{'slot':0})
 rpc('interact.placeBlock',{'x':x,'y':99,'z':z,'face':'up'});time.sleep(.2)
 cmd(f'cropgenetics testseed minecraft:{species}_plant {gain}' if short else f'cropgenetics testseed minecraft:{species}_plant 1 {gain} 1');rpc('inventory.selectHotbar',{'slot':0});click(x,z)
 cmd('item replace entity @s hotbar.1 with minecraft:bone_meal 32');rpc('inventory.selectHotbar',{'slot':1})
 for i in range(7):click(x,z)
 result=state(x,z)
 if result['stage']!=7 or result['gain']!=gain:raise RuntimeError('Plant did not mature: '+json.dumps(result))
 return result

def ready(x,z):
 s=state(x,z)
 if s['ready']==1:return s
 cmd('gamerule randomTickSpeed 512')
 try:
  for i in range(12):
   time.sleep(2);s=state(x,z)
   if s['ready']==1:return s
 finally:cmd('gamerule randomTickSpeed 0')
 raise RuntimeError('Fruit not ready within observation window')

def harvest(x,z,tag):
 position(x,z);rpc('inventory.selectHotbar',{'slot':8});cmd('tag @e[type=minecraft:item] add '+tag)
 before=inventory();click(x,z);time.sleep(.3)
 return {'inventoryBefore':before,'drops':drops(x,z,tag),'stateAfter':state(x,z),'inventoryAfter':inventory()}

def save_result(row):
 row['testedCommit']='4b2e78db09f02db3ee40a66e4bb98a8f7f4acac2';row['jarSha256']='9e272b6c9ddf97d8a96dbee64ec27b595caea704bef68c79760f0ad0e41da27a';row['worldCopy']=str(ROOT/'server/isolated-core-world');row['profile']='Core server; MCPFabric dev client';row['endUtc']=datetime.datetime.now(datetime.timezone.utc).isoformat()
 with (ROOT/'evidence/results.jsonl').open('a',encoding='utf-8') as f:f.write(json.dumps(row)+'\n')
 print(json.dumps({k:v for k,v in row.items() if k in ['id','trial','status','drops','failureOrBlocker']}),flush=True)
