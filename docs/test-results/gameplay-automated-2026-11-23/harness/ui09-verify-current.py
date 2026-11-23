from live import *

cmd('clear @s')
cmd('gamemode survival')
cmd('setblock 60 100 48 cropgenetics:seed_analyzer')
position(60, 48)
cmd('item replace entity @s hotbar.1 with cropgenetics:seed_journal')
rpc('inventory.selectHotbar', {'slot': 1})
rpc('control.lookAt', {'x': 60.5, 'y': 100.15, 'z': 48.5})

rpc('control.setInput', {'sneak': True})
time.sleep(.8)
before = inventory()
state_before = cmd('data get block 60 100 48')
action = rpc('interact.useItem')
time.sleep(1.5)
after = inventory()
state_after = cmd('data get block 60 100 48')
player_after = rpc('player.getState')
rpc('control.stop')

cmd('item replace entity @s hotbar.1 with cropgenetics:seed_journal')
rpc('inventory.selectHotbar', {'slot': 1})
rpc('control.lookAt', {'x': 60.5, 'y': 100.15, 'z': 48.5})
without_sneak_before = inventory()
without_sneak_action = rpc('interact.useItem')
time.sleep(.8)
without_sneak_screen = rpc('screen.get')
without_sneak_after = inventory()
state_final = cmd('data get block 60 100 48')
photo = rpc('vision.screenshot')['saved']
rpc('screen.close')

inserted = any('Slot: 1b' in line and 'seed_journal' in line for line in state_after)
hand_removed = not any(s['id'] == 'cropgenetics:seed_journal' for s in after['hotbar'] + after['main'])
book_open = without_sneak_screen.get('screen') != 'none'
hand_preserved = any(s['id'] == 'cropgenetics:seed_journal' for s in without_sneak_after['hotbar'] + without_sneak_after['main'])
valid = inserted and hand_removed and book_open and hand_preserved
save_result({
    'id': 'UI-09',
    'trial': 'final-sneak-and-normal-dispatch',
    'status': 'pass' if valid else 'fail',
    'actions': ['sneak useItem on empty analyzer', 'wait for inventory sync', 'normal useItem while targeting occupied analyzer'],
    'serverProof': {'before': state_before, 'afterSneak': state_after, 'final': state_final},
    'observedItemCounts': {'before': before, 'afterSneak': after, 'beforeNormal': without_sneak_before, 'afterNormal': without_sneak_after},
    'dispatch': {'sneakAction': action, 'playerAfter': player_after, 'normalAction': without_sneak_action, 'normalScreen': without_sneak_screen},
    'screenshots': [photo],
    'failureOrBlocker': None if valid else f'inserted={inserted}, handRemoved={hand_removed}, bookOpen={book_open}, handPreserved={hand_preserved}',
    'rollback': 'isolated world'
})
if not valid:
    raise RuntimeError('UI-09 verification failed')
