from live import *
backup('round-b-ui01');rpc('container.close');cmd('clear @s');position(56,60)
cmd('setblock 56 100 60 cropgenetics:seed_analyzer');cmd('setblock 57 100 60 cropgenetics:seed_chest')
cmd('item replace entity @s hotbar.0 with minecraft:wheat_seeds 64');cmd('item replace entity @s hotbar.1 with cropgenetics:seed_journal');cmd('item replace entity @s hotbar.2 with minecraft:stick');cmd('cropgenetics testseed minecraft:wheat_plant 7 4 9')
rpc('inventory.selectHotbar',{'slot':8});before=inventory();proof_before=cmd('data get entity @s Inventory');click(56,60);time.sleep(.5)
menu=rpc('container.state');assert menu['open'] and menu['size']==36,menu

def cc(slot,mode='pickup'):
 rpc('container.click',{'slot':slot,'mode':mode});time.sleep(.3);return rpc('container.state')
def total():
 v=inventory();m=rpc('container.state');out={}
 for s in v['hotbar']+v['main']+m['slots']:out[s['id']]=out.get(s['id'],0)+s['count']
 return out
expected={'minecraft:wheat_seeds':64,'cropgenetics:seed_journal':1,'minecraft:stick':1,'cropgenetics:genetic_seed':1}
states=[];states.append(cc(63,'quick_move'));proof64={'analyzer':cmd('data get block 56 100 60'),'chest':cmd('data get block 57 100 60')}
assert total()==expected,('64-seed conservation mismatch',total())
# The seven filler slots must reject a carried ordinary item. Put cursor back.
cc(65)
for slot in range(2,9):states.append(cc(slot))
cc(65)
assert total()==expected,('filler conservation mismatch',total())
assert not any(s['slot'] in range(2,9) for s in rpc('container.state')['slots'])
# Extract the analyzed ordinary seed, then insert genetic seed and journal.
states.append(cc(0,'quick_move'));states.append(cc(66,'quick_move'));states.append(cc(64,'quick_move'))
proof_insert={'analyzer':cmd('data get block 56 100 60'),'chest':cmd('data get block 57 100 60')};photo=rpc('vision.screenshot')['saved']
assert total()==expected,('insert conservation mismatch',total())
active=rpc('container.state');assert all(s['count']<=1 for s in active['slots'] if s['slot'] in [0,1])
# Take all active analyzer/chest slots back with real quick-moves.
for s in list(rpc('container.state')['slots']):
 if s['slot'] in [0,1] or s['slot']>=9:cc(s['slot'],'quick_move')
after=inventory();proof_after={'analyzer':cmd('data get block 56 100 60'),'chest':cmd('data get block 57 100 60'),'inventory':cmd('data get entity @s Inventory')};final=total()
assert final==expected,('final conservation mismatch',final)
assert rpc('container.state')['slots']==[]
save_result({'id':'UI-01','startUtc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'status':'pass','actions':['64-seed real shift-click','attempt carried item in all seven filler slots','extract analyzed ordinary seed','insert/extract genetic seed and journal','empty all storage with shift-click'],'observedItemCounts':{'before':before,'after':after,'totals':final},'serverProof':{'before':proof_before,'after64':proof64,'inserted':proof_insert,'final':proof_after},'states':states,'screenshots':[photo],'rollback':'new world backup-round-b-ui01'})
rpc('container.close');cmd('save-all flush')

