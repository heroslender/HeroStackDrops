<img src="https://avatars1.githubusercontent.com/u/16785313?s=96&v=4" alt="Heroslender" title="Heroslender" align="right" height="96" width="96"/>

# HeroStackDrops

[![GitHub stars](https://img.shields.io/github/stars/heroslender/HeroStackDrops.svg)](https://github.com/heroslender/HeroStackDrops/stargazers)
[![bStats Servers](https://img.shields.io/bstats/servers/5041.svg?color=1bcc1b)](https://bstats.org/plugin/bukkit/HeroStackDrops/5041)
[![bStats Servers](https://img.shields.io/bstats/players/5041.svg?color=1bcc1b)](https://bstats.org/plugin/bukkit/HeroStackDrops/5041)
[![GitHub All Releases](https://img.shields.io/github/downloads/heroslender/HeroStackDrops/total.svg?logoColor=fff)](https://github.com/heroslender/HeroStackDrops/releases/latest)
[![GitHub issues](https://img.shields.io/github/issues-raw/heroslender/HeroStackDrops.svg?label=issues)](https://github.com/heroslender/HeroStackDrops/issues)
[![GitHub last commit](https://img.shields.io/github/last-commit/heroslender/HeroStackDrops.svg)](https://github.com/heroslender/HeroStackDrops/commit)
[![Open Source Love](https://badges.frapsoft.com/os/v1/open-source.svg?v=103)](https://github.com/ellerbrock/open-source-badges/)

🔥 Um plugin que permite remover o limite dos packs de itens dropados do seu servidor de Minecraft, sendo possivel adicionar, também, um holograma para informar a quantidade de itens no pack.

![Preview](https://github.com/heroslender/HeroStackDrops/raw/master/assets/preview.png)

## Config

```yaml
restringir-itens:
  method: 'WHITELIST'
  itens:
    - 'INK_SACK'
    - 'COAL'
    - 'COBBLESTONE'
    - 'REDSTONE'
holograma:
  ativado: true
  texto: '&7{quantidade}x &e{nome}'
stack-on-spawn: false
raio-de-stack: 5
```

### Placeholders
-   `{quantidade}` - Quantidade de itens no pack
-   `{nome}` - Nome do item

## API

### Maven

```xml
<repository>
    <id>heroslender-repo</id>
    <url>https://nexus.heroslender.com/repository/maven-public/</url>
</repository>

<dependency>
  <groupId>com.heroslender</groupId>
  <artifactId>StackDrops</artifactId>
  <version>1.11.0</version>
</dependency>
```

### Events
-   `ItemUpdateEvent` - Chamado quando é atualizada a quantidade de um pack
-   `PlayerPickupItemEvent` - Chamado quando um player apanha um pack de itens