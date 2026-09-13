# Propuesta de aplicación móvil — Gestión y aprendizaje financiero

**Equipo:** Diego Hermilo Guillén García (AL07018240), Santiago Gutiérrez Rivera (AL03094306), Christopher Eduardo Soto Betancourt (AL07098518), Abraham Radahi Bautista Triana (AL07124721)
**Materia:** Desarrollo de Aplicaciones Móviles
**Fecha:** 31 de agosto de 2026

## 1. Descripción de la aplicación

Aplicación móvil de finanzas personales orientada a jóvenes sin experiencia previa en manejo del dinero. El núcleo del sistema clasifica cada ingreso como **fijo** o **variable**, y cada gasto como **esencial** o **recreativo**, y cruza ambas clasificaciones en un **indicador de equilibrio financiero** que refleja qué tan saludable es la relación entre lo que la persona gana y cómo lo gasta.

A partir de ese indicador, la aplicación entrega consejos contextuales (no un curso separado) y reportes concisos que permiten al usuario identificar puntos de mejora en su comportamiento financiero. Complementan el sistema la gestión de múltiples cuentas bancarias y tarjetas de crédito, y la definición de metas de ahorro con seguimiento de avance.

## 2. Problema que resuelve

Las aplicaciones de finanzas personales de uso general (Mint, Fintonic, YNAB) registran ingresos y gastos, pero no cruzan el tipo de ingreso con el tipo de gasto en un indicador único y accionable, y no traducen ese cruce en un consejo inmediato en el momento del registro. El usuario joven sin experiencia financiera necesita algo más que un historial de movimientos: necesita entender, en el momento, si su forma de gastar es sostenible frente a su forma de ingresar dinero.

## 3. Usuario objetivo

**Principal:** jóvenes (estudiantes y profesionales en inicio de carrera) sin experiencia previa en administración financiera personal, que combinan ingresos fijos (mesada, sueldo) y variables (trabajos independientes, propinas) y quieren aprender a equilibrar su gasto sin herramientas complejas de nivel corporativo.

## 4. Diferenciador

El valor no está en la cantidad de funciones sino en el cruce: ningún competidor accesible combina tipo de ingreso × tipo de gasto en un solo indicador de equilibrio, con tip contextual inmediato. Enseña mientras se usa, no aparte en un módulo educativo separado.

## 5. Alcance — entregable del semestre

### Dentro
- Registro y autenticación de usuario.
- Alta de ingresos: monto, tipo (fijo/variable), fecha, cuenta destino.
- Alta de gastos: monto, categoría (esencial/recreativo), fecha, cuenta/tarjeta origen, comprobante opcional.
- Gestión de múltiples cuentas bancarias y tarjetas de crédito (alta, edición, saldo).
- Cálculo del indicador de equilibrio financiero (ingreso fijo/variable vs. gasto esencial/recreativo).
- Panel de reportes conciso: distribución de gasto, tendencia, puntos de mejora.
- Tips contextuales según comportamiento (reglas sobre % de gasto recreativo, uso de tarjeta, etc.).
- Metas de ahorro: creación (monto objetivo, plazo) y seguimiento de avance.
- Alertas: desvío del equilibrio saludable, meta de ahorro en riesgo, vencimiento de tarjeta.

### Fuera (trabajo futuro)
- Mascota de la aplicación: alertas de gasto ilustradas en plan gratuito.
- Mascota como asistente conversacional en plan Pro.
- Reportes históricos comparativos entre periodos extensos, multi-moneda, apertura de datos vía open banking / conexión bancaria automática.

## 6. Requerimientos funcionales

| # | Requerimiento |
|---|----------------|
| RF01 | El sistema debe permitir registrar y autenticar usuarios. |
| RF02 | El sistema debe permitir registrar un ingreso con monto, tipo (fijo/variable), fecha y cuenta destino. |
| RF03 | El sistema debe permitir registrar un gasto con monto, categoría (esencial/recreativo), fecha, cuenta/tarjeta origen y comprobante opcional (foto). |
| RF04 | El sistema debe permitir dar de alta, editar y consultar el saldo de múltiples cuentas bancarias y tarjetas de crédito. |
| RF05 | El sistema debe calcular y mostrar el indicador de equilibrio financiero cruzando tipo de ingreso y tipo de gasto. |
| RF06 | El sistema debe presentar un panel de reportes con distribución de gasto por categoría y tendencia en el tiempo. |
| RF07 | El sistema debe generar tips contextuales basados en reglas sobre el comportamiento financiero registrado. |
| RF08 | El sistema debe permitir crear metas de ahorro (monto objetivo, plazo) y mostrar su avance. |
| RF09 | El sistema debe emitir alertas cuando el indicador de equilibrio se desvía, una meta de ahorro está en riesgo, o una tarjeta está por vencer. |
| RF10 | El sistema debe limitar, en el plan gratuito, el número combinado de cuentas bancarias y tarjetas de crédito registrables. |

## 7. Requerimientos no funcionales

| # | Requerimiento |
|---|----------------|
| RNF01 | Seguridad: los datos financieros deben cifrarse en tránsito (TLS) y en reposo; las contraseñas se almacenan con hash. |
| RNF02 | Usabilidad: el registro de un gasto o ingreso debe completarse en pocos toques, con interfaz pensada para uso en móvil. |
| RNF03 | Rendimiento: el cálculo del indicador de equilibrio y la generación de reportes deben responder en tiempo casi real (percibido &lt;1s con datos locales). |
| RNF04 | Disponibilidad: la aplicación debe permitir registrar movimientos sin conexión y sincronizarlos al recuperar conectividad. |
| RNF05 | Privacidad: el usuario debe poder borrar su cuenta y sus datos desde la aplicación. |
| RNF06 | Mantenibilidad: la arquitectura debe organizarse por capas (datos, lógica de negocio, presentación) para facilitar cambios aislados. |
| RNF07 | Compatibilidad: la aplicación se desarrolla en Android nativo (Kotlin), con una versión mínima de SDK a definir en la fase de implementación. |
| RNF08 | Identidad visual: interfaz minimalista y limpia, con paleta verde-morado, que proyecte calma y sensación de control del día a día. |

## 8. Modelo de monetización — Freemium

| Función | Gratis | Pago (Pro) |
|---|---|---|
| Cuentas bancarias + tarjetas de crédito (tope combinado) | Hasta 2 elementos en total | Ilimitadas |
| Metas de ahorro | 1 activa | Ilimitadas |
| Indicador de equilibrio financiero | Sí | Sí |
| Reportes | Básicos (periodo actual) | Con histórico |
| Tips contextuales | Básicos | Avanzados y personalizados |
| Exportación de reportes | No | PDF y Excel |
| Publicidad | Sí | No |
| Mascota (futuro) | Alertas ilustradas | Asistente conversacional |

**Criterio del corte:** el tope combinado de cuentas y tarjetas está pensado para que un usuario con vida financiera simple (una cuenta, una tarjeta) opere cómodo en el nivel gratuito, mientras que quien maneja varias cuentas y tarjetas —perfil con mayor capacidad de pago— alcanza el límite rápido. El indicador de equilibrio financiero se mantiene deliberadamente gratuito por ser el diferenciador del producto: restringirlo impediría que el usuario descubra el valor central y, por tanto, que se convierta.

## 9. Riesgos identificados

| Riesgo | Mitigación |
|---|---|
| Competencia consolidada (Mint, Fintonic, YNAB) | La propuesta no compite en cantidad de funciones sino en el indicador cruzado, ausente en esas plataformas. |
| Baja conversión a plan Pro | Nivel gratuito diseñado como canal de adquisición, no como producto sacrificado. |
| Alcance excesivo para el plazo del semestre | Delimitación explícita del entregable (sección 5) y registro de funciones diferidas. |
| Sensibilidad del dato financiero | Cifrado en tránsito/reposo (RNF01) y borrado de datos a pedido del usuario (RNF05) desde el diseño. |

## 10. Mockup

Generado con la skill `pen-design`. Archivos: `designs/finanzas-app.pen` (editable) y `designs/finanzas-app.png` (exportado, 5 pantallas).

**Sistema visual:** verde `#2F7D5B` / `#1D4F3A` para ingreso y gasto esencial; morado `#6C4CE0` para gasto recreativo y crédito; fondo `#F5F7F3`; tarjetas blancas con radio 20-24px. Tipografía Plus Jakarta Sans (cifras/títulos) + Inter (cuerpo). Tab bar capsular flotante.

**Pantallas:**
1. **Home** — saldo disponible, tarjeta *Tu equilibrio* con gauge 0-100 y barras apiladas ingreso fijo/variable vs. gasto esencial/recreativo, tip contextual, lista de movimientos.
2. **Registro rápido** — alterna gasto/ingreso, monto grande, toggle esencial/recreativo (captura el cruce en el momento del registro), categoría, cuenta, teclado numérico.
3. **Cuentas y tarjetas** — carrusel de cuentas/tarjetas, uso de línea de crédito, alertas de corte y pago mínimo.
4. **Metas** — meta destacada con anillo de avance y abono rápido, metas secundarias, racha de ahorro.
5. **Reportes** — resumen ingresos/gastos/ahorro con variación, tendencia de 6 meses, matriz 2×2 del cruce ingreso×gasto en lenguaje simple, top de categorías.
