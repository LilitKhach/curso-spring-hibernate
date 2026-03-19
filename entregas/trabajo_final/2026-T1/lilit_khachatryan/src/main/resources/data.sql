-- ======================
-- SOCIAS
-- ======================
INSERT INTO socias (id, nombre, apellido) VALUES (1, 'Maria', 'Lopez');
INSERT INTO socias (id, nombre, apellido) VALUES (2, 'Ana', 'Garcia');
INSERT INTO socias (id, nombre, apellido) VALUES (3, 'Lucia', 'Martinez');

-- ======================
-- CONTRATOS
-- ======================
INSERT INTO contratos (id, contrato_numero, socia_id)
VALUES (1, 'CTR-001', 1);

INSERT INTO contratos (id, contrato_numero, socia_id)
VALUES (2, 'CTR-002', 1);

INSERT INTO contratos (id, contrato_numero, socia_id)
VALUES (3, 'CTR-003', 2);

-- ======================
-- TRANSACCIONES
-- ======================
-- Contrato 1 (Maria)
INSERT INTO transacciones (id, monto, fecha, descripcion, tipo, contrato_id)
VALUES (1, 1000.00, '2024-01-10', 'Ingreso inicial', 'INGRESO', 1);

INSERT INTO transacciones (id, monto, fecha, descripcion, tipo, contrato_id)
VALUES (2, 200.00, '2025-01-15', 'Gasto operativo', 'GASTO', 1);

-- Contrato 2 (Maria)
INSERT INTO transacciones (id, monto, fecha, descripcion, tipo, contrato_id)
VALUES (3, 1500.00, '2025-02-01', 'Ingreso cliente', 'INGRESO', 2);

INSERT INTO transacciones (id, monto, fecha, descripcion, tipo, contrato_id)
VALUES (4, 300.00, '2025-02-05', 'Compra materiales', 'GASTO', 2);

-- Contrato 3 (Ana)
INSERT INTO transacciones (id, monto, fecha, descripcion, tipo, contrato_id)
VALUES (5, 2000.00, '2024-03-01', 'Ingreso contrato', 'INGRESO', 3);

INSERT INTO transacciones (id, monto, fecha, descripcion, tipo, contrato_id)
VALUES (6, 500.00, '2026-03-10', 'Gastos varios', 'GASTO', 3);

SELECT setval('socias_id_seq', (SELECT MAX(id) FROM socias));
SELECT setval('contratos_id_seq', (SELECT MAX(id) FROM contratos));
SELECT setval('transacciones_id_seq', (SELECT MAX(id) FROM transacciones));