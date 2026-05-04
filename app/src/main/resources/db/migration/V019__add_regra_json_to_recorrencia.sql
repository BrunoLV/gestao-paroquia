ALTER TABLE calendario.eventos_recorrencia ADD COLUMN regra_json TEXT NOT NULL DEFAULT '';
ALTER TABLE calendario.eventos_recorrencia ADD COLUMN data_fim_utc TIMESTAMP WITH TIME ZONE;
ALTER TABLE calendario.eventos_recorrencia ALTER COLUMN regra_json DROP DEFAULT;
