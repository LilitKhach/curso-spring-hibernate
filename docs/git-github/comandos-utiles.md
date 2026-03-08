# Comandos Git Utiles

Referencia rapida para el dia a dia del curso.

---

## Configuracion Inicial (una sola vez)

```bash
git config --global user.name "Tu Nombre"
git config --global user.email "tu@email.com"
```

---

## Flujo de Trabajo Diario

```bash
# Ver que archivos cambiaron
git status

# Agregar archivos
git add src/main/java/com/pizzeria/modelo/Pizza.java

# Agregar todo
git add .

# Crear commit
git commit -m "Agregar entidad Pizza con anotaciones JPA"

# Subir a tu fork
git push
```

---

## Recibir Actualizaciones del Profesor

```bash
git fetch upstream
git merge upstream/main
git push
```

---

## Emergencias

```bash
# Deshacer cambios en un archivo (antes de add)
git checkout -- archivo.java

# Quitar del staging (despues de add, antes de commit)
git reset HEAD archivo.java

# Ver historial
git log --oneline
```

---

## Reglas

1. Commit frecuente (al menos 1 por sesion)
2. Mensajes descriptivos ("Agregar entidad Pizza" no "cambios")
3. Push al final del dia
4. NO hacer force push
