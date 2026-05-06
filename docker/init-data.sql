-- Initial setup for the NS Fatima Calendar MVP
-- This script seeds the database with foundational data for local testing.

-- 1. Create the external-dependency 'organizacoes' table (if it doesn't exist)
CREATE TABLE IF NOT EXISTS calendario.organizacoes (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL
);

-- 2. Seed Locations (locais)
-- Using fixed UUIDs for deterministic testing in the demo script
INSERT INTO calendario.locais (id, nome, tipo, capacidade, ativo)
VALUES 
    ('00000000-0000-0000-0000-000000000001', 'Igreja Principal', 'IGREJA', 500, TRUE),
    ('00000000-0000-0000-0000-000000000002', 'Salão Paroquial', 'SALAO', 300, TRUE),
    ('00000000-0000-0000-0000-000000000003', 'Sala de Catequese 1', 'SALA', 30, TRUE)
ON CONFLICT (id) DO NOTHING;

-- 3. Seed Organizations/Pastorals (organizacoes)
INSERT INTO calendario.organizacoes (id, nome)
VALUES 
    ('00000000-0000-0000-0000-0000000000aa', 'Catequese'),
    ('00000000-0000-0000-0000-0000000000bb', 'Liturgia'),
    ('00000000-0000-0000-0000-0000000000cc', 'Pascom'),
    ('00000000-0000-0000-0000-0000000000dd', 'Clero')
ON CONFLICT (id) DO NOTHING;

-- 4. Seed Users (usuarios)
-- password_hash is {noop}password for testing simplicity (Spring Security NoOpPasswordEncoder)
INSERT INTO calendario.usuarios (id, username, password_hash, enabled)
VALUES 
    ('10000000-0000-0000-0000-000000000001', 'padre.pedro', '{noop}senha123', TRUE),
    ('10000000-0000-0000-0000-000000000002', 'maria.catequese', '{noop}senha123', TRUE),
    ('10000000-0000-0000-0000-000000000003', 'joao.liturgia', '{noop}senha123', TRUE)
ON CONFLICT (id) DO NOTHING;

-- 5. Seed Memberships (membros_organizacao)
INSERT INTO calendario.membros_organizacao (id, usuario_id, organizacao_id, tipo_organizacao, papel, ativo)
VALUES 
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-0000000000dd', 'CLERO', 'paroco', TRUE),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-0000000000aa', 'PASTORAL', 'coordenador', TRUE),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-0000000000bb', 'PASTORAL', 'membro', TRUE)
ON CONFLICT DO NOTHING;
