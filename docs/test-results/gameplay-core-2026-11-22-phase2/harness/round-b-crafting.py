from live import *
backup('round-b-crafting')

def menu_click(slot,mode='pickup'):
 rpc('container.click',{'slot':slot,'mode':mode});time.sleep(.3);return rpc('container.state')
def counts():
 v=inventory();out={}
 for s in v['hotbar']+v['main']:
  out[s['id']]=out.get(s['id'],0)+s['count']
 return out
for label,paper,named,seeds,quick in [('one-paper',1,False,1,False),('sixteen-paper',16,False,1,False),('named-paper',16,True,1,False),('shift-two-seeds',16,False,2,True)]:
 cmd('clear @s');rpc('container.close');cmd('item replace entity @s hotbar.1 with minecraft:paper'+('[minecraft:custom_name=\'{"text":"Core named paper"}\']' if named else '')+' '+str(paper))
 for _ in range(seeds):cmd('cropgenetics testseed minecraft:wheat_plant 7 4 9')
 before=inventory();server_before=cmd('data get entity @s Inventory');grid_states=[]
 menu_click(37);grid_states.append(menu_click(2))
 for n in range(seeds):
  source=36 if n==0 else 38
  menu_click(source);grid_states.append(menu_click(1))
  state_now=rpc('container.state');result=[s for s in state_now['slots'] if s['slot']==0]
  if len(result)!=1 or result[0]['id']!='minecraft:wheat_seeds' or result[0]['count']!=1:raise RuntimeError('Crafting result missing/incorrect; stop')
  grid_states.append(menu_click(0,'quick_move' if quick else 'pickup'))
  if not quick:menu_click(36)
 menu_click(2);menu_click(37)
 final=counts();server_after=cmd('data get entity @s Inventory');after=inventory();screenshot=rpc('vision.screenshot')['saved']
 expected={'minecraft:paper':paper,'minecraft:wheat_seeds':seeds}
 valid=final==expected and (not named or any('Core named paper' in s for s in server_after))
 save_result({'id':'ER-08','trial':label,'startUtc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'actions':['real inventory-menu pickup clicks','real crafting result pickup/quick_move','paper returned to original inventory slot'],'observedItemCounts':{'before':before,'after':after,'totals':final},'serverProof':{'before':server_before,'after':server_after},'gridStates':grid_states,'screenshots':[screenshot],'status':'pass' if valid else 'fail','failureOrBlocker':None if valid else 'Paper/count/name conservation mismatch; stop for inventory triage','rollback':'backup-round-b-crafting world copy'})
 if not valid:
  cmd('save-all flush');raise RuntimeError('Potential crafting loss/duplication; stopped')
cmd('save-all flush')
