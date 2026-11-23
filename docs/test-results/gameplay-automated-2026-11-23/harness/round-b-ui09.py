from live import *
# To be run only after round A has completed.
backup('round-b-ui09');cmd('clear @s');cmd('setblock 56 100 48 cropgenetics:seed_analyzer');position(56,48)
cmd('cropgenetics testseed minecraft:wheat_plant 7 4 9');rpc('inventory.selectHotbar',{'slot':0});time.sleep(.3)
seed_before=inventory();seed_nbt_before=cmd('data get entity @s Inventory');click(56,48);time.sleep(.5)
seed_slot=cmd('data get block 56 100 48');seed_after=inventory()
cmd('cropgenetics testseed minecraft:wheat_plant 3 5 7');rpc('inventory.selectHotbar',{'slot':0});time.sleep(.3)
occupied_seed_before=inventory();click(56,48);time.sleep(.4);occupied_seed_after=inventory();occupied_seed_slot=cmd('data get block 56 100 48');rpc('screen.close')
cmd('item replace entity @s hotbar.1 with cropgenetics:seed_journal');rpc('inventory.selectHotbar',{'slot':1});rpc('control.lookAt',{'x':56.5,'y':100.15,'z':48.5});time.sleep(.5)
attempts=[]
for method in ['interact.useItem','interact.placeBlock']:
 rpc('control.setInput',{'sneak':True});time.sleep(1);rpc('control.lookAt',{'x':56.5,'y':100.15,'z':48.5});time.sleep(.3)
 pre=inventory();player=rpc('player.getState');proof_before=cmd('data get block 56 100 48')
 action=rpc(method,{'x':56,'y':100,'z':48,'face':'up'} if method=='interact.placeBlock' else {})
 time.sleep(.7);post=inventory();proof_after=cmd('data get block 56 100 48');screen=rpc('screen.get');photo=rpc('vision.screenshot')['saved']
 attempts.append({'method':method,'confirmedPlayerState':player,'inventoryBefore':pre,'inventoryAfter':post,'serverBefore':proof_before,'serverAfter':proof_after,'action':action,'screen':screen,'screenshot':photo})
 rpc('control.stop');rpc('screen.close')
row={'id':'UI-09','startUtc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'status':'inconclusive','actions':['selected hotbar confirmed','normal seed insert','occupied seed attempt','journal sneak with both dispatch methods'],'seedBefore':seed_before,'seedNbtBefore':seed_nbt_before,'seedAfter':seed_after,'seedSlot':seed_slot,'occupiedSeedBefore':occupied_seed_before,'occupiedSeedAfter':occupied_seed_after,'occupiedSeedSlot':occupied_seed_slot,'journalAttempts':attempts,'failureOrBlocker':'Evaluate sneak flags and server slot/hand conservation before assigning final status.','rollback':'isolated world backup'}
save_result(row)
rpc('control.stop');rpc('screen.close');cmd('save-all flush')

