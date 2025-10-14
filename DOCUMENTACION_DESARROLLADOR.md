# Documentación para Desarrolladores - Simulador de Planificación de Procesos

## 1. Introducción General

Este documento sirve como guía técnica para el proyecto **DespachadorAPP**, un simulador visual de algoritmos de planificación de procesos desarrollado en JavaFX. El objetivo de esta guía es facilitar la comprensión del código, su arquitectura y el proceso para realizar modificaciones o añadir nuevas funcionalidades.

La aplicación permite al usuario visualizar y comparar el comportamiento de diferentes algoritmos de planificación (FIFO, LIFO, SJF, etc.) a través de una interfaz gráfica que se actualiza en tiempo real.

## 2. Estructura del Proyecto

El código fuente se organiza de la siguiente manera:

-   `src/main/java/tecnm/celaya/edu/mx/despachadorapp/`: Contiene todo el código fuente de Java.
    -   `HelloApplication.java`: Punto de entrada de la aplicación.
    -   `HelloController.java`: Controlador para la vista del menú principal.
    -   `Process.java`: El modelo de datos que representa un proceso.
    -   `*Controller.java`: Cada algoritmo tiene su propio controlador (ej. `FifoController`, `SjfController`).
-   `src/main/resources/tecnm/celaya/edu/mx/despachadorapp/`: Contiene los recursos de la aplicación.
    -   `*.fxml`: Archivos FXML que definen la estructura de las interfaces gráficas.
    -   `dark-theme.css`: Hoja de estilos para dar a la aplicación su apariencia oscura.

## 3. Flujo de la Aplicación

1.  **Inicio**: La ejecución comienza en `HelloApplication.java`. Este carga la vista principal definida en `main-view.fxml` y la muestra en una ventana.
2.  **Menú Principal**: La vista `main-view.fxml` muestra una lista de botones, uno por cada algoritmo. El controlador `HelloController.java` gestiona los clics en estos botones.
3.  **Lanzamiento de Simulación**: Al hacer clic en un botón (ej. "Iniciar Simulación FIFO"), el `HelloController` crea una nueva ventana y carga el FXML correspondiente al algoritmo seleccionado (ej. `fifo-view.fxml`). También le asigna su controlador específico (ej. `FifoController`).

## 4. Componentes Clave

### 4.1. El Modelo de Datos: `Process.java`

Esta es la clase fundamental que representa un proceso en el sistema. Sus atributos más importantes son:

-   `pid`: Identificador del proceso.
-   `arrivalTime`: El segundo exacto en el que el proceso llega al sistema.
-   `duration`: La duración total de la ráfaga de CPU que necesita el proceso.
-   `remainingDuration`: La duración restante. Se decrementa cada segundo que el proceso está en la CPU.
-   `location` y `state`: La ubicación actual (CPU, Memoria, Salida) y el estado (Ejecutando, Esperando, Finalizado).

**Importante**: Todos estos atributos son **Propiedades de JavaFX** (`SimpleIntegerProperty`, `SimpleStringProperty`). Esto es crucial, ya que permite que la `TableView` se vincule directamente a ellos y se actualice automáticamente cuando sus valores cambian, sin necesidad de código extra.

### 4.2. La Lógica de Simulación: Los Controladores de Algoritmos

Todos los controladores de algoritmos (ej. `FifoController`) comparten una estructura y lógica de simulación comunes.

#### Arquitectura de la Simulación (¡MUY IMPORTANTE!)

La simulación sigue un modelo **"sin estado"** o de **"re-simulación"**. Esto significa que:

> **En cada "tick" del temporizador, la simulación completa se recalcula desde el segundo 0 hasta el segundo actual.**

Aunque puede parecer ineficiente, este enfoque se eligió deliberadamente para permitir la funcionalidad de los botones **"Atrás"** y **"Adelante"** de una manera sencilla. Al recalcular todo en cada paso, podemos movernos libremente hacia adelante o hacia atrás en el tiempo sin necesidad de guardar "instantáneas" complejas del estado del sistema.

El método clave que implementa esto es `runSimulationStep(boolean forward)`.

#### Métodos Principales en un Controlador:

-   `initialize()`: Se ejecuta cuando se carga la vista. Configura las tablas y arranca la simulación.
-   `generateRandomProcesses()`: Crea una lista de procesos de ejemplo con valores aleatorios.
-   `runSimulationStep()`: El corazón de la simulación. En cada llamada:
    1.  Resetea el estado de las variables principales (`cpuProcess`, `memoryQueue`, `finishedOrderList`).
    2.  Resetea el estado de todos los procesos en la lista (`location`, `state`, `remainingDuration`).
    3.  Inicia un bucle `for` desde `t = 0` hasta el `timer` actual.
    4.  En cada iteración `t` del bucle, simula los eventos de ese segundo: la llegada de nuevos procesos, la selección de un proceso para la CPU (aquí es donde reside la lógica del algoritmo específico) y el trabajo de la CPU.
-   `updateUI()`: Refleja el estado calculado en la interfaz gráfica (actualiza las etiquetas y la lista de procesos finalizados). Llama a `processStatusTable.refresh()` para forzar el redibujado de la tabla de estados.
-   `on...ButtonClick()`: Gestionan las acciones de los botones de control (Play/Pausa, Siguiente, Reiniciar).

### 4.3. Lógica Específica de Cada Algoritmo

La diferencia fundamental entre cada controlador reside en el bloque `if (cpuProcess == null && !memoryQueue.isEmpty())` dentro de `runSimulationStep()`.

-   **`FifoController`**: Usa una `Queue`. La lógica es `cpuProcess = memoryQueue.poll();`. Simple y directo.
-   **`LifoController`**: Usa una `LinkedList` como una pila. Los procesos se añaden con `memoryQueue.addFirst(p)` y se seleccionan con `cpuProcess = memoryQueue.removeFirst();`.
-   **`SjfController`**: Usa una `List`. Antes de seleccionar, ordena la cola con `memoryQueue.sort(Comparator.comparingInt(Process::getDuration));` para encontrar el más corto.
-   **`LjfController`**: Idéntico a SJF, pero con el comparador invertido: `memoryQueue.sort(Comparator.comparingInt(Process::getDuration).reversed());`.
-   **`RoundRobinLifoController`**: El más complejo. Introduce una variable `quantumCounter`. Además de la lógica LIFO, tiene un bloque que comprueba si el proceso en la CPU ha agotado su quantum (`quantumCounter >= QUANTUM`). Si es así, lo expulsa y lo devuelve a la cola de memoria.

## 5. Cómo Añadir un Nuevo Algoritmo

Gracias a la estructura actual, añadir un nuevo algoritmo es un proceso muy mecánico:

1.  **Crear la Vista FXML**: Duplica un archivo existente (ej. `fifo-view.fxml`) y renómbralo (ej. `nuevo-algo-view.fxml`). Asegúrate de cambiar el `fx:controller` en el archivo FXML para que apunte a tu nuevo controlador (ej. `tecnm.celaya.edu.mx.despachadorapp.NuevoAlgoController`).

2.  **Crear el Controlador Java**: Duplica un controlador existente (ej. `FifoController.java`) y renómbralo a `NuevoAlgoController.java`.

3.  **Modificar la Lógica de Planificación**: Dentro de `runSimulationStep()`, ve al bloque `if (cpuProcess == null && !memoryQueue.isEmpty())` y modifica la forma en que se elige el siguiente proceso de la `memoryQueue`. Esta es la única parte que necesitas cambiar para la lógica del algoritmo.

4.  **Ajustar la UI (Opcional)**: Si tu algoritmo necesita mostrar información extra (como la duración en SJF/LJF), modifica el método `updateUI()` para construir el texto de la etiqueta `memoryProcessLabel` como necesites.

5.  **Integrar en el Menú Principal**:
    -   Abre `main-view.fxml` y añade un nuevo botón para tu algoritmo.
    -   Abre `HelloController.java`, añade un nuevo método `onNuevoAlgoButtonClick()` y llama a `openSimulationWindow("nuevo-algo-view.fxml", "Simulación Nuevo Algoritmo")` dentro de él.

¡Y eso es todo! Siguiendo estos pasos, puedes integrar cualquier algoritmo no apropiativo de manera rápida y consistente.
