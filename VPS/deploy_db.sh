#!/bin/bash
echo "Menyiapkan Database MySQL..."
if ! command -v mysql &> /dev/null
then
    echo "MySQL tidak ditemukan! Silakan install mysql-server terlebih dahulu."
    exit 1
fi

echo "Masukkan password root MySQL anda:"
mysql -u root -p < init.sql

echo "Database berhasil di-deploy!"
