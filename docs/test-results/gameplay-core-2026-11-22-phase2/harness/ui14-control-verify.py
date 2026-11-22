from live import *
for label in ['Options...','Controls...','Key Binds...']:
 u=rpc('ui.widgets');assert any(w['text']==label and w['active'] for w in u['widgets']);rpc('ui.click',{'widget':label})
u=rpc('ui.widgets');found=[w for w in u['widgets'] if 'F8' in w['text']];assert len(found)==1,found
(ROOT/'evidence/ui14-control-verification.json').write_text(json.dumps({'key':found,'option':[l for l in (ROOT/'client/options.txt').read_text().splitlines() if 'cropgenetics' in l]}),encoding='utf-8')
rpc('vision.screenshot')
for label in ['Done','Done','Done','Back to Game']:rpc('ui.click',{'widget':label})
cmd('save-all flush')
