from pathlib import Path
import json,urllib.request,sys,datetime
root=Path(__file__).parent
cfg=json.loads((root/'client/config/mcpfabric.config.json').read_text())
method=sys.argv[1];params=json.loads(sys.argv[2]) if len(sys.argv)>2 else {}
req=urllib.request.Request(f'http://127.0.0.1:{cfg["port"]}/rpc',data=json.dumps({'method':method,'params':params}).encode(),headers={'Content-Type':'application/json','Authorization':'Bearer '+cfg['token']})
try:
 result=json.load(urllib.request.urlopen(req,timeout=20))
except Exception as e:
 result={'error':str(e)}
if method=='vision.screenshot' and result.get('ok'):
 import base64
 image=result['result'].pop('base64',None)
 if image:
  name='screenshot-'+datetime.datetime.now().strftime('%H%M%S')+'.png'
  (root/'evidence'/name).write_bytes(base64.b64decode(image))
  result['result']['saved']=name
with (root/'evidence/bridge-transcript.jsonl').open('a',encoding='utf-8') as f: f.write(json.dumps({'utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'method':method,'params':params,'response':result})+'\n')
print(json.dumps(result,ensure_ascii=False))
