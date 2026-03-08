# Como hacer Fork y Clonar el Repositorio

Guia paso a paso para obtener tu copia del curso.

---

## Paso 1: Hacer Fork

1. Entrar a https://github.com/TodoEconometria/curso-spring-hibernate
2. Click en el boton **Fork** (arriba a la derecha)
3. Dejar el nombre por defecto
4. Click en **Create fork**

---

## Paso 2: Clonar tu Fork

```bash
git clone https://github.com/TU_USUARIO/curso-spring-hibernate.git
cd curso-spring-hibernate
```

---

## Paso 3: Configurar el upstream

```bash
git remote add upstream https://github.com/TodoEconometria/curso-spring-hibernate.git
git remote -v
```

Deben ver:

```
origin    https://github.com/TU_USUARIO/curso-spring-hibernate.git (fetch)
origin    https://github.com/TU_USUARIO/curso-spring-hibernate.git (push)
upstream  https://github.com/TodoEconometria/curso-spring-hibernate.git (fetch)
upstream  https://github.com/TodoEconometria/curso-spring-hibernate.git (push)
```

- **origin** = tu fork (donde subes TU trabajo)
- **upstream** = el repo del profesor (de donde recibes actualizaciones)

---

## Paso 4: Abrir en IntelliJ

1. Abrir IntelliJ IDEA
2. File → Open → seleccionar la carpeta `curso-spring-hibernate`
3. Los manuales estan en `manuales/`

---

## Siguiente paso

Ver [comandos-utiles.md](comandos-utiles.md) para el flujo de trabajo diario.
