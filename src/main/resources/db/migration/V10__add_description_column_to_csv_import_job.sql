-- Adiciona coluna description na tabela csv_import_job
ALTER TABLE csv.csv_import_job 
ADD COLUMN IF NOT EXISTS description TEXT; 