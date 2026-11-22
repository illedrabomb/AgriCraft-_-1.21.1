from live import *
s=rpc('screen.get');assert s['inWorld'],s
rpc('inventory.selectHotbar',{'slot':6});rpc('control.look',{'pitch':-70});time.sleep(.3);rpc('interact.useItem');time.sleep(.3)
for _ in range(3):rpc('ui.click',{'widget':'>'})
u=rpc('ui.widgets');assert any(w['text']=='Current key: F8 (click)' for w in u['widgets']),u
(ROOT/'evidence/ui14-after-restart.json').write_text(json.dumps({'widgets':u,'keyLine':[l for l in (ROOT/'client/options.txt').read_text().splitlines() if 'cropgenetics' in l],'screenshot':rpc('vision.screenshot')}),encoding='utf-8')
print('Journal F8 and persisted options confirmed after real client restart.')
