# Tinkers' Construct

![MIT](https://img.shields.io/badge/license-MIT-blue.svg?longCache=true&style=flat)

Unofficial NeoForge 1.21.1 port of [Tinkers' Construct](https://github.com/SlimeKnights/TinkersConstruct),
originally by [Slime Knights](https://github.com/SlimeKnights).

Tinkers' Construct is a mod about putting tools together in a wide variety of
ways, then modifying them until they turn into something else. The tools never
disappear and can be named and changed to your heart's desire. Once you make
them, they're yours forever. Many different materials can be used to make your
tools. Tools can be forged in the part builder, or by melting down metals in the
multiblock smeltery.

Tinkers' Construct requires [Mantle](https://github.com/Maf1sher/Mantle-NeoForge);
place both jars in the `mods` folder.

## Building

Requires JDK 21. Tinkers' Construct builds against Mantle, so clone the
[Mantle repository](https://github.com/Maf1sher/Mantle-NeoForge) next to this
one and build it first.

Linux / macOS:

```bash
cd Mantle-NeoForge && ./gradlew build
cd ../TinkersConstruct-NeoForge && ./gradlew build
```

Windows (Command Prompt or PowerShell):

```bat
cd Mantle-NeoForge && gradlew.bat build
cd ..\TinkersConstruct-NeoForge && gradlew.bat build
```

The jar is written to `build/libs/TinkersConstruct-1.21.1-*.jar`.

## Optional add-ons

- [Tinkers' Integrations and Tweaks](https://github.com/Maf1sher/TCIntegrations-NeoForge)
  — NeoForge 1.21.1 port of TCIntegrations, adding modpack integrations for other mods.
- [Construct's Casting](https://github.com/Maf1sher/ConstructsCasting-NeoForge)
  — NeoForge 1.21.1 port of the Iron's Spells 'n Spellbooks bridge, adding modifiable
  spellbooks, staffs, and magic tool materials.
- [Tinkers' Jewelry](https://github.com/Maf1sher/TinkersJewelry-NeoForge)
  — NeoForge 1.21.1 port of the Curios jewelry add-on, building trinkets from Tinkers' parts.
- [Tinkers' Levelling Addon](https://github.com/Maf1sher/tinkers-levelling-addon-NeoForge)
  — NeoForge 1.21.1 port of the tool levelling add-on, which adds the Improvable modifier.

## Credits

- Original mod and author: [Slime Knights](https://github.com/SlimeKnights/TinkersConstruct)
- NeoForge 1.21.1 port: [Maf1sher/TinkersConstruct-NeoForge](https://github.com/Maf1sher/TinkersConstruct-NeoForge)
- License: MIT (see [LICENSE](LICENSE))
