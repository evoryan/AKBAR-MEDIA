#!/bin/bash

# Pastikan script dijalankan dari direktori yang sama
cd "$(dirname "$0")" || exit

while true; do
    echo "============================="
    echo "    MANAJEMEN TENANT CLI     "
    echo "============================="
    echo "1. Tambah Tenant Baru"
    echo "2. Hapus Tenant"
    echo "3. Disable Tenant (Blokir Login)"
    echo "4. Enable Tenant (Buka Blokir)"
    echo "0. Keluar"
    echo "============================="
    
    read -p "Pilih menu (0-4): " choice
    
    case $choice in
        1)
            echo ""
            echo "=== Setup Tenant Baru ==="
            read -p "Masukkan nama database tenant (contoh: akbar_media_client2): " DB_NAME
            if [ -z "$DB_NAME" ]; then echo "Batal."; continue; fi
            read -p "Masukkan Nama Lengkap Admin: " ADMIN_NAME
            if [ -z "$ADMIN_NAME" ]; then echo "Batal."; continue; fi
            read -p "Masukkan Username untuk Login: " USERNAME
            if [ -z "$USERNAME" ]; then echo "Batal."; continue; fi
            read -p "Masukkan Password untuk Login: " PASSWORD
            if [ -z "$PASSWORD" ]; then echo "Batal."; continue; fi
            
            node manage_tenant.js add "$DB_NAME" "$ADMIN_NAME" "$USERNAME" "$PASSWORD"
            ;;
        2)
            echo ""
            echo "=== Hapus Tenant ==="
            node manage_tenant.js list
            read -p "Masukkan Username dari tenant yang ingin dihapus (kosongkan untuk batal): " USERNAME
            if [ -z "$USERNAME" ]; then echo "Batal."; continue; fi
            read -p "⚠️ ANDA YAKIN INGIN MENGHAPUS TENANT INI DAN SEMUA DATANYA? (y/n): " CONFIRM
            if [ "$CONFIRM" = "y" ] || [ "$CONFIRM" = "Y" ]; then
                node manage_tenant.js delete "$USERNAME"
            else
                echo "Batal."
            fi
            ;;
        3)
            echo ""
            echo "=== Disable Tenant ==="
            node manage_tenant.js list
            read -p "Masukkan Username dari tenant yang ingin di-disable: " USERNAME
            if [ -n "$USERNAME" ]; then
                node manage_tenant.js disable "$USERNAME"
            fi
            ;;
        4)
            echo ""
            echo "=== Enable Tenant ==="
            node manage_tenant.js list
            read -p "Masukkan Username dari tenant yang ingin di-enable: " USERNAME
            if [ -n "$USERNAME" ]; then
                node manage_tenant.js enable "$USERNAME"
            fi
            ;;
        0)
            echo "Keluar..."
            exit 0
            ;;
        *)
            echo "Pilihan tidak valid!"
            ;;
    esac
    echo ""
done
