from live import *
u=rpc('ui.widgets');print(json.dumps({'title':u['title'],'widgets':[w for w in u['widgets'] if 'F10' in w['text'] or w['text']=='Done']}))
