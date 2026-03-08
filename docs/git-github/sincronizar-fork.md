# Sincronizar tu Fork

Cuando el profesor sube material nuevo, traer los cambios a tu fork.

---

## Paso a Paso

```bash
# 1. Verificar upstream
git remote -v

# Si no esta, agregarlo:
git remote add upstream https://github.com/TodoEconometria/curso-spring-hibernate.git

# 2. Descargar cambios
git fetch upstream

# 3. Aplicar a tu rama
git checkout main
git merge upstream/main

# 4. Subir a tu fork
git push origin main
```

---

## Si Hay Conflictos

```bash
git status                    # Ver archivos en conflicto
# Resolver en IntelliJ (marca con <<<<<<<, =======, >>>>>>>)
git add archivo_resuelto.java
git commit -m "Resolver conflicto"
git push
```

**Tip:** Si el conflicto es en un manual, quedarse con la version del profesor:

```bash
git checkout --theirs manuales/DIA_11_MANUAL_SPRING_BOOT_INTRO.md
git add manuales/DIA_11_MANUAL_SPRING_BOOT_INTRO.md
git commit -m "Tomar version actualizada del manual"
```

---

## Frecuencia

Sincronizar al inicio de cada dia de clase.
