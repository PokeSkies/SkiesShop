import os

import re
import json

# Hello, this is a PebblesShop to SkiesShop converter script!
# This script will convert files to .json files for use with the SkiesGUIs mod.
# This script is not perfect and may not convert everything correctly.

# Place your PebblesShop .json files in the 'input' directory.
# Run this script to convert the files to SkiesGUIs files.
# The converted .json files will be placed in the 'output' directory.

input_dir = 'input'
output_dir = 'output'
economy = "IMPACTOR"
currency = "impactor:dollars"

def parse_gui_type(size):
    rows = int(size) // 9
    if rows < 1 or rows > 6 or int(size) % 9 != 0:
        print(f"Unsupported GUI Size Found: {size}")
        return None
    return f"GENERIC_9x{rows}"

def convert_color(match_obj):
    if match_obj.group() is not None:
        match_result = match_obj.group().replace('&', '').replace('§', '')
        match match_result:
            case '0': return '<black>'
            case '1': return '<dark_blue>'
            case '2': return '<dark_green>'
            case '3': return '<dark_aqua>'
            case '4': return '<dark_red>'
            case '5': return '<dark_purple>'
            case '6': return '<gold>'
            case '7': return '<gray>'
            case '8': return '<dark_gray>'
            case '9': return '<blue>'
            case 'a': return '<green>'
            case 'b': return '<aqua>'
            case 'c': return '<red>'
            case 'd': return '<light_purple>'
            case 'e': return '<yellow>'
            case 'f': return '<white>'
            case 'k': return '<obf>'
            case 'l': return '<b>'
            case 'm': return '<st>'
            case 'n': return '<u>'
            case 'o': return '<i>'
            case 'r': return '<reset>'
            case _:
                if match_result.startswith('#'):
                    return f"<{match_result}>"
                else:
                    print("Unsupported Color Found: " + match_result)

def parse_color(input):
    return (re.sub(r'[&§][0-9a-fklmnor]|[&§]#([0-9a-fA-F]{6})', convert_color, input)
            .replace("%player_name%", "%player%"))


def format_money(value):
    return int(value) if isinstance(value, float) and value.is_integer() else value


def fix_placeholders(text):
    return parse_color(text.replace("{player_name}", "%player%").replace("{player}", "%player%"))


def slugify(value, fallback):
    value = re.sub(r'[^a-zA-Z0-9_]+', '_', value.strip().lower()).strip('_')
    return value or fallback


def parse_nbt(nbt):
    if not nbt or nbt == "{}":
        return None
    if isinstance(nbt, dict):
        return nbt
    try:
        return json.loads(nbt)
    except json.JSONDecodeError:
        return nbt


def parse_shop_catalog_entries(items, first_index=0, page=1):
    entries_map = {}

    for slot, item in enumerate(items):
        item_index = first_index + slot
        item_id = slugify(item.get('name', ''), f"item_{item_index}")
        if item_id in entries_map:
            item_id = f"{item_id}_{item_index}"

        quantity = (item.get('quantityList') or [1])[0]
        entry_type = "COMMAND" if item.get('buyType') == 'command' else "ITEM"
        entry_map = {
            "type": entry_type,
            "slot": slot,
            "page": [page]
        }
        buy_price = format_money(item.get('buyPrice', 0) * quantity)
        if buy_price > 0:
            entry_map['buy'] = {
                "price": buy_price,
                "economy": economy,
                "currency": currency
            }
        if entry_type == "COMMAND":
            entry_map['commands'] = [
                fix_placeholders(command)
                for command in item.get('commands', [])
            ]
        else:
            entry_map['item'] = item['material']
            entry_map['amount'] = quantity
            sell_price = format_money(item.get('sellPrice', 0) * quantity)
            if sell_price > 0:
                entry_map['sell'] = {
                    "price": sell_price,
                    "economy": economy,
                    "currency": currency
                }

        if 'name' in item:
            entry_map['name'] = parse_color(item['name'])

        lore = [parse_color(line) for line in item.get('description', [])]
        if lore:
            entry_map['lore'] = lore

        if entry_type == "ITEM":
            nbt = parse_nbt(item.get('nbt'))
            if nbt is not None:
                entry_map['nbt'] = nbt

        entries_map[item_id] = entry_map

    return entries_map


def build_hardcoded_items(size):
    bottom_row_start = size - 9
    return {
        "background": {
            "item": "minecraft:black_stained_glass_pane",
            "slots": list(range(bottom_row_start, size))
        },
        "back": {
            "item": "minecraft:barrier",
            "slot": bottom_row_start + 1,
            "name": "<red>Back to Categories",
            "lore": [
                "<gray>Click to go back to the categories"
            ],
            "actions": {
                "open_shop": {
                    "type": "OPEN_SHOP",
                    "id": "example_categories"
                }
            }
        },
        "balance": {
            "item": "minecraft:emerald",
            "slots": [bottom_row_start + 4],
            "name": "<green>You have %impactor:balance dollars% coins!"
        },
        "next_page": {
            "item": "minecraft:arrow",
            "slots": [bottom_row_start + 8],
            "name": "<green>Next Page",
            "lore": [
                "<gray>Go to the next page"
            ],
            "actions": {
                "next_page": {
                    "type": "NEXT_PAGE"
                }
            }
        },
        "previous_page": {
            "item": "minecraft:arrow",
            "slots": [bottom_row_start],
            "name": "<green>Previous Page",
            "lore": [
                "<gray>Go to the previous page"
            ],
            "actions": {
                "previous_page": {
                    "type": "PREVIOUS_PAGE"
                }
            }
        }
    }


def convert_shop_entries(data, filename):
    base_filename = os.path.splitext(filename)[0]
    title = parse_color(data.get('shopName', filename))
    catalog_items = data.get('items', [])
    size = int(data.get('size', 54))
    items_per_page = max(size - 9, 1)
    pages = [
        catalog_items[start:start + items_per_page]
        for start in range(0, len(catalog_items), items_per_page)
    ] or [[]]
    converted_entries = {}

    for page_index, page_items in enumerate(pages):
        converted_entries.update(
            parse_shop_catalog_entries(
                page_items,
                page_index * items_per_page,
                page_index + 1
            )
        )

    return {
        base_filename + ".json": {
            "title": title,
            "type": parse_gui_type(size),
            "alias_commands": [base_filename],
            "entries": converted_entries,
            "items": build_hardcoded_items(size)
        }
    }


def convert_file(input_path, output_path):
    with open(input_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
        nodes = convert_shop_entries(data, os.path.basename(input_path))

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    for filename, node in nodes.items():
        node_output_path = os.path.join(os.path.dirname(output_path), filename)
        with open(node_output_path, 'w', encoding='utf-8') as o:
            json.dump(node, o, indent=4, ensure_ascii=False)


print("Iterating through the shop files in the 'input' directory and converting them...")
os.makedirs(input_dir, exist_ok=True)
for root, dirs, files in os.walk(input_dir):
    for filename in files:
        if filename.lower().endswith('.json'):
            input_path = os.path.join(root, filename)
            output_root = root.replace(input_dir, output_dir)
            output_filename = os.path.splitext(filename)[0] + ".json"
            output_path = os.path.join(output_root, output_filename)
            convert_file(input_path, output_path)
