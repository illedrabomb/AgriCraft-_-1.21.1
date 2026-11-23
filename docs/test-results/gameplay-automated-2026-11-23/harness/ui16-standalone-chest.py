from live import *

cmd('gamemode survival')
cmd('clear @s')
position(10, 30)
cmd('setblock 10 100 30 cropgenetics:seed_chest')
cmd('item replace entity @s hotbar.0 with minecraft:wheat_seeds 64')
cmd('item replace entity @s hotbar.1 with minecraft:stick')
cmd('item replace entity @s hotbar.2 with cropgenetics:seed_journal')
cmd('cropgenetics testseed minecraft:wheat_plant 7 4 9')
rpc('inventory.selectHotbar', {'slot': 8})
before = inventory()
click(10, 30)
time.sleep(.6)
menu_before = rpc('container.state')
assert menu_before['open'] and menu_before['size'] == 27, menu_before

def quick(slot):
    rpc('container.click', {'slot': slot, 'mode': 'quick_move'})
    time.sleep(.4)
    return rpc('container.state')

after_normal = quick(54)
after_stick = quick(55)
after_journal = quick(56)
after_genetic = quick(57)
server_stored = cmd('data get block 10 100 30')
after = inventory()
photo = rpc('vision.screenshot')['saved']

stored_text = '\n'.join(server_stored)
valid = (
    'minecraft:wheat_seeds' in stored_text
    and 'cropgenetics:genetic_seed' in stored_text
    and 'minecraft:stick' not in stored_text
    and 'cropgenetics:seed_journal' not in stored_text
    and any(s['id'] == 'minecraft:stick' for s in after['hotbar'] + after['main'])
    and any(s['id'] == 'cropgenetics:seed_journal' for s in after['hotbar'] + after['main'])
)
save_result({
    'id': 'UI-16',
    'status': 'pass' if valid else 'fail',
    'actions': ['open standalone 3x9 chest', 'shift-click normal seed', 'shift-click stick', 'shift-click journal', 'shift-click genetic seed'],
    'observedItemCounts': {'before': before, 'after': after},
    'serverProof': server_stored,
    'states': {'before': menu_before, 'normal': after_normal, 'stick': after_stick, 'journal': after_journal, 'genetic': after_genetic},
    'screenshots': [photo],
    'failureOrBlocker': None if valid else 'Unexpected standalone Seed Chest filter result',
    'rollback': 'isolated world'
})
rpc('container.close')
cmd('save-all flush')
if not valid:
    raise RuntimeError('UI-16 failed')
