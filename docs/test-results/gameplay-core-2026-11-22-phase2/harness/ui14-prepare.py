from live import *
backup('round-b-ui14');plant(56,44,'wheat',1);rpc('inventory.selectHotbar',{'slot':8});rpc('control.lookAt',{'x':56.5,'y':100.3,'z':44.5});time.sleep(.5)
cmd('item replace entity @s hotbar.6 with cropgenetics:seed_journal');print(rpc('player.getState'));print(rpc('screen.get'));time.sleep(12);print(rpc('vision.screenshot'))
