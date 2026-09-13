# Equilibrio — Manual de identidad de marca

**Equipo:** Diego Hermilo Guillén García (AL07018240), Santiago Gutiérrez Rivera (AL03094306), Christopher Eduardo Soto Betancourt (AL07098518), Abraham Radahi Bautista Triana (AL07124721)
**Materia:** Desarrollo de Aplicaciones Móviles · 5to Semestre 2026-2
**Fecha:** 08 de septiembre de 2026
**Fuente:** `docs/Equilibrio.pdf` §05 (Identidad de marca) y `docs/superpowers/specs/2026-08-31-app-finanzas-personales-design.md`

---

## 01. Principio rector

**El color comunica la clasificación antes que el texto.**

Cada ingreso es fijo o variable; cada gasto es esencial o recreativo. La aplicación cruza ambas clasificaciones en un indicador único. Por eso el sistema visual distingue, con el color, **verde para ingreso y gasto esencial**, **morado para gasto recreativo y crédito** — antes de que el usuario lea una sola palabra.

Este principio es la restricción que ordena todas las decisiones de este documento: ningún token de color nuevo puede competir, confundirse ni reemplazar ese significado ya establecido.

---

## 02. Paleta base

### Colores fijos (definidos en el PDF, no se tocan)

| Token | Hex | Significado |
|---|---|---|
| `color.base.green` | `#2F7D5B` | Ingreso · Gasto esencial |
| `color.base.green-deep` | `#1D4F3A` | Verde profundo — gradientes, énfasis |
| `color.base.purple` | `#6C4CE0` | Gasto recreativo · Crédito |
| `color.base.bg` | `#F5F7F3` | Fondo de la aplicación |

### Colores derivados (introducidos por este manual)

| Token | Hex | Derivación | Uso |
|---|---|---|---|
| `color.base.green-mid` | `#7EAE99` | verde mezclado 38% hacia blanco | serie secundaria: ingreso variable en gráficas |
| `color.base.green-soft` | `#E3EFE7` | verde mezclado ~90% hacia el fondo | rellenos suaves: insignias, toggle "Esencial" |
| `color.base.purple-mid` | `#A490EC` | morado mezclado 38% hacia blanco | acento secundario, degradado de tarjeta de crédito |
| `color.base.purple-soft` | `#EDE9FB` | morado mezclado ~90% hacia el fondo | fondo de tarjeta "Tip de hoy", toggle "Recreativo" |
| `color.neutral.surface` | `#FFFFFF` | blanco puro | tarjetas |
| `color.neutral.ink` | `#1C2420` | negro cálido, familia de tono verde | texto primario |
| `color.neutral.ink-muted` | `#5B675E` | ink aclarado | texto secundario |
| `color.neutral.ink-faint` | `#8A948A` | ink aclarado más — **no usar en cuerpo de texto** | metadatos, timestamps |
| `color.neutral.border` | `#E3E8E3` | ink-faint mezclado ~80% hacia blanco | bordes de tarjeta y chip inactivo |
| `color.neutral.border-strong` | `#CBD4CC` | ink-faint mezclado ~55% hacia blanco | borde de énfasis, divisor |

Los neutros se derivan tiñendo hacia el verde de marca en baja saturación, en vez de usar gris puro — así toda la interfaz, incluidos textos y bordes, queda en la misma familia tonal del verde principal. Es lo que hace que la paleta se lea como diseñada y no como gris de librería genérica.

---

## 03. Tokens semánticos — el problema verde/morado resuelto

Verde y morado ya **significan** algo en este producto: clasifican dinero. Por eso el reflejo genérico "éxito = verde, error = rojo" es una trampa — un mensaje de éxito en verde compite visualmente con el verde que ya codifica "ingreso".

> **Regla dura:** verde y morado son **colores de dominio** — clasifican dinero. Los colores de retroalimentación (éxito, advertencia, error, información) son **colores de estado** — describen la confianza del sistema en una acción. Los colores de dominio nunca aparecen en un rol de retroalimentación, y los colores de retroalimentación nunca aparecen como relleno de una categoría (nunca colorean un chip, una insignia, un monto o un segmento de gráfica).

| Token semántico | Valor (claro) | Origen | Nota |
|---|---|---|---|
| `semantic.ingreso` | `#2F7D5B` | fijo | montos `+$4,200`, insignias de ingreso |
| `semantic.ingreso-fijo` | `#1D4F3A` | derivado | sub-tono en barra apilada / leyenda |
| `semantic.ingreso-variable` | `#7EAE99` | derivado | sub-tono en barra apilada / leyenda |
| `semantic.gasto-esencial` | `#2F7D5B` | fijo | mismo tono que ingreso (ambos "dominio verde") |
| `semantic.gasto-recreativo` | `#6C4CE0` | fijo | montos `−$85`, insignias de recreativo |
| `semantic.credito` | `#6C4CE0` / `#A490EC` | fijo | tarjeta de crédito, uso de línea |
| `semantic.superficie` | `#FFFFFF` | derivado | tarjetas |
| `semantic.fondo` | `#F5F7F3` | fijo | fondo de pantalla |
| `semantic.borde` | `#E3E8E3` | derivado | `#CBD4CC` para énfasis |
| `semantic.texto-primario` | `#1C2420` | derivado | títulos, montos, cuerpo |
| `semantic.texto-secundario` | `#5B675E` | derivado | subtítulos, metadatos |
| `semantic.texto-terciario` | `#8A948A` | derivado | solo timestamps — nunca cuerpo |
| `semantic.exito` | `#2F7D5B` | reutiliza verde | solo en confirmaciones ligadas a una acción monetaria positiva ("Guardado") |
| `semantic.advertencia` | `#9A6400` | nuevo (ámbar) | nunca verde ni morado |
| `semantic.error` | `#B3492E` | nuevo (terracota, no rojo puro) | ver §04 |
| `semantic.informacion` | `#2E5FB3` | nuevo (azul) | ver §04 |

**Por qué no rojo puro para error:** un rojo saturado (`#E53935`) competiría visualmente con el sistema y se leería como una tercera categoría de gasto. Un terracota/óxido cumple la función de alarma sin robarle protagonismo al morado. Ámbar y azul, igual: tonos que no existen en ningún otro lugar de la paleta de dominio.

---

## 04. Accesibilidad de color — contraste calculado (WCAG)

Todos los ratios siguen la fórmula de luminancia relativa WCAG. AA texto de cuerpo exige ≥4.5:1; AA texto grande, ≥3:1.

### Paleta base

| Par | Ratio | AA cuerpo | AA grande |
|---|---|---|---|
| `ink` #1C2420 sobre blanco | 15.88 | ✅ | ✅ |
| `ink` sobre fondo `#F5F7F3` | 14.73 | ✅ | ✅ |
| `ink-muted` #5B675E sobre blanco | 5.92 | ✅ | ✅ |
| `ink-muted` sobre fondo | 5.49 | ✅ | ✅ |
| `ink-faint` #8A948A sobre blanco | 3.14 | ❌ | ✅ |
| `ink-faint` sobre fondo | 2.91 | ❌ | ❌ |
| `green` #2F7D5B sobre blanco | 4.99 | ✅ | ✅ |
| `green` sobre fondo | 4.63 | ✅ | ✅ |
| `green-deep` sobre blanco / fondo | 9.41 / 8.73 | ✅ | ✅ |
| `purple` #6C4CE0 sobre blanco | 5.60 | ✅ | ✅ |
| `purple` sobre fondo | 5.20 | ✅ | ✅ |
| blanco sobre `green` (botón) | 4.99 | ✅ | ✅ |
| blanco sobre `purple` (chip) | 5.60 | ✅ | ✅ |
| `green` sobre `green-soft` #E3EFE7 | 4.22 | ❌ | ✅ |
| `green-deep` sobre `green-soft` | 7.96 | ✅ | ✅ |
| `purple` sobre `purple-soft` #EDE9FB | 4.71 | ✅ | ✅ |

### Colores de retroalimentación

| Token | Sobre blanco | Sobre fondo | Modo oscuro sobre `#14181A` |
|---|---|---|---|
| `error` `#B3492E` | 5.37 ✅ | 4.98 ✅ | `#E08A6D` ≈ 7.1 ✅ |
| `error-deep` `#8C3520` | 7.94 ✅ | 7.36 ✅ | `#C86A4C` ≈ 5.4 ✅ |
| `advertencia` `#9A6400` | 5.00 ✅ | 4.64 ✅ | `#D9A64A` ≈ 7.8 ✅ |
| `informacion` `#2E5FB3` | 6.17 ✅ | 5.72 ✅ | `#7FA6E8` ≈ 6.9 ✅ |

### Dos fallas detectadas y su corrección

1. **`ink-faint` falla AA en todos lados** (3.14 / 2.91). Corrección: se restringe a texto decorativo no esencial (un timestamp ya redundante con el orden), nunca por debajo de 16sp, y nunca como único portador de información. Donde hoy se usaría para algo que sí importa (ej. "MXN · toca para editar"), se promueve a `ink-muted`.
2. **`green` sobre `green-soft` falla AA de cuerpo** (4.22, se necesita 4.5). Corrección: el texto/icono dentro de una insignia con relleno `green-soft` siempre usa `green-deep` (7.96:1). El verde base se reserva para relleno sólido sobre blanco o fondo.

---

## 05. Modo oscuro

Método: aclarar y desaturar levemente cada acento manteniendo su tono — nunca invertir a un color más oscuro. Mismo método ya usado en `designs/reporte-propuesta.html`.

| Token claro | Hex claro | Token oscuro | Hex oscuro | Contraste sobre fondo oscuro |
|---|---|---|---|---|
| `bg` | `#F5F7F3` | `bg.dark` | `#14181A` | — |
| `surface` | `#FFFFFF` | `surface.dark` | `#1D2321` | — |
| `ink` | `#1C2420` | `ink.dark` | `#EDEEE9` | 15.32 ✅ |
| `ink-muted` | `#5B675E` | `ink-muted.dark` | `#A7B0A8` | 8.01 ✅ |
| `ink-faint` | `#8A948A` | `ink-faint.dark` | `#727C74` | 4.13 (solo texto grande) |
| `green` | `#2F7D5B` | `green.dark` | `#5FBE94` | 7.90 ✅ |
| `green-deep` | `#1D4F3A` | `green-deep.dark` | `#3E8F6C` | 4.55 ✅ |
| `green-soft` | `#E3EFE7` | `green-soft.dark` | `#16261E` | (relleno, no texto) |
| `purple` | `#6C4CE0` | `purple.dark` | `#A392F5` | 6.80 ✅ |
| `purple-deep` | `#4E35B0` | `purple-deep.dark` | `#9C8CF2` | 6.34 ✅ |
| `purple-soft` | `#EDE9FB` | `purple-soft.dark` | `#211C38` | (relleno, no texto) |
| `border` | `#E3E8E3` | `border.dark` | `#2A2F2C` | (no textual) |
| `error` | `#B3492E` | `error.dark` | `#E08A6D` | ≈7.1 ✅ |
| `advertencia` | `#9A6400` | `advertencia.dark` | `#D9A64A` | ≈7.8 ✅ |
| `informacion` | `#2E5FB3` | `informacion.dark` | `#7FA6E8` | ≈6.9 ✅ |

---

## 06. Tipografía

Dos familias: **Plus Jakarta Sans** (cifras y títulos) + **Inter** (cuerpo y UI).

| Rol | Fuente | Tamaño (sp) | Peso | Interlineado | Tracking | Uso |
|---|---|---|---|---|---|---|
| `display-cifra` | Plus Jakarta Sans | 40 | 700 | 44 (1.1) | −0.02em | `$5,440`, el `72` del gauge |
| `display-cifra-sm` | Plus Jakarta Sans | 28 | 700 | 32 (1.14) | −0.01em | monto secundario (`$240` en teclado) |
| `h1` | Plus Jakarta Sans | 24 | 700 | 30 (1.25) | −0.01em | título de pantalla |
| `h2` | Plus Jakarta Sans | 18 | 600 | 24 (1.33) | 0 | encabezado de tarjeta |
| `h3` | Plus Jakarta Sans | 15 | 600 | 20 (1.33) | 0 | subsección |
| `body` | Inter | 15 | 400 | 22 (1.47) | 0 | texto de fila, tips |
| `body-strong` | Inter | 15 | 600 | 22 (1.47) | 0 | énfasis en línea |
| `body-small` | Inter | 13 | 400 | 18 (1.38) | 0 | subtítulo de fila |
| `label` | Inter | 13 | 600 | 16 (1.23) | 0.01em | botones, chips, tab bar |
| `caption` | Inter | 11 | 500 | 14 (1.27) | 0.02em | etiquetas, "MXN", deltas |
| `numeric-tabular` | Plus Jakarta Sans | según contexto | 600–700 | según contexto | 0 | cifras tabulares — `tnum` activado siempre |

**Regla:** todo dígito de dinero o porcentaje usa Plus Jakarta Sans con cifras tabulares (`font-feature-settings: "tnum" 1`), nunca Inter — así los montos alinean en columna en listas y reportes.

---

## 07. Espaciado (escala base 4pt)

| Token | Valor | Uso |
|---|---|---|
| `space.4` | 4dp | separación icono-etiqueta dentro de un chip |
| `space.8` | 8dp | separación entre segmentos de barra apilada |
| `space.12` | 12dp | separación entre insignia de fila y su texto |
| `space.16` | 16dp | margen de pantalla; padding de tarjetas compactas |
| `space.20` | 20dp | padding estándar de tarjeta |
| `space.24` | 24dp | separación entre tarjetas; padding de tarjetas grandes |
| `space.32` | 32dp | separación entre secciones mayores |
| `space.48` | 48dp | respiro bajo la barra de estado |

**Reglas de componente:** fila de lista mínimo 56dp de alto · área táctil mínima 48×48dp en todo elemento tocable · radio de tarjeta 20dp estándar / 24dp en tarjetas hero · margen de pantalla 16dp a ambos lados.

---

## 08. Radios y elevación

| Token | Valor | Uso |
|---|---|---|
| `radius.sm` | 12dp | botones de teclado, etiquetas pequeñas |
| `radius.md` | 20dp | tarjeta blanca estándar |
| `radius.lg` | 24dp | tarjetas hero (gauge, cuentas, tip) |
| `radius.pill` | 999dp | chips, toggles, tab bar, botones |
| `radius.circle` | 50% | insignias de icono, FAB, avatar |

| Elevación | Sombra | Uso |
|---|---|---|
| `elevation.0` | ninguna | elementos planos, chips en reposo |
| `elevation.1` | `0 1px 2px rgba(28,36,32,.04), 0 4px 12px rgba(28,36,32,.05)` | tarjeta blanca sobre el fondo |
| `elevation.2` | `0 2px 4px rgba(28,36,32,.06), 0 12px 28px rgba(28,36,32,.08)` | tab bar flotante, FAB, hoja inferior |
| `elevation.3` | `0 4px 8px rgba(28,36,32,.08), 0 16px 40px rgba(28,36,32,.10)` | diálogo modal |

Las sombras siempre son el color de tinta a baja opacidad — nunca negro puro, nunca coloreadas. Mantiene la sensación "suave, en calma".

---

## 09. Movimiento

| Token | Duración | Curva | Uso |
|---|---|---|---|
| `motion.instant` | 100ms | linear | selección de chip, check de casilla |
| `motion.fast` | 160ms | `cubic-bezier(.2,0,0,1)` | feedback de botón presionado |
| `motion.base` | 220ms | `cubic-bezier(.2,0,0,1)` | entrada de tarjeta, cambio de tab |
| `motion.slow` | 320ms | `cubic-bezier(.3,0,.2,1)` | apertura de hoja, transición de pantalla |
| `motion.gauge-fill` | 600–800ms | `cubic-bezier(.16,1,.3,1)` | llenado del arco de equilibrio 0–100 |

**Regla de marca:** ninguna curva con rebote (spring/bounce) en todo el sistema. El rebote comunica urgencia o juego; el trabajo de esta app es *bajar* la ansiedad, así que todo movimiento desacelera suavemente hacia el reposo.

**Reducción de movimiento:** con `prefers-reduced-motion` activo, toda duración colapsa a ~1ms excepto el llenado del gauge, que hace crossfade del número en vez de animar el arco — nunca se elimina toda la retroalimentación, porque el número que cambia es información relevante.

---

## 10. Iconografía — Material Symbols Rounded

### Configuración

- **Familia:** Material Symbols **Rounded** — estándar de Android, coherente con el trazo redondeado observado en el mockup.
- **FILL:** 0 (contorno) en reposo, 1 (relleno) en estado activo/seleccionado — codifica el estado también por forma, no solo por color, en beneficio de usuarios daltónicos dado que toda la marca descansa en verde-vs-morado.
- **wght:** 400 por defecto (no bajar de 300 ni subir de 500).
- **GRAD:** 0 en modo claro, +200 en modo oscuro.
- **opsz:** 20 en chips/contextos densos, 24 por defecto (filas, botones, tab bar), 40 en contextos hero (FAB, ilustración de estado vacío).

### Reglas de color de insignia

| Tipo de insignia | Relleno | Color de icono | Uso |
|---|---|---|---|
| Sólida | `green` o `purple` | blanco | chip activo, insignia de tip, FAB, botón primario |
| Suave | `green-soft` o `purple-soft` | `green-deep` / `purple-deep` (nunca el tono base — ver §04) | icono de fila, tile de estadística, celda de matriz |
| Neutra | `#EEF1EE` | `ink-muted` | ítem sin clasificar, menú, ajustes |

**Regla dura:** un movimiento nunca recibe insignia verde o morada hasta que el usuario lo clasificó como esencial/recreativo o fijo/variable. Un color por defecto lo clasificaría en silencio antes que el texto — lo opuesto exacto de la promesa de marca. Todo movimiento nuevo usa insignia Neutra.

### Mapeo icono → función

| Función | Nombre Material Symbols | Color de insignia |
|---|---|---|
| Inicio (tab) | `home` | neutra / `green-soft` activa |
| Cuentas (tab) | `account_balance_wallet` | neutra / `green-soft` activa |
| Metas (tab) | `flag` | neutra / `green-soft` activa |
| Reportes (tab) | `pie_chart` | neutra / `green-soft` activa |
| Notificaciones | `notifications` | neutra |
| Volver | `chevron_left` | neutra |
| Avanzar | `chevron_right` | neutra |
| Agregar (FAB) | `add` | sólida verde o morada según contexto |
| Confirmación de sistema | `check_circle` | familia feedback |
| Tip de hoy | `lightbulb` | sólida morada |
| Comida | `restaurant` | suave verde (esencial) |
| Transporte | `directions_bus` | suave verde (esencial) |
| Súper | `shopping_cart` | suave verde (esencial) |
| Salidas / antojos | `local_bar` | suave morada (recreativo) |
| Suscripciones | `repeat` | suave morada (recreativo) |
| Música | `music_note` | suave morada (recreativo) |
| Otro (sin clasificar) | `more_horiz` | neutra |
| Cuenta bancaria | `account_balance` | sólida/degradado verde profundo |
| Tarjeta de crédito | `credit_card` | sólida/degradado morada |
| Ahorro / meta | `savings` | suave verde |
| Borrar dígito | `backspace` | neutra |
| Confirmar / guardar | `check` | blanco sobre botón sólido verde |
| Sincronización | `autorenew` | neutra / informacion si es estado pendiente |
| Racha de ahorro | `local_fire_department` | sólida morada |
| Meta "laptop" | `laptop_mac` | suave verde |
| Servicios / nube | `cloud` | suave verde |
| Fecha | `calendar_month` | neutra |
| Tendencia positiva | `trending_up` | verde (codifica dato, no feedback) |
| Delta positivo | `north_east` | verde |
| Delta negativo (gasto sube) | `south_east` | morado — preserva el significado de dominio, no rojo |
| Tip destacado | `auto_awesome` | sólida morada |
| Editar | `edit` | neutra |
| Buscar | `search` | neutra |
| Eliminar (destructivo) | `delete` | `feedback.error` — único lugar donde el error-rojo aparece legítimamente como tinte de icono |
| Advertencia | `warning` | `feedback.advertencia` |
| Error / fallo de sync | `error` | `feedback.error` |
| Información | `info` | `feedback.informacion` |
| Sin conexión (estado, no error) | `cloud_off` | `ink-muted` — no es color de feedback |
| Seguridad | `lock` | neutra |
| Adjuntar comprobante | `photo_camera` | neutra |
| Plan Pro | `workspace_premium` | sólida morada |
| Sin resultados | `search_off` | `ink-faint`, opsz 40 |
| Estado vacío | `inventory_2` / contextual | `ink-faint`, opsz 40 |

Tamaños: 16dp en línea con texto, 20dp en chips/tab bar, 24dp en filas/botones, 40dp en insignias hero, 48dp en ilustración de estado vacío.

---

## 11. Retroalimentación, errores y avisos

### 11.1 Regla de desambiguación

Los colores de feedback solo pueden aplicarse a: (1) un Snackbar, (2) un banner, (3) validación inline de campo, (4) el acento/icono de un diálogo, (5) un chip de estado cuya propia etiqueta ya dice una palabra de feedback ("Pendiente", "Fallo de sincronización"). Nunca rellenan un chip de categoría, una insignia de movimiento, una barra o segmento de gráfica, ni el color de texto de un monto.

### 11.2 Manejo de errores — tabla de decisión

| Severidad | Persistencia | ¿Requiere acción? | Componente | Ubicación | Duración |
|---|---|---|---|---|---|
| Baja, recuperable al instante | Transitoria | Sí, inmediata | Validación inline | Bajo el campo | Hasta corregir |
| Media, destructiva e irreversible | Puntual | Sí, antes de continuar | `AlertDialog` bloqueante | Centro de pantalla | Hasta cerrar |
| Media, informativa, reversible | Transitoria | Opcional (deshacer) | Snackbar | Inferior, sobre el tab bar | 4s (6s con acción) |
| Alta, bloquea función central | Continua hasta resolver | No inmediata, pero visible | Estado de pantalla completa | Reemplaza el contenido | Hasta resolver |
| Condición vigente, no urgente | Persistente mientras dure | No | Banner persistente | Bajo el encabezado | Mientras dure |
| No es error — modo esperado sin conexión | Persistente mientras dure | No | Indicador de estado sutil | Tab bar / encabezado | Mientras dure |

**Aplicado a los flujos reales:**

- **Formulario "Nuevo movimiento":** monto vacío → borde del campo en `error`, texto de ayuda "Agrega un monto para guardar." · categoría o cuenta sin elegir → borde en `advertencia` (es guía, no un error), texto "Elige una categoría para continuar." · el monto no numérico debe prevenirse con el teclado numérico, no validarse después.
- **Diálogo bloqueante** (borrar movimiento / cuenta / meta / cuenta de usuario, RNF05): icono `delete` en insignia suave de error, título con el nombre del objeto ("¿Eliminar 'Café con Sofi'?"), cuerpo de una línea ("Esto no se puede deshacer."), botón "Cancelar" con más énfasis visual que "Eliminar" — lo destructivo nunca es el camino de menor resistencia.
- **Snackbar:** sin acción — "Cambios guardados." con `check_circle` verde, 4s. Con acción — "Movimiento eliminado." + "Deshacer" en morado, 6s, anclado 8dp sobre el tab bar flotante.
- **Sin conexión (RNF04) no es un error:** banner discreto, sin color de alarma, "Sin conexión · tus cambios se guardan y se sincronizan después." Nunca pantalla completa — la app está diseñada para funcionar offline.
- **Fallo de sincronización real:** banner en `advertencia` ("Algunos movimientos no se sincronizaron. Toca para reintentar."), escala a `error` solo tras 3+ reintentos fallidos.
- **Sesión expirada:** pantalla completa, sin color de error — es rutina, no una falla. Icono `lock` neutro, "Tu sesión terminó."
- **Banner persistente:** sincronización pendiente en `informacion` · tarjeta por vencer en `advertencia`, escalando desde `informacion` cuando faltan más de 3 días.

### 11.3 Alertas del producto (RF09)

| Alerta | Tratamiento | Color | Icono | Dónde aparece |
|---|---|---|---|---|
| Desvío del equilibrio saludable | Chip de estado en la tarjeta "Tu equilibrio" reemplaza "En control" + notificación push | `advertencia` (el gauge mantiene su propio verde/morado) | `warning` | Inicio + push |
| Meta de ahorro en riesgo | Estado inline en la tarjeta de la meta + push si el plazo se acerca | `advertencia` — nunca morado, esto es coaching, no un juicio | `warning` pequeño junto al % | Metas + push |
| Vencimiento / corte de tarjeta | Banner persistente en Cuentas + push días antes | `informacion` a más de 3 días, escala a `advertencia` dentro de los últimos 3 | `calendar_month` → `warning` | Cuentas + push |

Fórmula de copy para las tres: **qué pasa → qué puedes hacer**, una oración cada una, sin signos de exclamación, siempre con una acción concreta al final.

### 11.4 Tono de voz

**Fórmula:** [qué pasó, sin culpa] + [qué puedes hacer, concreto].

**Palabras prohibidas:** fracaso, error crítico, inválido, prohibido, incorrecto, "no puedes", "debiste", signos de exclamación en avisos/errores.

**Reglas:** nunca culpar al usuario por su gasto — los tips son coaching contextual (relación causal: esto → aquello), nunca un veredicto. Segunda persona informal ("tú"). Sustantivos concretos del dominio ("tu monto", "esta cuenta"), nunca jerga técnica.

| Antes (genérico/técnico) | Después (marca Equilibrio) |
|---|---|
| "Error: el campo monto es obligatorio." | "Agrega un monto para guardar." |
| "Valor inválido." | *(prevenido por el teclado numérico — no debería aparecer)* |
| "Debes seleccionar una categoría." | "Elige una categoría para continuar." |
| "¡Advertencia! Tu meta está en riesgo de fallar." | "Tu meta 'Viaje a la playa' va más lenta de lo planeado. Un abono de $220 esta semana la vuelve a encarrilar." |
| "Error crítico de sincronización." | "Algunos movimientos no se sincronizaron. Toca para reintentar." |
| "Sesión inválida, vuelve a iniciar sesión." | "Tu sesión terminó. Vuelve a iniciar sesión para seguir viendo tus finanzas." |
| "¿Estás seguro? Esta acción no se puede deshacer." | "¿Eliminar 'Café con Sofi'? Esto no se puede deshacer." |
| "Has alcanzado el límite de tu plan." | "Ya tienes 2 cuentas y tarjetas en tu plan gratuito. Pásate a Pro para agregar más." |

### 11.5 Estados de componente

| Componente | Default | Press | Focus | Disabled | Error |
|---|---|---|---|---|---|
| Botón primario | relleno `green` | `green-deep`, escala 0.98 | anillo 2dp `informacion` | relleno `border`, texto `ink-faint` | n/a |
| Botón secundario | borde `border-strong`, texto `ink` | relleno `green-soft`, borde `green` | anillo 2dp `informacion` | borde `border`, texto `ink-faint` | n/a |
| Chip de categoría | blanco, borde 1dp | relleno sólido `purple`, texto blanco | anillo | relleno `border`, 50% opacidad | n/a |
| Toggle Esencial/Recreativo | blanco, borde 1dp | — | — | — | — |
| — Esencial seleccionado | `green-soft` + borde `green` + `check` | | | | |
| — Recreativo seleccionado | `purple-soft` + borde `purple` | | | | |
| Campo de texto | borde 1dp `border` | borde `border-strong` | borde 2dp `informacion` | relleno `bg`, texto `ink-faint` | borde 1.5dp `error` + texto de ayuda |
| Ítem de tab bar activo | píldora `green-soft`, icono `FILL=1`, texto `green-deep` | — | anillo | n/a | n/a |
| FAB | círculo sólido `green`, elevation.2 | escala 0.95 + elevation.1 | anillo | n/a | n/a |

**Anillo de foco (accesibilidad):** 2dp sólido `informacion` (`#2E5FB3`), 2dp de separación del borde del elemento — solo se renderiza en navegación por teclado/lector de pantalla, nunca en toques táctiles.

### 11.6 Estados de pantalla no felices

- **Vacíos** (sin movimientos, cuentas, metas, búsqueda sin resultados): icono 48dp en `ink-faint`, título `h3`, una línea `body-small` en `ink-muted`, botón de acción cuando aplica. Copy siempre invitador: "Aún no tienes metas", nunca "No hay datos".
- **Carga:** skeleton loaders para listas/tarjetas de forma conocida (preserva la estabilidad del layout, se siente en calma). Spinner solo dentro de un botón tras un tap, o en el primer arranque de la app antes de conocer ningún layout.
- **Primer uso:** el gauge en 0/100 con cero datos se leería como "fallando" — se reemplaza por una tarjeta neutra: "Registra tu primer movimiento para ver tu equilibrio."
- **Límite del plan gratuito (RF10):** es un aviso comercial, no un error — debe verse claramente distinto. Acento sólido morado (alinea con "Pro"), icono `workspace_premium`, botón de acción con el mismo peso visual que un botón positivo — nunca iconografía de error, nunca ámbar/rojo.

---

## 12. Aplicación de requisitos

| Requisito | Tratamiento en este manual |
|---|---|
| RF09 (alertas) | §11.3 — los tres tipos de alerta con color, icono y ubicación exactos |
| RF10 (límite freemium) | §11.6 — tratado como aviso comercial, nunca como error |
| RNF04 (offline) | §11.2 — sin conexión es un estado, no un error; banner discreto |
| RNF05 (borrado de cuenta) | §11.2 — diálogo bloqueante con confirmación explícita |
| RNF08 (identidad visual) | Todo el documento — paleta, tipografía y radios idénticos a la fuente |

---

## 13. Anexo — tokens para Compose

```kotlin
// Color.kt
val Green = Color(0xFF2F7D5B)
val GreenDeep = Color(0xFF1D4F3A)
val GreenMid = Color(0xFF7EAE99)
val GreenSoft = Color(0xFFE3EFE7)
val Purple = Color(0xFF6C4CE0)
val PurpleMid = Color(0xFFA490EC)
val PurpleSoft = Color(0xFFEDE9FB)
val Background = Color(0xFFF5F7F3)
val Surface = Color(0xFFFFFFFF)
val Ink = Color(0xFF1C2420)
val InkMuted = Color(0xFF5B675E)
val InkFaint = Color(0xFF8A948A)
val Border = Color(0xFFE3E8E3)
val Error = Color(0xFFB3492E)
val Warning = Color(0xFF9A6400)
val Info = Color(0xFF2E5FB3)

// Spacing.kt
object Spacing {
    val xs = 4.dp; val sm = 8.dp; val md = 12.dp
    val base = 16.dp; val lg = 20.dp; val xl = 24.dp
    val xxl = 32.dp; val xxxl = 48.dp
}

// Shape.kt
object Radii {
    val sm = 12.dp; val md = 20.dp; val lg = 24.dp
    val pill = 999.dp
}
```

---

*Manual de identidad — Equilibrio · Desarrollo de Aplicaciones Móviles · Septiembre 2026*
