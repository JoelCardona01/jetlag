# Guía de publicación: GitHub, webs de plugins y donaciones

Paso a paso para pasar del código a tener Jet Lag publicado y aceptando donaciones.

---

## 0. Personaliza los marcadores

Antes de subir nada, busca y reemplaza estos textos:

| Marcador | Dónde aparece | Ponle |
|---|---|---|
| `TU_USUARIO` | `README.md`, `plugin.yml`, `config.yml`, `.github/FUNDING.yml` | Tu usuario de GitHub (y de Ko-fi, si es otro, en `FUNDING.yml`) |
| `TU_NOMBRE` | `LICENSE`, `plugin.yml` (`authors`) | Tu nombre o nick |

Truco rápido (Linux/macOS/Git Bash):

```bash
grep -rl "TU_USUARIO" . | xargs sed -i 's/TU_USUARIO/tuusuario/g'
grep -rl "TU_NOMBRE"  . | xargs sed -i 's/TU_NOMBRE/Tu Nombre/g'
```

Opcional: si quieres que el paquete Java lleve tu nombre (`io.github.tuusuario.jetlag`), cambia `dev.jetlag` en las carpetas, en los `package` y en `main:` de `plugin.yml`. No es obligatorio.

---

## 1. Compilar y probar

```bash
./gradlew build          # Windows: gradlew.bat build
```

El jar sale en `build/libs/JetLag-1.0.0.jar`. Pruébalo en un servidor de prueba siguiendo la sección 7 de `ARQUITECTURA.md`. **No publiques sin probarlo en al menos una versión vieja y una nueva.**

---

## 2. Subirlo a GitHub (repositorio público)

1. En GitHub: **New repository** → nombre `jetlag` → **Public** → *sin* README (ya tienes uno).
2. En la carpeta del proyecto:

```bash
git init
git add .
git commit -m "Jet Lag 1.0.0"
git branch -M main
git remote add origin https://github.com/TU_USUARIO/jetlag.git
git push -u origin main
```

3. En la pestaña **Actions** verás que el workflow `Build` compila el plugin. Si sale en verde, todo va bien.
4. En **About** (rueda dentada de la portada), añade descripción y *topics*: `minecraft`, `paper`, `spigot`, `minecraft-plugin`, `bukkit`, `fun`.

### Publicar una versión (Release)

El workflow crea una Release con el jar adjunto automáticamente cuando subes un *tag* que empiece por `v`:

```bash
git tag v1.0.0
git push origin v1.0.0
```

Para cada versión nueva: sube `version` en `build.gradle.kts`, añade una entrada en `CHANGELOG.md`, haz commit y crea el tag nuevo (`v1.0.1`, `v1.1.0`...).

Sigue *versionado semántico*: `1.0.1` para arreglos, `1.1.0` para eventos nuevos y `2.0.0` si cambias la config de forma incompatible.

---

## 3. Webs donde publicarlo

Te recomiendo este orden, de más a menos impacto para un plugin nuevo:

### Modrinth — https://modrinth.com
- Es la plataforma más moderna y acepta plugins (Paper, Spigot, Purpur).
- Crear proyecto → tipo **Plugin** → sube el jar.
- **Loaders:** marca Paper, Purpur, Spigot y Bukkit. **Versiones de juego:** desde 1.16.5 hasta la última que hayas probado.
- **Environment:** *Server-side only*.
- Descripción: puedes pegar el `README.md`.
- En **Links**, añade el repositorio de GitHub y tus enlaces de donación.
- Modrinth reparte ingresos por publicidad a los creadores (se activa en los ajustes de tu cuenta, en *Monetization*).

### Hangar — https://hangar.papermc.io
- Es la plataforma **oficial de PaperMC**, así que encaja perfecto con un plugin de Paper.
- Inicia sesión con tu cuenta de PaperMC → *New Project* → sube el jar en *Versions*.
- Plataforma: **Paper**, con el rango de versiones probado.

### SpigotMC — https://www.spigotmc.org/resources/
- Es la web clásica, con muchísimo público. Jet Lag solo usa la API de Bukkit, así que también funciona en Spigot.
- *Post Resource* → categoría **Fun**. Puedes poner el enlace de descarga a la Release de GitHub o subir el jar.
- Su editor usa BBCode, no Markdown: tendrás que adaptar el README.

### CurseForge (Bukkit Plugins) — https://www.curseforge.com/minecraft/bukkit-plugins
- Menos activa para plugins hoy en día, pero suma visibilidad. Los proyectos pasan por moderación manual, que puede tardar unos días.

> **Consejo:** usa el mismo nombre, icono y descripción en todas. Así, quien busque "Jet Lag" lo reconoce en cualquier sitio.

---

## 4. Donaciones

Configúralas antes del lanzamiento para que los enlaces salgan desde el primer día.

| Opción | Cómo | Notas |
|---|---|---|
| **Ko-fi** | Crea una cuenta en https://ko-fi.com y conecta PayPal o Stripe | Lo más rápido de montar. Las donaciones sueltas no tienen comisión de plataforma |
| **GitHub Sponsors** | https://github.com/sponsors → *Join the waitlist / Get started* | Necesita aprobación y datos fiscales. Queda muy integrado: aparece el botón ❤ *Sponsor* en el repo |
| **PayPal.me** | https://paypal.me | Sencillo, pero la gente lo usa menos para "invitar a un café" |

Cuando tengas cuenta:
1. Edita `.github/FUNDING.yml` (borra las líneas que no uses). GitHub mostrará el botón **Sponsor** automáticamente.
2. Pon los mismos enlaces en el README (sección *Support the project*) y en los *Links* de Modrinth y Hangar.

Sé realista: un plugin gracioso suele recibir pocas donaciones, y lo que más ayuda es que salga en vídeos. Las donaciones llegan cuando la gente ve que el proyecto se mantiene vivo.

---

## 5. Darlo a conocer

Lo que más descargas trae, de más a menos:

1. **Un GIF o vídeo corto** (10-30 s) de reacciones reales: un amigo hace `/home` y le cae la TNT. Ponlo en la cabecera de Modrinth y en el README.
2. **Clips para TikTok / YouTube Shorts** del estilo "Minecraft, pero cada teletransporte tiene efectos secundarios".
3. **Publicar en comunidades:** r/admincraft (para dueños de servidores), r/Minecraft (con el clip) y servidores de Discord de Paper/Modrinth en los canales de *showcase*.
4. **Escribir a youtubers pequeños o medianos** que hacen series con amigos. Un vídeo vale más que cualquier otra cosa.
5. **Responder rápido** a los *issues* de GitHub y a los comentarios. Las actualizaciones frecuentes hacen que las plataformas te muestren más.

---

## 6. Lista de comprobación antes del lanzamiento

- [ ] Marcadores `TU_USUARIO` y `TU_NOMBRE` reemplazados
- [ ] Probado en 1.16.5, 1.20.x y la última versión
- [ ] Probado con EssentialsX (`/tpa`, `/home`)
- [ ] Repo público en GitHub con el workflow en verde
- [ ] Release `v1.0.0` creada con el jar
- [ ] Icono del proyecto (512×512, por ejemplo un avión con un creeper)
- [ ] GIF o vídeo de demostración
- [ ] Cuenta de Ko-fi o GitHub Sponsors y `FUNDING.yml` actualizado
- [ ] Publicado en Modrinth y Hangar (y opcionalmente SpigotMC y CurseForge)
