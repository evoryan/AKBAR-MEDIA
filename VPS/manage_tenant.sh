#!/bin/bash

# ==============================================================================
# SCRIPT INTERAKTIF MANAJEMEN TENANT - AKBAR MEDIA BILLING & MIKROTIK
# ==============================================================================

# Pastikan script dieksekusi di dalam direktori VPS tempat script berada
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR" || exit 1

# Definisi Warna ANSI untuk Tampilan CLI yang Rapi & Menarik
COLOR_RESET="\033[0m"
COLOR_BOLD="\033[1m"
COLOR_CYAN="\033[36m"
COLOR_GREEN="\033[32m"
COLOR_YELLOW="\033[33m"
COLOR_RED="\033[31m"
COLOR_BLUE="\033[34m"
COLOR_MAGENTA="\033[35m"
COLOR_WHITE="\033[37m"

# Fungsi Header CLI
print_header() {
    clear
    echo -e "${COLOR_CYAN}${COLOR_BOLD}================================================================${COLOR_RESET}"
    echo -e "${COLOR_GREEN}${COLOR_BOLD}       AKBAR MEDIA BILLING - PANEL MANAJEMEN TENANT CLI         ${COLOR_RESET}"
    echo -e "${COLOR_CYAN}${COLOR_BOLD}================================================================${COLOR_RESET}"
    echo -e "${COLOR_WHITE} Direktori : ${COLOR_YELLOW}$SCRIPT_DIR${COLOR_RESET}"
    echo -e "${COLOR_WHITE} Tanggal   : ${COLOR_YELLOW}$(date '+%Y-%m-%d %H:%M:%S')${COLOR_RESET}"
    echo -e "${COLOR_CYAN}----------------------------------------------------------------${COLOR_RESET}"
}

# Fungsi Pembatas
print_divider() {
    echo -e "${COLOR_CYAN}----------------------------------------------------------------${COLOR_RESET}"
}

# Fungsi Jeda
pause_screen() {
    echo ""
    echo -e "${COLOR_YELLOW}Tekan [ENTER] untuk kembali ke menu utama...${COLOR_RESET}"
    read -r
}

# Periksa apakah Node.js dan file manage_tenant.js tersedia
check_prerequisites() {
    if ! command -v node &> /dev/null; then
        echo -e "${COLOR_RED}❌ Error: Node.js tidak ditemukan pada sistem ini.${COLOR_RESET}"
        echo -e "Silakan install Node.js terlebih dahulu (misal: sudo apt install nodejs)."
        exit 1
    fi

    if [ ! -f "manage_tenant.js" ]; then
        echo -e "${COLOR_RED}❌ Error: File 'manage_tenant.js' tidak ditemukan di $SCRIPT_DIR.${COLOR_RESET}"
        exit 1
    fi
}

check_prerequisites

# Loop Menu Interaktif
while true; do
    print_header
    echo -e "${COLOR_BOLD}${COLOR_YELLOW}PILIH MENU OPERASI TENANT:${COLOR_RESET}"
    echo -e " ${COLOR_GREEN}1.${COLOR_RESET} 📋 Lihat Daftar Semua Tenant (List Tenants)"
    echo -e " ${COLOR_GREEN}2.${COLOR_RESET} ➕ Tambah Tenant Baru (Setup DB, Schema & Akun Superadmin)"
    echo -e " ${COLOR_GREEN}3.${COLOR_RESET} 🔑 Reset Password Tenant (Ganti Password Superadmin)"
    echo -e " ${COLOR_GREEN}4.${COLOR_RESET} 🚫 Nonaktifkan Tenant (Disable / Suspend Akses Login)"
    echo -e " ${COLOR_GREEN}5.${COLOR_RESET} ✅ Aktifkan Kembali Tenant (Enable / Restore Akses Login)"
    echo -e " ${COLOR_GREEN}6.${COLOR_RESET} 🧪 Ubah Mode Demo (Toggle Status Mode Demo / Produksi)"
    echo -e " ${COLOR_GREEN}7.${COLOR_RESET} 📊 Lihat Statistik & Detail Tenant (Pelanggan, Router, Tagihan)"
    echo -e " ${COLOR_GREEN}8.${COLOR_RESET} 💾 Backup Database Tenant (Export SQL Dump)"
    echo -e " ${COLOR_RED}9.${COLOR_RESET} 🗑️  Hapus Tenant & Databasenya (Permanent Delete)"
    echo -e " ${COLOR_WHITE}0.${COLOR_RESET} 🚪 Keluar"
    print_divider

    read -rp "👉 Masukkan pilihan Anda [0-9]: " CHOICE
    echo ""

    case "$CHOICE" in
        1)
            # 1. LIHAT SEMUA TENANT
            echo -e "${COLOR_CYAN}${COLOR_BOLD}=== [1] DAFTAR SEMUA TENANT TERDAFTAR ===${COLOR_RESET}"
            node manage_tenant.js list
            pause_screen
            ;;

        2)
            # 2. TAMBAH TENANT BARU
            echo -e "${COLOR_GREEN}${COLOR_BOLD}=== [2] SETUP TENANT BARU ===${COLOR_RESET}"
            echo -e "${COLOR_WHITE}Silakan lengkapi formulir pendaftaran tenant berikut:${COLOR_RESET}"
            echo ""

            # Input Database Name
            while true; do
                read -rp "1. Nama Database Tenant (contoh: akbar_media_client2): " DB_NAME
                # Bersihkan spasi
                DB_NAME="$(echo "$DB_NAME" | tr -d ' ')"
                if [ -z "$DB_NAME" ]; then
                    echo -e "${COLOR_RED}   Nama database tidak boleh kosong!${COLOR_RESET}"
                elif [[ ! "$DB_NAME" =~ ^[a-zA-Z0-9_]+$ ]]; then
                    echo -e "${COLOR_RED}   Nama database hanya boleh berisi huruf, angka, dan underscore (_)!${COLOR_RESET}"
                else
                    break
                fi
            done

            # Input Nama Admin
            while true; do
                read -rp "2. Nama Lengkap Admin / Tenant (contoh: ISP Maju Bersama): " ADMIN_NAME
                if [ -z "$ADMIN_NAME" ]; then
                    echo -e "${COLOR_RED}   Nama admin tidak boleh kosong!${COLOR_RESET}"
                else
                    break
                fi
            done

            # Input Username
            while true; do
                read -rp "3. Username Login Superadmin (contoh: admin_maju): " USERNAME
                USERNAME="$(echo "$USERNAME" | tr -d ' ')"
                if [ -z "$USERNAME" ]; then
                    echo -e "${COLOR_RED}   Username tidak boleh kosong!${COLOR_RESET}"
                else
                    break
                fi
            done

            # Input Password
            while true; do
                read -rsp "4. Password Login Superadmin: " PASSWORD
                echo ""
                if [ -z "$PASSWORD" ]; then
                    echo -e "${COLOR_RED}   Password tidak boleh kosong!${COLOR_RESET}"
                else
                    read -rsp "   Konfirmasi Password: " PASSWORD_CONFIRM
                    echo ""
                    if [ "$PASSWORD" != "$PASSWORD_CONFIRM" ]; then
                        echo -e "${COLOR_RED}   Password tidak cocok! Silakan ulangi.${COLOR_RESET}"
                    else
                        break
                    fi
                fi
            done

            # Demo Mode
            read -rp "5. Apakah ini tenant versi DEMO? [y/N]: " IS_DEMO_INPUT
            IS_DEMO="0"
            if [[ "$IS_DEMO_INPUT" =~ ^[yY]$ ]]; then
                IS_DEMO="1"
            fi

            echo ""
            echo -e "${COLOR_YELLOW}Konfirmasi Data Tenant:${COLOR_RESET}"
            echo -e " - Database  : ${COLOR_CYAN}$DB_NAME${COLOR_RESET}"
            echo -e " - Nama      : ${COLOR_CYAN}$ADMIN_NAME${COLOR_RESET}"
            echo -e " - Username  : ${COLOR_CYAN}$USERNAME${COLOR_RESET}"
            echo -e " - Mode Demo : ${COLOR_CYAN}$([ "$IS_DEMO" = "1" ] && echo "YA (Demo)" || echo "TIDAK (Produksi)")${COLOR_RESET}"
            echo ""
            read -rp "Lanjutkan proses pembuatan tenant? [Y/n]: " CONFIRM_ADD
            if [[ "$CONFIRM_ADD" =~ ^[nN]$ ]]; then
                echo -e "${COLOR_YELLOW}Pembuatan tenant dibatalkan.${COLOR_RESET}"
            else
                echo -e "\n${COLOR_CYAN}Sedang memproses...${COLOR_RESET}"
                node manage_tenant.js add "$DB_NAME" "$ADMIN_NAME" "$USERNAME" "$PASSWORD" "$IS_DEMO"
            fi
            pause_screen
            ;;

        3)
            # 3. RESET PASSWORD TENANT
            echo -e "${COLOR_YELLOW}${COLOR_BOLD}=== [3] RESET PASSWORD SUPERADMIN TENANT ===${COLOR_RESET}"
            node manage_tenant.js list
            read -rp "Masukkan Username tenant yang ingin direset passwordnya (kosongkan untuk batal): " USERNAME
            USERNAME="$(echo "$USERNAME" | tr -d ' ')"
            if [ -n "$USERNAME" ]; then
                read -rsp "Masukkan Password Baru: " NEW_PASS
                echo ""
                read -rsp "Konfirmasi Password Baru: " NEW_PASS_CONFIRM
                echo ""
                if [ -z "$NEW_PASS" ]; then
                    echo -e "${COLOR_RED}Password tidak boleh kosong! Batal.${COLOR_RESET}"
                elif [ "$NEW_PASS" != "$NEW_PASS_CONFIRM" ]; then
                    echo -e "${COLOR_RED}Password konfirmasi tidak cocok! Batal.${COLOR_RESET}"
                else
                    node manage_tenant.js reset-password "$USERNAME" "$NEW_PASS"
                fi
            else
                echo -e "${COLOR_YELLOW}Operasi dibatalkan.${COLOR_RESET}"
            fi
            pause_screen
            ;;

        4)
            # 4. DISABLE TENANT
            echo -e "${COLOR_RED}${COLOR_BOLD}=== [4] NONAKTIFKAN TENANT (DISABLE / BLOKIR LOGIN) ===${COLOR_RESET}"
            node manage_tenant.js list
            read -rp "Masukkan Username tenant yang ingin di-nonaktifkan (kosongkan untuk batal): " USERNAME
            USERNAME="$(echo "$USERNAME" | tr -d ' ')"
            if [ -n "$USERNAME" ]; then
                read -rp "Yakin ingin menonaktifkan login untuk tenant '$USERNAME'? [y/N]: " CONFIRM_DISABLE
                if [[ "$CONFIRM_DISABLE" =~ ^[yY]$ ]]; then
                    node manage_tenant.js disable "$USERNAME"
                else
                    echo -e "${COLOR_YELLOW}Operasi dibatalkan.${COLOR_RESET}"
                fi
            else
                echo -e "${COLOR_YELLOW}Operasi dibatalkan.${COLOR_RESET}"
            fi
            pause_screen
            ;;

        5)
            # 5. ENABLE TENANT
            echo -e "${COLOR_GREEN}${COLOR_BOLD}=== [5] AKTIFKAN KEMBALI TENANT (ENABLE LOGIN) ===${COLOR_RESET}"
            node manage_tenant.js list
            read -rp "Masukkan Username tenant yang ingin diaktifkan kembali (kosongkan untuk batal): " USERNAME
            USERNAME="$(echo "$USERNAME" | tr -d ' ')"
            if [ -n "$USERNAME" ]; then
                node manage_tenant.js enable "$USERNAME"
            else
                echo -e "${COLOR_YELLOW}Operasi dibatalkan.${COLOR_RESET}"
            fi
            pause_screen
            ;;

        6)
            # 6. TOGGLE DEMO MODE
            echo -e "${COLOR_MAGENTA}${COLOR_BOLD}=== [6] UBAH STATUS MODE DEMO TENANT ===${COLOR_RESET}"
            node manage_tenant.js list
            read -rp "Masukkan Username tenant yang ingin diubah mode demonya (kosongkan untuk batal): " USERNAME
            USERNAME="$(echo "$USERNAME" | tr -d ' ')"
            if [ -n "$USERNAME" ]; then
                node manage_tenant.js toggle-demo "$USERNAME"
            else
                echo -e "${COLOR_YELLOW}Operasi dibatalkan.${COLOR_RESET}"
            fi
            pause_screen
            ;;

        7)
            # 7. STATISTIK TENANT
            echo -e "${COLOR_CYAN}${COLOR_BOLD}=== [7] STATISTIK & DETAIL TENANT ===${COLOR_RESET}"
            node manage_tenant.js list
            read -rp "Masukkan Username tenant yang ingin dilihat statistiknya: " USERNAME
            USERNAME="$(echo "$USERNAME" | tr -d ' ')"
            if [ -n "$USERNAME" ]; then
                node manage_tenant.js stats "$USERNAME"
            else
                echo -e "${COLOR_YELLOW}Operasi dibatalkan.${COLOR_RESET}"
            fi
            pause_screen
            ;;

        8)
            # 8. BACKUP DATABASE TENANT
            echo -e "${COLOR_BLUE}${COLOR_BOLD}=== [8] BACKUP DATABASE TENANT ===${COLOR_RESET}"
            node manage_tenant.js list
            read -rp "Masukkan Nama Database tenant yang ingin dibackup (contoh: akbar_media_client2): " TARGET_DB
            TARGET_DB="$(echo "$TARGET_DB" | tr -d ' ')"
            if [ -n "$TARGET_DB" ]; then
                BACKUP_DIR="$SCRIPT_DIR/backups"
                mkdir -p "$BACKUP_DIR"
                TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
                BACKUP_FILE="$BACKUP_DIR/${TARGET_DB}_backup_${TIMESTAMP}.sql"

                # Ambil kredensial DB dari .env jika ada
                DB_USER="akbar"
                DB_PASS="08Delapan"
                DB_HOST="localhost"
                if [ -f ".env" ]; then
                    ENV_USER=$(grep -E "^DB_USER=" .env | cut -d '=' -f2 | tr -d '"' | tr -d "'")
                    ENV_PASS=$(grep -E "^DB_PASSWORD=" .env | cut -d '=' -f2 | tr -d '"' | tr -d "'")
                    ENV_HOST=$(grep -E "^DB_HOST=" .env | cut -d '=' -f2 | tr -d '"' | tr -d "'")
                    [ -n "$ENV_USER" ] && DB_USER="$ENV_USER"
                    [ -n "$ENV_PASS" ] && DB_PASS="$ENV_PASS"
                    [ -n "$ENV_HOST" ] && DB_HOST="$ENV_HOST"
                fi

                echo -e "${COLOR_CYAN}Membuat backup untuk database '$TARGET_DB'...${COLOR_RESET}"
                if command -v mysqldump &> /dev/null; then
                    mysqldump -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASS" "$TARGET_DB" > "$BACKUP_FILE" 2>/dev/null
                    if [ $? -eq 0 ] && [ -s "$BACKUP_FILE" ]; then
                        echo -e "${COLOR_GREEN}✅ SUKSES: Backup berhasil disimpan di:${COLOR_RESET}"
                        echo -e "   ${COLOR_YELLOW}$BACKUP_FILE${COLOR_RESET}"
                        echo -e "   Ukuran file: $(du -h "$BACKUP_FILE" | cut -f1)"
                    else
                        echo -e "${COLOR_RED}❌ Gagal melakukan backup dengan mysqldump. Pastikan nama database benar dan kredensial valid.${COLOR_RESET}"
                        rm -f "$BACKUP_FILE"
                    fi
                else
                    echo -e "${COLOR_YELLOW}⚠️ Perintah 'mysqldump' tidak ditemukan di VPS. Silakan install paket mysql-client.${COLOR_RESET}"
                fi
            else
                echo -e "${COLOR_YELLOW}Operasi dibatalkan.${COLOR_RESET}"
            fi
            pause_screen
            ;;

        9)
            # 9. HAPUS TENANT
            echo -e "${COLOR_RED}${COLOR_BOLD}=== [9] HAPUS TENANT & DATABASE PERMANEN ===${COLOR_RESET}"
            echo -e "${COLOR_RED}⚠️ PERINGATAN: Tindakan ini akan menghapus database tenant beserta seluruh data pelanggan, tagihan, transaksi, dan akun superadmin!${COLOR_RESET}"
            echo ""
            node manage_tenant.js list
            read -rp "Masukkan Username dari tenant yang ingin DIHAPUS (kosongkan untuk batal): " USERNAME
            USERNAME="$(echo "$USERNAME" | tr -d ' ')"
            if [ -n "$USERNAME" ]; then
                echo -e "${COLOR_RED}${COLOR_BOLD}KONFIRMASI PENGHAPUSAN:${COLOR_RESET}"
                read -rp "Ketik 'HAPUS' (huruf besar) untuk melanjutkan penghapusan: " CONFIRM_DELETE
                if [ "$CONFIRM_DELETE" = "HAPUS" ]; then
                    echo -e "\n${COLOR_CYAN}Sedang menghapus tenant...${COLOR_RESET}"
                    node manage_tenant.js delete "$USERNAME"
                else
                    echo -e "${COLOR_YELLOW}Penghapusan dibatalkan (kata kunci konfirmasi tidak cocok).${COLOR_RESET}"
                fi
            else
                echo -e "${COLOR_YELLOW}Operasi dibatalkan.${COLOR_RESET}"
            fi
            pause_screen
            ;;

        0)
            # 0. KELUAR
            echo -e "${COLOR_GREEN}Terima kasih. Keluar dari panel manajemen tenant.${COLOR_RESET}"
            exit 0
            ;;

        *)
            echo -e "${COLOR_RED}Pilihan tidak valid! Silakan masukkan angka 0 sampai 9.${COLOR_RESET}"
            pause_screen
            ;;
    esac
done
