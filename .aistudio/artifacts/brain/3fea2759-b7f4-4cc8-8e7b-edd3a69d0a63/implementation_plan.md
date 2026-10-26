# VentaAlPaso - Gestión de Ventas, Inversión y Ganancias

Aplicación Android intuitiva y de alta velocidad diseñada específicamente para vendedores ambulantes, puestos de mercado y comerciantes independientes (como venta de pan en la calle o puestos de comida/mercancía). Permite registrar ventas en segundos, categorizar claramente la inversión inicial, reinversión de insumos y gastos operativos, calcular en tiempo real la ganancia neta y generar reportes mensuales formateados listos para compartir por WhatsApp o copiar al portapapeles.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> Decisiones confirmadas a través de la interacción inicial:
> 1. **Modalidad de registro de ventas**: Catálogo rápido táctil con botones grandes de productos frecuentes + Venta libre rápida (monto directo en efectivo/transferencia sin producto previo).
> 2. **Clasificación financiera de egresos**: Separación explícita de movimientos en 3 rubros:
>    - **Inversión**: Capital inicial o compras de herramientas/equipamiento (ej. canasta, exhibidor, balanza, delantal).
>    - **Reinversión**: Compra de insumos y reposición de mercadería para seguir produciendo y vendiendo (ej. harina, manteca, levadura, bolsas, producto mayorista).
>    - **Gasto Operativo**: Costos diarios y operativos (ej. transporte, permiso municipal, combustible, almuerzo de trabajo).
> 3. **Exportación de reportes**: Reporte mensual en texto formateado profesionalmente (con emojis estructurados, totales claros y desglose) con botones directos para **Copiar al portapapeles** y **Compartir por WhatsApp / Mensajería**.

---

## 1. Overview & Core Concept

- **El Problema**: El vendedor que trabaja solo en la calle o en un puesto no tiene tiempo de llenar formularios lentos entre cliente y cliente. A menudo mezcla la ganancia del día con el dinero que debe reinvertir mañana en ingredientes o mercadería, terminando el mes sin saber si realmente ganó dinero o se comió el capital.
- **La Solución**: Una pantalla principal de cobro con botones grandes de 1 toque (ej. "Pan Batayola", "Pan Francés", "Criollo", "Factura", "+$500 Libre") con resumen de la venta actual, y una pestaña de Finanzas que calcula de forma transparente:
  - `Ventas Totales` - `Reinversión` - `Gastos` = **Ganancia Neta Disponible**.
  - Seguimiento del retorno de la **Inversión Inicial**.
- **Público Objetivo**: Vendedores de pan a pie o en bicicleta, puestos de ferias y mercados, vendedores de café/tortas, food carts y pequeños emprendedores solitarios.

---

## 2. User Experience & Visual Design

### Navegación y Pantallas Principales

1. **Terminal de Ventas (Punto de Venta Rápido)**:
   - Encabezado con selector de turno/día y total vendido hoy.
   - Grilla con botones grandes de productos con precio destacado e indicador táctil inmediato.
   - Botón de "Venta Libre" (teclado numérico rápido para importes personalizados).
   - Canasta rápida en la parte inferior con confirmación de cobro en 1 toque (Efectivo o Transferencia/QR) y sonido/vibración de feedback.
2. **Finanzas y Movimientos (Inversión / Reinversión / Gastos)**:
   - Tarjetas de resumen métrico con colores contrastantes:
     - **Verde**: Total Ingresos por Ventas.
     - **Azul**: Reinversión en Mercadería/Insumos (lo que vuelve a producir).
     - **Naranja**: Inversión en Activos/Herramientas.
     - **Rojo/Terracota**: Gastos Operativos.
     - **Dorado / Verde Esmeralda**: Ganancia Neta Real.
   - Botón flotante para registrar egreso con selector rápido de 3 categorías (`Inversión`, `Reinversión`, `Gasto Operativo`).
3. **Reportes y Exportación**:
   - Selector de Mes y Año (ej. Octubre 2026).
   - Desglose del mes: Días trabajados, ventas acumuladas, reinversión total, gastos operativos, inversión amortizada y ganancia neta.
   - Vista previa del reporte de texto formateado listo para enviar.
   - Acciones principales: **Compartir en WhatsApp** (usa `Intent.ACTION_SEND`), **Copiar Texto** e **Imprimir/Visualizar**.
4. **Catálogo de Productos**:
   - Agregar, editar o pausar productos (Nombre, Precio de Venta, Costo Estimado de Insumo, Color/Icono).

### Estilo Visual y Paleta (Material 3 Optimizado para Exteriores)
- **Modo Alto Contraste para la Calle**: Colores profundos y tipografía de alto peso visual para legibilidad bajo luz solar directa.
- **Paleta de Colores**:
  - `Primary`: Ámbar cálido / Tostado artesanal (`#B25E00` / `#FFB77C` en dark) evocando panadería, calor de puesto y comercio tradicional.
  - `Secondary / Success`: Verde Bosque esmeralda (`#1B5E20` / `#81C784`) para ganancias e ingresos.
  - `Tertiary / Reinvestment`: Azul Índigo (`#1565C0` / `#90CAF9`) para reinversiones.
  - `Expense`: Terracota intenso (`#C62828` / `#EF9A9A`) para gastos.
  - `Surface`: Fondos limpios y elevados con esquinas redondeadas y botones de toque mínimo de 56dp para operación rápida con una sola mano.

---

## 3. Key Product Decisions & Trade-Offs

- **Almacenamiento Local Robusto con Room SQLite**:
  - *Decisión*: Base de datos SQLite local completa con Room (entidades reactivas vía `Flow`).
  - *Por qué*: El vendedor ambulante frecuentemente pierde señal en esquinas, subtes o ferias abiertas sin Wi-Fi. La app debe funcionar al 100% offline, con cero demoras de red y sin requerir cuentas ni contraseñas que dificulten el inicio del día.
- **Exportación en Texto Enriquecido vs Archivos Complejos**:
  - *Decisión*: Generador de texto estructurado con formato visual limpio para WhatsApp y portapapeles.
  - *Por qué*: Según la preferencia seleccionada por el usuario, el texto plano formateado puede enviarse directamente a un socio, contador, cónyuge o bloc de notas sin necesidad de tener Excel o visores de hojas de cálculo instalados.

---

## 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────────┐
│                       Jetpack Compose UI                        │
│  ┌─────────────────┐  ┌──────────────────┐  ┌────────────────┐  │
│  │ SalesScreen     │  │ FinanceScreen    │  │ ReportsScreen  │  │
│  │ (Catalog & POS) │  │ (Invest/Expense) │  │ (Export text)  │  │
│  └────────┬────────┘  └────────┬─────────┘  └───────┬────────┘  │
└───────────┼────────────────────┼────────────────────┼───────────┘
            │                    │                    │
┌───────────▼────────────────────▼────────────────────▼───────────┐
│                      AppViewModel (MVVM)                        │
│  - uiState: StateFlow<SalesUiState>                             │
│  - recordSale(), recordExpense(), generateMonthlyReportText()   │
└────────────────────────────────┬────────────────────────────────┘
                                 │
┌────────────────────────────────▼────────────────────────────────┐
│                    Repository & Room Database                   │
│  ┌───────────────────────┐          ┌────────────────────────┐  │
│  │ ProductDao            │          │ TransactionDao         │  │
│  │ (Items, Prices, Cost) │          │ (Sales, Expenses, Cat) │  │
│  └───────────────────────┘          └────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │ SQLite Room Database ("ventaalpaso.db")                    │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### Modelo de Datos (Room Entities)

1. **`Product`**:
   - `id: Long` (autoGenerate)
   - `name: String` (ej. "Baguette", "Torta Frita", "Café con Leche")
   - `price: Double` (precio de venta al público)
   - `costPrice: Double` (costo estimado de insumo por unidad)
   - `category: String`
   - `isActive: Boolean`
   - `colorHex: Long`

2. **`TransactionRecord`**:
   - `id: Long` (autoGenerate)
   - `timestamp: Long`
   - `type: TransactionType` (`SALE`, `EXPENSE`)
   - `expenseCategory: ExpenseCategory?` (`INVESTMENT`, `REINVESTMENT`, `OPERATING_EXPENSE`)
   - `amount: Double`
   - `paymentMethod: PaymentMethod` (`CASH`, `TRANSFER`)
   - `description: String`
   - `productId: Long?`
   - `quantity: Int`

3. **Formato del Reporte Mensual (Texto para WhatsApp)**:
   ```text
   📊 REPORTE MENSUAL: OCTUBRE 2026
   🏪 Negocio: Venta al Paso / Puesto
   -----------------------------------
   💰 Total Ventas (Ingresos): $ 450,000
   🔄 Total Reinversión (Insumos): $ 180,000
   🛠️ Total Inversión (Capital): $ 40,000
   📦 Total Gastos Operativos: $ 30,000
   -----------------------------------
   📉 GASTO TOTAL DEL MES: $ 250,000
   💵 GANANCIA NETA REAL: $ 200,000
   📈 Margen Neto: 44.4%
   -----------------------------------
   ⭐ Productos más vendidos:
   1. Pan Casero (320 u.) - $160,000
   2. Facturas docena (110 u.) - $132,000
   3. Criollos 1/2 kg (95 u.) - $95,000
   -----------------------------------
   Generado con VentaAlPaso
   ```

---

## 5. Implementation Steps

1. **Configuración de Room & Metadatos**:
   - Establecer `name` y `description` en `metadata.json` y `strings.xml`.
   - Implementar entidades, DAOs y Room Database con inicialización de datos de muestra para que la app arranque lista para usar con productos típicos de venta al paso (Pan, medialunas, café, etc.).
2. **Capa de Lógica & Repositorio**:
   - Repositorio reactivo con Flows para cálculos en tiempo real de ingresos, inversiones, reinversiones, gastos totales y balance neto.
   - Formateador de reportes de texto y generador de intents de compartir.
3. **Pantallas Compose**:
   - **Punto de Venta**: Botones de catálogo rápido con incremento de cantidades, entrada de venta libre y cobro instantáneo.
   - **Registro de Inversión y Gastos**: Diálogo modal simplificado con selector de categoría (Inversión inicial, Reinversión en insumos, Gastos operativos).
   - **Panel Financiero**: Gráficos visuales de barras/proporción en Canvas Jetpack Compose con balances y KPIs claros.
   - **Pantalla de Reporte Mensual**: Selector de mes/año, previsualización en tarjeta estilizada y botones de exportación rápida.
4. **Verificación**:
   - Compilación con `compile_applet` y prueba de flujo.
