# Social Media Relationship Analyzer

This Java project models and analyzes a social network as a graph. Users are nodes, friendships are edges. Features include:
- Add/remove users and friendships
- BFS/DFS traversals
- Shortest path between users
- Common friends, friend suggestions
- Community detection

## Structure
- `src/graph` — Graph and traversal logic
- `src/user` — User model and management
- `src/analysis` — Analysis and advanced features

## Requirements
- Java 8+
- No external dependencies required for core features

## Usage
Build and run using VS Code tasks or your preferred Java IDE.

## Uso con GraphStream

Para compilar y ejecutar con visualización, asegúrate de incluir los `.jar` de GraphStream en el classpath:

### Compilar:

```
javac -cp "librarys/gs-core-2.0.jar;librarys/gs-ui-swing-2.0.jar;bin" -d bin src/**/*.java
```

### Ejecutar:

```
java -cp "librarys/gs-core-2.0.jar;librarys/gs-ui-swing-2.0.jar;bin" Main
```

> Si usas PowerShell, usa `;` como separador. Si usas CMD, usa `;` o `:` según tu sistema.
