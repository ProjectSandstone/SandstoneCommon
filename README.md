# SandstoneCommon

Common things shared by Sandstone implementations written to JVM.

###### Features:

- Abstract Game implementation.
- Adapter Helper.
- Constants & Sandstone Constants Initialization.
- Dependency Injection (Guice).
- EventManager & Event Listener Container.
- Plugin Manager & Loader.
- Registry Implementation.
- Scheduler Task implementation.
- Service Manager.
- Test platform.
- Utilities (Extensions, Versioning Scheme, Dependency Comparator, Time Unity conversion, etc...).


## Adapter

###### Rules: 

- Only hand-write an adapter if it's really needed.
- Register hand-written adapters in `AdapterEnvironment`.
- Use `@Implementation` only if the implementation is simple, but not easy to be written using `Adapter` annotation system.
- Prefer delegation to a external class using `@Invoke` if the implementation is bigger (do not pollute the AdapterClass with bigger `@Implementation` codes).
- When writing an implementation using `@Implementation` prefer direct invocation of Converters instead of calling methods of `AdapterEnvironment` (Adapter classes cannot have a big performance overhead).

**Remember, some things is resolved lazily and saved on cache, but other things must be resolved whenever the method is called.**