# Persona 3 — RF01 (Login), RF09 (Alertas), RF10 (Freemium)

**Estado al:** 2026-09-20
**Rama de trabajo:** `Login` (sobre `main`, commit base `2961436`)

Este documento registra qué se encontró ya construido de una sesión anterior, qué se agregó en esta ronda de trabajo, y qué queda pendiente. No reemplaza al código — si hay duda, el código manda.

---

## 1. Resumen ejecutivo

| RF | Estado | Nota |
|---|---|---|
| **RF10 — Límite freemium** | ✅ Completo | Ya estaba hecho antes de esta sesión (`PlanTier`, `CheckFreemiumLimitUseCase`, diálogo en `AddAccountScreen`). No se tocó. |
| **RF09 — Alertas** | ✅ Completo | Esquema/DAO/UseCases ya existían; en esta sesión se conectó lo que faltaba: banner en Cuentas y Metas, y el disparo real de la verificación de desvío. |
| **RF01 — Login** | ✅ Completo (Google + correo/contraseña), **opcional** | Google Sign-In (Credential Manager) ya existía. Se agregó login/registro con correo y contraseña. El login **no bloquea** el uso de la app — es accesible desde Perfil, no un gate de arranque. |

---

## 2. Archivos nuevos

| Archivo | Propósito |
|---|---|
| `core-data/.../security/PasswordHasher.kt` | Hash/verificación de contraseñas con Argon2id (`com.lambdapioneer.argon2kt:argon2kt:1.6.0`). |
| `core-domain/.../usecase/RegisterWithPasswordUseCase.kt` | Caso de uso: registro con correo/contraseña. |
| `core-domain/.../usecase/SignInWithPasswordUseCase.kt` | Caso de uso: login con correo/contraseña. |
| `app/.../auth/LoginUiState.kt` | Estado + validaciones (email, longitud de contraseña, confirmación) de la pantalla de login. |
| `app/.../auth/LoginViewModel.kt` | ViewModel de `LoginScreen`: maneja login/registro por contraseña y también el botón de Google (reutiliza `GoogleAuthClient`). |
| `app/.../auth/LoginScreen.kt` | Pantalla Compose con tabs "Iniciar sesión"/"Registrarse", accesible desde Perfil (no es la pantalla de arranque). |
| `app/.../goals/GoalsPlaceholderViewModel.kt` | Alimenta el banner de alertas `GOAL_AT_RISK` en la ruta de Metas mientras no exista `:feature-goals` (Persona 2). |
| `app/.../goals/GoalsPlaceholderScreen.kt` | Reemplaza el placeholder estático de Metas; ahora sí muestra el banner de alertas. |

## 3. Archivos modificados

### RF01 — Login con correo/contraseña
| Archivo | Cambio |
|---|---|
| `gradle/libs.versions.toml` | Agregada dependencia `argon2kt` (versión 1.6.0). |
| `core-data/build.gradle.kts` | Agregado `implementation(libs.lambdapioneer.argon2kt)`. |
| `core-data/.../entity/UserEntity.kt` | Nueva columna `password_hash` (TEXT nullable). |
| `core-data/.../dao/UserDao.kt` | Nuevo query `getByEmail(email)`. |
| `core-data/.../local/Migrations.kt` | Nueva `MIGRATION_7_8` (agrega `password_hash`). |
| `core-data/.../local/EquilibrioDatabase.kt` | Versión de esquema `7 → 8`. |
| `core-data/.../di/DataModule.kt` | Registrada `MIGRATION_7_8` en el builder de Room. |
| `core-domain/.../model/User.kt` | Nuevo campo `hasPassword`; `isSignedIn` ahora es `googleId != null || hasPassword`. |
| `core-data/.../mapper/UserMapper.kt` | Mapea `hasPassword = passwordHash != null`. |
| `core-domain/.../repository/UserRepository.kt` | Nuevos métodos `registerWithPassword` / `signInWithPassword` (con `Result`). |
| `core-data/.../repository/UserRepositoryImpl.kt` | Implementación de ambos métodos + inyecta `PasswordHasher`. **Además:** se corrigió `signInWithGoogle` para que ya no sobreescriba el nombre de usuario elegido en Perfil en cada re-login (ver §5). |
| `app/.../auth/ProfileScreen.kt` | Botón nuevo "Usar correo y contraseña" (secundario, junto al de Google) quenavega a `LoginScreen`. |
| `app/.../navigation/EquilibrioNavHost.kt` | Nueva ruta `login`, alcanzable solo desde Perfil — **no** es el `startDestination` (login opcional, no forzado). |

> **Nota de diseño importante:** se intentó primero un *guard* de navegación que forzaba `LoginScreen` como pantalla de arranque si no había sesión. El usuario pidió revertirlo — la app debe seguir funcionando como invitado (modo Fase 1 original) y el login queda como algo secundario/opcional, accesible desde Perfil. Ese guard (`SessionGateViewModel`, `ObserveIsLoggedIn`) se creó y luego se **eliminó** por completo; no quedó rastro en el estado actual.

### RF09 — Alertas
| Archivo | Cambio |
|---|---|
| `feature-accounts/.../AccountsUiState.kt` | Nuevo `AlertUi` + `toUi()` (filtra `BALANCE_DEVIATION`); nuevo campo `pendingAlerts`. |
| `feature-accounts/.../AccountsViewModel.kt` | Inyecta `GetPendingAlertsUseCase` / `MarkAlertAsReadUseCase`; nuevo `onAlertDismissed()`. |
| `feature-accounts/.../AccountsScreen.kt` | Reemplaza el `TODO` — ahora renderiza `EqAlertBanner` sobre el contenido. |
| `feature-entry/.../QuickEntryViewModel.kt` | Inyecta `CheckBalanceDeviationAlertUseCase` y lo invoca justo después de guardar cada ingreso/gasto (antes existía el caso de uso pero nadie lo llamaba). |

### Saludo con nombre de usuario (pedido aparte)
| Archivo | Cambio |
|---|---|
| `feature-home/.../HomeViewModel.kt` | El saludo "Hola, …" ahora usa `user.displayName` (el "Nombre de usuario" editable en Perfil) en vez de `user.givenName` (nombre de la cuenta de Google). |
| `core-data/.../repository/UserRepositoryImpl.kt` | `signInWithGoogle` ya no pisa el `displayName` personalizado en cada login — solo lo siembra la primera vez si el usuario no tenía ninguno. |

---

## 4. Cómo probar sin configurar Google Cloud

El botón **"Simular sign-in (debug)"** en Perfil (solo visible en builds debug) ejecuta el mismo flujo (`signInWithGoogleUseCase`) sin tocar Google Cloud Console — sirve para probar el guard de sesión, el login opcional y el nombre de usuario mientras no haga falta el botón real de Google.

Para que el botón **real** de Google funcione hace falta reemplazar el placeholder en `app/src/main/res/values/strings.xml` (`default_web_client_id`) con un Web Client ID real de Google Cloud Console, más registrar el SHA-1 del keystore (debug: `47:4D:63:D2:F0:74:41:80:40:8D:BB:07:4A:41:1D:B9:53:72:5A:4A`; producción: el SHA-1 de *App signing key* de Play Console, no el de tu keystore local).

---

## 5. Pendiente / conocido

- **RF09:** el banner de Metas (`GOAL_AT_RISK`) y el de Tarjetas (`CARD_DUE`) seguirán vacíos hasta que Persona 2 y Persona 1 entreguen sus datos — los `TODO()` bloqueados (`CheckGoalAtRiskAlertUseCase`, `CheckCardDueAlertUseCase`) se dejaron intactos, tal como se pidió.
- **RF01:** el botón real de Google no funcionará hasta configurar el Web Client ID (ver §4). No bloquea el resto del desarrollo.
- **Base de datos:** sigue siendo 100 % local (Room/SQLite). No hay backend ni sincronización en la nube — eso está fuera de alcance hasta Fase 3 según `docs/PLAN_DESARROLLO.md`.
- **Verificación en dispositivo:** `assembleDebug`/`installDebug` corridos y confirmados sin crashear en `CPH2531` (Android 15); banner de RF09 visto renderizado en pantalla real.

---

## 6. Ronda de revisión — banner en Inicio y verificación de LocalSession

**Banner de alertas en Inicio:** no faltaba del todo — `HomeScreen.kt` ya renderizaba `EqAlertBanner` por cada alerta pendiente (heredado de antes de esta sesión), y `HomeViewModel` ya inyectaba `GetPendingAlertsUseCase`/`MarkAlertAsReadUseCase` con `HomeEvent.AlertDismissed`. El gap real: a diferencia de Cuentas, no filtraba por tipo, así que mostraría cualquier alerta (`BALANCE_DEVIATION`, `GOAL_AT_RISK` o `CARD_DUE`) en vez de solo la que le corresponde a Inicio.

- **Corregido en:** `feature-home/.../HomeUiState.kt` — `List<Alert>.toUi()` ahora filtra `AlertType.BALANCE_DEVIATION`, igual que `AccountsUiState.toUi()`.
- No se tocó `HomeScreen.kt` ni `HomeViewModel.kt` — ya seguían el patrón correcto (inyección de casos de uso, renderizado de `EqAlertBanner`, dismiss vía evento).

**`LocalSession`:** sigue activa y en uso — es el `SessionManager` real del proyecto (DataStore-backed), usado por los 5 repositorios (`UserRepositoryImpl`, `AccountRepositoryImpl`, `TransactionRepositoryImpl`, `CategoryRepositoryImpl`, `AlertRepositoryImpl`). **No** usa un id fijo/hardcodeado: `currentUserId()` solo auto-genera un usuario local la primera vez que no existe ninguno, y `setCurrentUserId()` ya lo actualiza correctamente en cada login/registro (Google o correo/contraseña, ver §3 de este documento). Las queries a Room ya usan el `userId` real de la sesión activa. **No se tocó nada** — resuelto de una ronda anterior.

**Build/instalación:** `assembleDebug` y `installDebug` exitosos. Se lanzó en `CPH2531` (Android 15) sin `FATAL EXCEPTION`/`AndroidRuntime` en logcat para `mx.equilibrio.app`. No se pudo confirmar visualmente el banner en esta ronda porque el dispositivo (del usuario) estaba en uso activo con otra app al momento de la verificación; la corrección es de una sola línea (filtro de tipo) sobre un banner que ya se sabía renderizaba correctamente en Inicio.
