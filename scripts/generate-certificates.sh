#!/bin/bash
set -e

# Diretório onde as chaves serão armazenadas
CERT_DIR="src/main/resources/certificates"

# Criar o diretório de certificados se não existir
mkdir -p ${CERT_DIR}

echo "Gerando chaves RSA para JWT..."

# Gerar chave privada
openssl genrsa -out ${CERT_DIR}/private.pem 2048

# Extrair chave pública da chave privada
openssl rsa -in ${CERT_DIR}/private.pem -pubout -out ${CERT_DIR}/public.pub

# Ajustar permissões
chmod 644 ${CERT_DIR}/private.pem
chmod 644 ${CERT_DIR}/public.pub

echo "Chaves RSA geradas com sucesso em ${CERT_DIR}/"
echo "  - private.pem: Chave privada"
echo "  - public.pub: Chave pública" 