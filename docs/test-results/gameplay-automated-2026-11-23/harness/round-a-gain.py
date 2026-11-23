from live import *
backup('round-a-gain');cmd('cropgenetics harvestmultiplier reset');cmd('cropgenetics harvestmultiplier')
for gain,x in [(1,0),(10,10)]:
 plant(x,44,'melon',gain)
 for cycle in [1,2]:
  before=ready(x,44);out=harvest(x,44,f'er13_{gain}_{cycle}');rows=out['drops'];low,high=(3,7) if gain==1 else (12,16)
  valid=len(rows)==1 and rows[0]['id']=='minecraft:melon_slice' and low<=rows[0]['count']<=high
  save_result({'id':'ER-13','trial':f'gain-{gain}-cycle-{cycle}','startUtc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'gain':gain,'serverProof':before,'observedItemCounts':out,'drops':rows,'screenshots':[rpc('vision.screenshot')['saved']],'status':'pass' if valid else 'fail','rollback':'disposable copy'})
  if not valid:raise RuntimeError('Unexpected melon harvest; stop')
for gain,x in [(1,0),(2,10),(5,20),(8,30),(10,40)]:
 plant(x,60,'pumpkin',gain,short=True)
 for cycle in [1,2]:
  before=ready(x,60);out=harvest(x,60,f'er12_{gain}_{cycle}');rows=out['drops']
  valid=len(rows)==1 and rows[0]['id']=='minecraft:pumpkin' and 1<=rows[0]['count']<=4 and (gain!=1 or rows[0]['count']==1)
  save_result({'id':'ER-12','trial':f'gain-{gain}-cycle-{cycle}','gain':gain,'startUtc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'serverProof':before,'observedItemCounts':out,'drops':rows,'screenshots':[rpc('vision.screenshot')['saved']],'status':'partial' if valid else 'fail','failureOrBlocker':'Bounds observed only; two samples per gain are not a distribution acceptance.','rollback':'disposable copy'})
  if not valid:raise RuntimeError('Unexpected pumpkin harvest; stop')
cmd('cropgenetics harvestmultiplier reset');cmd('cropgenetics harvestmultiplier');cmd('gamerule randomTickSpeed 0');cmd('save-all flush')

