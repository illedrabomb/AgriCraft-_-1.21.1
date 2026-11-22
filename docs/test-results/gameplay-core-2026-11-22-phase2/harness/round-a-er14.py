from live import *
start=datetime.datetime.now(datetime.timezone.utc).isoformat()
cmd('gamerule randomTickSpeed 0');cmd('gamemode creative');cmd('tp @s 32.5 104 36.5');time.sleep(1)
cmd('fill -4 99 12 68 99 68 minecraft:stone');cmd('fill -4 100 12 68 103 68 minecraft:air');cmd('tp @s 0.5 100 23.5');cmd('gamemode survival');cmd('time set day');cmd('weather clear')
backup('round-a-er14');baseline=cmd('cropgenetics harvestmultiplier')
try:
 for factor in [0,1,2,64]:
  cmd('cropgenetics harvestmultiplier '+str(factor));active=cmd('cropgenetics harvestmultiplier')
  for species in ['wheat','pumpkin','melon']:
   idx=[0,1,2,64].index(factor)*3+['wheat','pumpkin','melon'].index(species);x=(idx%6)*10;z=20+(idx//6)*12
   t0=datetime.datetime.now(datetime.timezone.utc).isoformat()
   before=plant(x,z,species,10)
   if species!='wheat':
    try:before=ready(x,z)
    except RuntimeError as e:
     save_result({'id':'ER-14','trial':f'{species}-factor-{factor}','startUtc':t0,'status':'blocked','failureOrBlocker':str(e)});continue
   outcome=harvest(x,z,'er14_'+str(idx));rows=outcome['drops'];expected={'wheat':'minecraft:wheat','pumpkin':'minecraft:pumpkin','melon':'minecraft:melon_slice'}[species]
   valid=(not rows) if factor==0 else (len(rows)==1 and rows[0]['id']==expected and 1<=rows[0]['count']<=64)
   if factor==64:valid=valid and rows[0]['count']==64
   photo=rpc('vision.screenshot')
   row={'id':'ER-14','trial':f'{species}-factor-{factor}','startUtc':t0,'actions':['fresh testseed 1/10/1','mature with actual bone meal','fruit readiness polled if gourd','operator override and query','one normal right-click harvest'],'gain':10,'factor':factor,'multiplierFeedback':active,'serverBefore':before,'observedItemCounts':outcome,'drops':rows,'screenshots':[photo['saved']],'status':'pass' if valid else 'fail','rollback':'new disposable world; factor reset in finally'}
   if factor==0:
    tag='zero_break_'+str(idx);cmd('tag @e[type=minecraft:item] add '+tag);rpc('interact.breakBlock',{'x':x,'y':100,'z':z,'mode':'survival'});time.sleep(1)
    broken=drops(x,z,tag);row['zeroFactorBreakDrops']=broken
    totals={}
    for d in broken:totals[d['id']]=totals.get(d['id'],0)+d['count']
    if totals!={'cropgenetics:crop_sticks':1,'cropgenetics:genetic_seed':1}:row['status']='fail';row['failureOrBlocker']='Unexpected zero-factor seed/stick preservation; stop for triage.'
   save_result(row)
   if row['status']=='fail':raise RuntimeError('Potential harvest/inventory defect; stopping at '+row['trial'])
finally:
 cmd('gamerule randomTickSpeed 0');cmd('cropgenetics harvestmultiplier reset');reset=cmd('cropgenetics harvestmultiplier')
 (ROOT/'evidence/er14-reset.json').write_text(json.dumps({'before':baseline,'after':reset}),encoding='utf-8')
 cmd('save-all flush')
