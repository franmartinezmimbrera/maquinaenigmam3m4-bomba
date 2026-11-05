# Enigma M3-M4 y Bombe de Turing v1

Dos aplicaciones de escritorio del mismo proyecto, para la práctica de *Seguridad en Tecnologías de la Información*
(Universidad de Jaén):

1. **Simulador de la máquina Enigma** (modelos **M3** y **M4**): cifra y descifra mensajes con una configuración a tu elección
   y te enseña el recorrido de cada letra.
2. **Bombe de Turing**: implementación del ataque con el que Bletchley Park rompía Enigma, a partir de un texto cifrado y una
   palabra o frase que se sospecha que contiene (un *crib*).

Funcionan con **Java 8 o superior**.

## Cómo ejecutarlas

Son dos ficheros `.jar` independientes, incluidos en esta carpeta:

```bash
java -jar enigma-m3-m4.jar          # el simulador de Enigma
java -jar enigma-m3-m4-bombe.jar    # la Bombe
```

No hay que compilar nada si ya tienes los `.jar`.

### Compilarlas (solo si quieres modificarlas)

Necesitas un **JDK** (no solo el JRE) 8 o superior y **Maven**:

```bash
mvn -q test       # ejecuta los tests
mvn -q package    # genera los dos jar en target/
```

## El simulador de Enigma

Qué incluye:

- **Rotores I a VIII**, con su muesca (los VI, VII y VIII tienen dos) y el **doble paso** de los rotores.
- **Reflectores** UKW-B y UKW-C.
- **Modo M4**: casilla «Modo M4» para añadir un **cuarto rotor (Beta o Gamma)**, que se usa con un reflector fino.
- **Ajuste de anillo** y **posición inicial** de cada rotor.
- **Clavijero** con la distribución QWERTZ, de hasta 13 pares: haz clic en dos letras para conectarlas y en una letra ya
  conectada para desconectarla.
- **Teclado y lámparas** dibujados en pantalla: puedes pulsar las teclas con el ratón o con el teclado del ordenador, y la
  lámpara de salida se enciende.
- **Texto plano ↔ texto cifrado**: botones **Cifrar**, **Descifrar** y **Limpiar** (atajos Ctrl+Intro, Ctrl+D y Ctrl+L).
  Enigma es simétrica: con la misma configuración, cifrar dos veces devuelve el texto original.
- **Mostrar pasos de cifrado**: recorrido de cada letra por el clavijero, los rotores y el reflector.
- **Guardar y cargar configuración** (Archivo, Ctrl+S / Ctrl+O) en un fichero `.properties`.

Cómo empezar:

1. Pulsa **«Rotores y reflector…»** (o haz clic en las ventanas de los rotores): elige tres rotores distintos, sus anillos,
   sus posiciones iniciales y el reflector, y pulsa **Aplicar ajustes**.
2. Conecta algunos pares de letras en el clavijero.
3. Escribe el mensaje en **Texto plano** y pulsa **Cifrar**.

Detalles que conviene saber:

- Solo se cifran las letras **A–Z**. Los espacios, números, signos y letras con tilde se copian tal cual al resultado y **no**
  hacen avanzar los rotores. Escribe los mensajes sin tildes ni «ñ».
- Los cableados y muescas proceden del script de referencia de la asignatura (`enigma.py`), y el resultado coincide con él.
  En la M4, el cuarto rotor también avanza cuando el rotor izquierdo llega a su muesca, **igual que ese script** (en la máquina
  histórica no giraba): es una decisión deliberada para coincidir con la referencia.

## La Bombe de Turing

La Bombe no «adivina» el clavijero directamente: con el texto cifrado y el crib construye un grafo (el *menu*) que conecta
letras a través de cada posición de la máquina. Si ese grafo tiene un **bucle**, puede probar una hipótesis para una letra y ver
si al recorrer el bucle se llega a una contradicción. Lo repite para las 26 hipótesis y para cada orden de rotores y posición
inicial; eso es lo que hacía el *diagonal board* de la máquina real.

Cómo usarla:

1. Pega el **texto cifrado completo** y escribe el **crib** (cuanto más largo, mejor; 15–20 letras o más).
2. Pulsa **«Buscar alineaciones válidas»**: Enigma nunca cifra una letra como ella misma, y eso descarta posiciones imposibles.
3. Elige una posición (*offset*) del desplegable. La barra de estado indica cuántos **bucles** tiene el menu; conviene que haya
   al menos 2.
4. Marca los **rotores posibles** (por defecto I–V, los cinco de la práctica; puedes añadir VI, VII y VIII, pero la búsqueda tarda
   bastante más), el **reflector** y los **anillos** (los anillos hay que conocerlos o suponerlos). Beta y Gamma no aparecen: solo
   existen como cuarto rotor de la M4.
5. Pulsa **«Buscar con la Bombe»** y espera (de unos segundos a un par de minutos).
6. La tabla de **stops** muestra el orden de rotores, la posición inicial y los pares de clavijero deducidos. Prueba esa
   configuración en el simulador para descifrar el resto del mensaje.

Limitaciones (iguales que las de la Bombe histórica):

- Busca solo **orden de rotores y posición inicial**; los **anillos no se buscan**.
- Trabaja con **tres rotores** (M3). No ataca la M4.
- Necesita un menu **con al menos un bucle**; si no lo tiene, avisa en lugar de devolver una lista vacía.
- Muchos *stops* son falsos y hay que descartarlos a mano; si una alineación da 0 stops, es normal: prueba con otra.
- Si cierras la ventana mientras busca, la búsqueda se cancela.

## La práctica

Las dos herramientas se usan juntas en la práctica: cada alumno cifra un mensaje con el simulador de Enigma y rompe el de un
compañero con la Bombe.

## Estructura del proyecto

```
src/main/java/com/example/enigma/
  model/        la máquina (rotores, reflectores, clavijero, doble paso, trazas)
  bombe/        la Bombe (menu y búsqueda)
  controller/   conexión entre el modelo y la interfaz
  ui/           ventanas, teclado, lámparas, clavijero, ajustes, Bombe
src/test/java/  tests JUnit (cifrado, doble paso, clavijero, M4, Bombe, cancelación)
pom.xml         construcción con Maven (genera los dos jar)
```

## Autores y uso

Desarrollado por Francisco J. Martínez Mimbrera, Manuel J. Lucena López, Luis Gonzaga Pérez Cordón y
Cristóbal J. Carmona del Jesus (Universidad de Jaén).

**Software libre de uso educativo.** Puede utilizarse, copiarse y distribuirse sin permiso de los autores para fines
exclusivamente educativos y sin ánimo de lucro. Se ofrece «tal cual», sin garantía de ningún tipo. Esto es un aviso de uso,
no una licencia formal. El escudo de la Universidad de Jaén es propiedad de la Universidad de Jaén y no está incluido en
esta cesión de uso.
