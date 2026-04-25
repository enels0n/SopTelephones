# SopTelephones

Provider-based telephones for RP/economy gameplay.

Current MVP:
- providers
- number ranges
- player phone accounts
- player-side contacts
- physical phone and SIM items
- right-click phone menu
- unread state through custom model data
- message history by phone number
- relay towers
- core uplink coverage check
- SMS with Vault economy

Planned next:
- offline queue
- contacts
- multiple phones with player-side selection
- provider balances payout tools
- tariffs and inter-provider prices
- illegal towers/jammers/interception hooks

Admin commands:
- `/soptelephones reload`
- `/soptelephones provider create <id> <displayName...>`
- `/soptelephones provider price <id> <amount>`
- `/soptelephones range add <providerId> <prefix> <from> <to>`
- `/soptelephones tower add <id> <providerId> <world> <x> <y> <z> <coverageRadius> <linkRadius>`
- `/soptelephones phone assign <player> <providerId> <number>`
- `/soptelephones phone give <player> <modelId>`
- `/soptelephones sim give <player> <providerId> <number>`
- `/soptelephones phone primary <player> <number>`

Player commands:
- `/phone`
- `/phone use <number>`
- `/phone contact list`
- `/phone contact add <name> <number>`
- `/phone contact remove <name>`
- `/sms <number> <message>`

Phone item flow:
- hold the phone and right-click to open the menu
- sneak-right-click with a SIM in offhand to insert it
- sneak-right-click without a SIM in offhand to eject the installed one
- click a contact or recent number to insert `/sms <number> ` into chat
