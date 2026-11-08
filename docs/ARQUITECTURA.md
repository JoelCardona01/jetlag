# Cómo está hecho Jet Lag

Este documento explica el diseño del plugin para que puedas mantenerlo, ampliarlo y entender cada decisión.

---

## 1. Visión general

Jet Lag es un **plugin de servidor** (Bukkit/Paper). Los jugadores no instalan nada. La idea es simple:

1. Escuchamos **todos** los teletransportes (`PlayerTeleportEvent`).
2. Filtramos los que nos interesan (tipo, mundo, cooldown, distancia, permiso).
3. Tiramos un dado (`chance`, por defecto 15 %).
4. Si sale, elegimos un evento al azar **ponderado por su peso**.
5. El evento puede **cambiar el destino** (antes del teletransporte) y luego **ejecuta su efecto** 2 ticks después de llegar.
6. Se suma a las estadísticas y se anuncia en el chat.

¿Por qué `PlayerTeleportEvent` y no interceptar el comando `/tp`? Porque así funciona con **cualquier** plugin de teletransporte (EssentialsX, CMI, HuskHomes...). Casi todos usan la causa `COMMAND`, así que `/tpa`, `/home`, `/spawn` y `/warp` entran solos, sin escribir integraciones.

---

## 2. Estructura del proyecto

```
jetlag/
├── build.gradle.kts            # Compila contra paper-api 1.16.5 con bytecode Java 8
├── settings.gradle.kts
├── gradlew / gradle/wrapper    # Gradle Wrapper: compila sin tener Gradle instalado
├── .github/
│   ├── workflows/build.yml     # CI: compila en cada push y publica Release al crear un tag
│   └── FUNDING.yml             # Botón "Sponsor" de GitHub
├── src/main/java/dev/jetlag/
│   ├── JetLagPlugin.java       # Punto de entrada: arranca todo
│   ├── JetLagSettings.java     # config.yml convertido en objeto inmutable
│   ├── TeleportListener.java   # El corazón: decide si toca Jet Lag
│   ├── ProtectionListener.java # Anti-grief: nuestras explosiones no rompen bloques
│   ├── JetLagCommand.java      # /jetlag y su autocompletado
│   ├── events/
│   │   ├── JetLagEvent.java    # Interfaz que implementa cada evento
│   │   ├── AbstractEvent.java  # Base con helpers para leer opciones
│   │   ├── EventRegistry.java  # Lista de eventos, pesos, sorteo y ejecución
│   │   └── *Event.java         # Los 12 eventos
│   └── util/
│       ├── Compat.java         # Trucos de compatibilidad entre versiones
│       ├── Tags.java           # Marca las entidades que creamos
│       ├── Messages.java       # Sistema de idiomas
│       └── Stats.java          # Contador por jugador (stats.yml)
└── src/main/resources/
    ├── plugin.yml              # Nombre, comando, permisos, api-version
    ├── config.yml              # Configuración comentada
    └── lang/en.yml, es.yml     # Textos
```

---

## 3. El flujo de un teletransporte

```
Jugador escribe /home
        │
        ▼
PlayerTeleportEvent (causa COMMAND)
        │
TeleportListener.onTeleport  (prioridad HIGH, ignora cancelados)
        │  ¿plugin activo? ¿causa en trigger-causes? ¿sin jetlag.bypass?
        │  ¿mundo permitido? ¿distancia mínima? ¿fuera de cooldown?
        │  ¿dado < chance?
        ▼
EventRegistry.pickRandom()   → solo eventos con peso > 0 y canRun() == true
        │
event.redirect()             → opcional: cambia el destino (sky_drop, wrong_destination)
        │
   (el jugador llega)
        │  +2 ticks
        ▼
EventRegistry.fire()         → event.run() + stats + mensaje en el chat
```

Detalles importantes:

- **Prioridad `HIGH`:** se ejecuta después de que la mayoría de plugins decidan el destino, pero todavía podemos modificarlo. `MONITOR` no sirve, porque en esa prioridad está prohibido modificar el evento.
- **`ignoreCancelled = true`:** si otro plugin cancela el teletransporte (por ejemplo, un cooldown de EssentialsX), no hacemos nada.
- **Retraso de 2 ticks:** si spawneamos el creeper en el mismo tick, el cliente aún no ha cargado el destino y quedaría raro. 2 ticks (0,1 s) bastan.
- **`try/catch` en `fire()`:** si un evento falla (por ejemplo, por una API rara en una versión concreta), se registra un aviso y el teletransporte sigue funcionando. Un evento roto nunca rompe el servidor.

---

## 4. Las clases, una a una

### `JetLagPlugin`
Arranca en `onEnable`: guarda la config por defecto, crea `Messages`, `Stats` y `EventRegistry`, registra los dos listeners y el comando, y programa un guardado de estadísticas cada 5 minutos. `reloadEverything()` relee todo (lo usa `/jetlag reload`).

### `JetLagSettings`
Convierte `config.yml` en campos `final`. En cada recarga se crea un objeto nuevo, así el resto del código nunca lee rutas de texto sueltas ni se encuentra la config a medio cambiar. Las causas de teletransporte se leen por **nombre**: si pones una que no existe en esa versión de Minecraft, se avisa y se ignora en lugar de petar.

### `TeleportListener`
Toda la lógica de "¿toca Jet Lag?" descrita arriba. Guarda el último disparo por jugador para el cooldown y lo limpia al desconectarse.

### `ProtectionListener`
Para que el plugin sea **gracioso y no destructivo**:

| Evento de Bukkit | Qué hace |
|---|---|
| `EntityExplodeEvent` | Si la entidad es nuestra y `block-damage: false`, vacía la lista de bloques → la explosión hace daño, pero no rompe nada |
| `EntityChangeBlockEvent` | Los yunques que caen desaparecen al tocar el suelo en vez de convertirse en bloques |
| `EntityDamageByEntityEvent` | Los fuegos artificiales de broma no hacen daño |

¿Cómo sabemos que una entidad es "nuestra"? → `Tags`.

### `Tags`
Añade el *scoreboard tag* `jetlag` a todo lo que spawneamos. Los scoreboard tags existen desde la 1.10, se guardan con la entidad (sobreviven a reinicios) y no necesitan metadatos ni PersistentDataContainer.

### `EventRegistry`
- Registra los 12 eventos en un `LinkedHashMap` (el orden se mantiene en el autocompletado).
- `load()` lee `events.<id>.enabled` y `events.<id>.weight`.
- `pickRandom()` hace un **sorteo ponderado**: suma los pesos, saca un número entre 0 y el total y va restando. Con pesos 10, 10 y 2, el último sale 2 de cada 22 veces.
- `trigger()` lo usa `/jetlag test`: aplica la redirección con un teletransporte real (marcando al jugador como `suppressed` para que el listener no vuelva a tirar el dado) y luego lanza el evento.

### `JetLagEvent` y `AbstractEvent`
Cada evento implementa:

| Método | Para qué |
|---|---|
| `id()` | Nombre en config, idiomas y `/jetlag test` |
| `canRun(player, destino)` | Si tiene sentido ahora (p. ej. `wrong_destination` necesita otro jugador) |
| `redirect(player, destino)` | Opcional: devuelve un destino nuevo o `null` |
| `announceDelayTicks()` | Retrasa el mensaje del chat (lo usa `fake_ban` para no destripar la broma) |
| `run(player)` | El efecto |

`AbstractEvent` añade `opt("clave", valorPorDefecto)`, que lee `events.<id>.<clave>` en cada ejecución. Por eso `/jetlag reload` cambia, por ejemplo, el radio de la TNT al instante.

### `Messages`
- Al arrancar copia `lang/en.yml` y `lang/es.yml` a la carpeta del plugin si no existen.
- Carga el idioma de `language:` y usa el `en.yml` del jar como **respaldo**: si en una actualización añades una clave nueva, los servidores con un `es.yml` antiguo no ven mensajes vacíos.
- Colores con `&` (funcionan en todas las versiones). Placeholders en pares: `get("clave", "player", "Steve")`.

### `Stats`
Un contador por UUID (los nombres pueden cambiar) guardado en `stats.yml`. Solo escribe si hay cambios (`dirty`). Se guarda cada 5 minutos y al apagar.

### `JetLagCommand`
Un `switch` con los subcomandos y un `TabCompleter` que solo sugiere los subcomandos de admin a quien tenga `jetlag.admin`.

---

## 5. Cómo se consigue la compatibilidad con muchas versiones

Es la parte más delicada. La estrategia tiene cuatro reglas:

**1. Compilar contra la versión más antigua soportada (1.16.5).**
Si algo compila contra la API de 1.16.5, existe también en las versiones nuevas: Bukkit casi nunca *borra* métodos, solo los marca como `@Deprecated`. Al revés no funciona: si compilas contra 1.21 y usas un método nuevo, el plugin peta en 1.16.

**2. Bytecode Java 8.**
Con `options.release.set(8)`, el `.jar` se ejecuta en Java 8 (servidores 1.16.5) y en Java 17/21+ (servidores modernos).

**3. Nunca referenciar constantes que Mojang ha renombrado.** En su lugar, se buscan por nombre en tiempo de ejecución:

| Cosa | Problema | Solución en Jet Lag |
|---|---|---|
| Sonidos | `Sound` dejó de ser un `enum` en 1.21.3 y algunos cambiaron de nombre | `world.playSound(loc, "entity.creeper.primed", ...)` con la clave de texto, estable desde 1.9 |
| Efectos de poción | `CONFUSION` → `NAUSEA` y `SLOW` → `SLOWNESS` en 1.20.5 | `Compat.potion("NAUSEA", "CONFUSION")` prueba ambos nombres |
| Atributos | `GENERIC_MOVEMENT_SPEED` → `MOVEMENT_SPEED` en 1.21.3 | No se usan: la velocidad del creeper es un efecto de poción |
| Partículas | Varios renombres en 1.20.5 | No se usan: los efectos visuales son fuegos artificiales |
| Causas de teletransporte | Algunas no existen en todas las versiones | Se leen de la config por nombre, ignorando las desconocidas |

**4. `api-version: '1.16'` en `plugin.yml`.**
Le dice al servidor que el plugin usa nombres de materiales modernos (no los "legacy" de antes de 1.13), y los servidores nuevos lo cargan sin problema.

> ⚠️ **Folia** (el fork multihilo de Paper) no está soportado, porque usamos el planificador clásico de Bukkit. Para soportarlo habría que usar `RegionScheduler`, que no existe en 1.16.5.

---

## 6. Cómo añadir un evento nuevo

Ejemplo: **"yeet"**, que lanza al jugador por los aires con caída lenta.

**1. Crea `src/main/java/dev/jetlag/events/YeetEvent.java`:**

```java
package dev.jetlag.events;

import dev.jetlag.JetLagPlugin;
import dev.jetlag.util.Compat;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class YeetEvent extends AbstractEvent {

    public YeetEvent(JetLagPlugin plugin) {
        super("yeet", plugin);
    }

    @Override
    public void run(Player player) {
        player.setVelocity(new Vector(0, opt("power", 1.5), 0));
        Compat.effect(player, 8, 0, "SLOW_FALLING");
        Compat.sound(player.getLocation(), "entity.firework_rocket.launch", 1f, 1f);
    }
}
```

**2. Regístralo** en el constructor de `EventRegistry`:

```java
register(new YeetEvent(plugin));
```

**3. Añádelo a `config.yml`:**

```yaml
  yeet:
    enabled: true
    weight: 8
    power: 1.5
```

**4. Añade el mensaje** en `lang/en.yml` y `lang/es.yml`, dentro de `events:`:

```yaml
  yeet: '&e{player} &7was yeeted into the stratosphere.'
```

**5. Prueba con** `/jetlag test yeet`.

Antes de usar un método de la API, comprueba que existe en 1.16.5 (si compila con el `build.gradle.kts` del proyecto, existe). Si el nombre de un sonido o efecto ha cambiado entre versiones, pasa los dos nombres a `Compat`.

---

## 7. Cómo probarlo en local

1. **Compila:** `./gradlew build` → `build/libs/JetLag-1.0.0.jar`.
2. **Monta un servidor de prueba:** descarga Paper de https://papermc.io/downloads, ponlo en una carpeta, ejecuta `java -jar paper.jar` una vez, acepta la EULA (`eula=true`) y copia el jar a `plugins/`.
3. **Pruebas básicas** (con OP):
   - `/jetlag chance 100`: así todos los teletransportes disparan.
   - `/tp ~ ~ ~10` varias veces.
   - `/jetlag test <evento>` para cada uno de los 12.
   - Con un segundo jugador (o una segunda cuenta), prueba `wrong_destination`.
   - En el Nether, comprueba que `sky_drop` no aparece.
   - Pon un `/home` con EssentialsX y comprueba que también dispara.
   - Comprueba que la TNT y el creeper **no rompen bloques**.
4. **Prueba al menos tres versiones** antes de publicar: **1.16.5** (con Java 8 o 11), **1.20.4** (Java 17) y **la última** (Java 21+). Si funciona en esas tres, casi seguro que funciona en las intermedias.

> **Nota sobre `saveConfig()`:** `/jetlag chance` y `/jetlag on|off` guardan `config.yml`. En servidores anteriores a 1.18, Bukkit borra los comentarios del archivo al guardarlo. No rompe nada, pero conviene saberlo.

---

## 8. Ideas para futuras versiones

- Más eventos: `yeet`, "intercambio de posición con otro jugador", "llegas en una jaula de cristal", "te sigue un enjambre de abejas pacíficas".
- Integración con **bStats** para ver cuántos servidores usan el plugin (muy útil para enseñar en Modrinth).
- Comprobador de actualizaciones contra la API de Modrinth.
- Eventos por mundo, o pesos distintos por permiso.
- Una API pública para que otros plugins registren sus propios eventos.
