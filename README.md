# Almundo.com Call Center
Ejercicio de Java para Almundo.com.

## Documentación
La documentación se encuentra provista en forma de documentación de código (JavaDoc) y de
un diagrama UML de clases.

### JavaDoc
La documentación JavaDoc no se encuentra compilada en este repositorio Git. Para verla,
ejecutar:
```
mvn site # Genera documentación de miembros públicos
mvn javadoc:javadoc # Para, además, ver los miembros privados
```

### Diagrama UML de clases
![Diagrama UML](uml_class_diagram.svg?raw=true)
([Archivo generado con IntelliJ](callCenterClassDiagram.uml))

## Puntos Extra
* Cuando no hay ningún empleado libre, la llamada quedará "on-hold" hasta que sea posible
  atenderla. Los tests unitarios cubriendo este caso se encuentran ya provistos.
* Cuando entran más de 10 llamadas concurrentes, las llamadas en exceso quedarán "on-hold"
  hasta que algún worker thread (de los que debería existir uno por línea telefonica) se
  libere. Los tests unitarios cubriendo este caso se encuentran ya provistos.

## Notas

### Posible 'Denial of Service'
Si se crearan llamadas en gran volumen, tantas que no fuese posible manejar (este es un
escenario que no tiene mucha relación con la realidad en el contexto dado, esto es, un call
center), la cola de tareas crecería irrestrictamente y eso causaría algunos problemas
(mínimos) de retraso en el 'dispatch' de las llamadas, así como un (ligero) crecimiento en
la carga. Para solucionar este inconveniente, podría proveerse un callback de "hang up" a
cada llamada (`PhoneCall`), en el que la tarea se remueva de la cola, pero, nuevamente, en
un contexto de una central telefónica de una empresa, esta prevención no es necesaria.

### Ausencia de la clase CallCenter
Decidí no crear una clase `CallCenter`, ya que no tenía funcionalidad diferencial alguna.
Dado el contexto del ejercicio, una clase `Dispatcher` parece ser suficiente.
En un contexto de producción, la clase última nombrada será parte, muy probablemente de la
primera, teniendo `CallCenter` funcionalidad relevante.
