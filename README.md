# SopTelephones

Provider-based telephones for RP/economy gameplay.

Current MVP:
- providers
- number ranges
- player phone accounts
- player-side contacts
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
- `/soptelephones phone primary <player> <number>`

Player commands:
- `/phone`
- `/phone use <number>`
- `/phone contact list`
- `/phone contact add <name> <number>`
- `/phone contact remove <name>`
- `/sms <number> <message>`
