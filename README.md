# SkiesShop
<img height="50" src="https://camo.githubusercontent.com/a94064bebbf15dfed1fddf70437ea2ac3521ce55ac85650e35137db9de12979d/68747470733a2f2f692e696d6775722e636f6d2f6331444839564c2e706e67" alt="Requires Fabric Kotlin"/>

A Fabric server-sided mod featuring a powerful shop system allowing you to create infinite shops with items and commands available for purchase and for sale with relative ease!

More information on configuration can be found on the [Wiki](https://docs.skiesdev.com/skiesshop/getting_started/introduction/)!

## Features
- Create practically infinite Shops *(idk, haven't tested that)*
- Customize everything about items in the inventory
  - Multi slot definitions *(one item definition, multiple slots)*
  - Name/Lore customization with [MiniMessage formatting](https://docs.advntr.dev/minimessage/format.html)
  - Full access to NBT *(custom model data, Pokemon, etc.)*
  - Pagination for shops
- Shop entry requirements
- Decoration Items with click Actions
- Presets system for quick Shop Entry modifications
- Confirmation Menu support with completely customizable layout and items
- All customizable items support custom names, lore, enchantments, and more (anything that is a component/nbt can be applied)
- GUI open and close Actions
- Transaction Logging with built-in GUI for viewing a player's transactions
- 14 action types *(for now)*
- 7 requirement types *(for now)*
- Economy Integrations (Impactor, Pebbles Economy)
- Placeholder Integrations (Impactor, PlaceholderAPI)

## Installation
1. Download the latest version of the mod from [Modrinth](https://modrinth.com/mod/skiesshop).
2. Download all required dependencies:
   - [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin)
3. Download any optional dependencies:
   - [Impactor](https://modrinth.com/mod/impactor) **_(Economy, Placeholders)_**
   - [MiniPlaceholders](https://modrinth.com/plugin/miniplaceholders) **_(Placeholders)_**
   - [PlaceholderAPI](https://modrinth.com/mod/placeholder-api) **_(Placeholders)_**
   - Pebbles Economy **_(Economy)_**
   - [BEconomy](https://modrinth.com/mod/beconomy) **_(Economy)_**
   - [CobbleDollars](https://modrinth.com/mod/cobbledollars) **_(Economy)_**
   - [Plan](https://modrinth.com/plugin/plan) **_(Additional Requirements)_**
4. Install the mod and dependencies into your server's `mods` folder.
5. Configure your Shops in the `./config/skiesshop/shops/` folder.

## Commands/Permissions
| Command                        | Description                                        | Permission                                            |
|--------------------------------|----------------------------------------------------|-------------------------------------------------------|
| /skiesshop, /shops, /shop      | Default command                                    | skiesshop.command.base                                |
| /shops reload                  | Reloads any configuration changes                  | skiesshop.command.reload                              |
| /shops debug                   | Toggle the debug mode for more insight into errors | skiesshop.command.debug                               |
| /shops open <shop_id> [player] | Open the specified Shop, optionally for a player   | skiesshop.command.open, skiesshop.command.open.others |
| /shops transactions <player>   | Open the transactions GUI of a specific player     | skiesshop.command.transactions                        |

## Planned Features
- Item Currency Support
- More Shop Entry Types
  - Pokemon? (can be achieved already via commands, but maybe something more complex)
  - Suggestions?
- Stock Limits
- Rotating Stock
- Dynamic Pricing


## Support
A community support Discord has been opened up for all Skies Development related projects! Feel free to join and ask questions or leave suggestions :)

<a class="discord-widget" href="https://discord.gg/cgBww275Fg" title="Join us on Discord"><img src="https://discordapp.com/api/guilds/1158447623989116980/embed.png?style=banner2"></a>
