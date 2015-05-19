# Proto
Proto is a really simple to use project to serialize Android cursors in objects.

Proto has the annotation DatabaseField, that is responsible to associate a database column with a POJO field.
Only fields annotated with DatabaseField will be serialized.

```java
public class Entity {

    @DatabaseField(columnName = "_id")
    private Long id;

    @DatabaseField(columnName = "name")
    private String name;

    @DatabaseField(columnName = "enabled")
    private Boolean enabled;

    // getters and setters
}
```

Serialize the current position of cursor to a object of type informed in second parameter.
```java
Entity entity = Proto.object(cursor, Entity.class);
```

Serialize all positions of cursor to a list of objects of type informed in second parameter.
```java
List<Entity> entities = Proto.list(cursor, Entity.class);
```

By default Proto close the cursors passed by parameter after the serialization, but this is configurable.
```java
Proto.configuration().closeCursor(true);
```

### Using with Proguard

Add the follow line:
```
-keepattributes *Annotation*
````