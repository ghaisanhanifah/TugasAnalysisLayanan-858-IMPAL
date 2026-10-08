import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Simulasi layanan USSD *858# - jalur: 1. Transfer Pulsa
 * Alur: dial -> menu utama -> pilih 1 -> input nomor tujuan
 *       -> input nominal -> konfirmasi -> eksekusi -> notifikasi sukses.
 * Implementasi mengikuti PSPEC P1.1 s.d. P1.5.
 */
public class TransferPulsaUSSD {

    // ===== Data store D1: Data Pelanggan (nomor -> saldo pulsa) =====
    static class DataPelanggan {
        private final Map<String, Long> saldo = new HashMap<>();

        void tambah(String nomor, long pulsa) { saldo.put(nomor, pulsa); }
        boolean ada(String nomor) { return saldo.containsKey(nomor); }
        long getSaldo(String nomor) { return saldo.get(nomor); }
        void kurangi(String nomor, long n) { saldo.put(nomor, saldo.get(nomor) - n); }
        void tambahSaldo(String nomor, long n) { saldo.put(nomor, saldo.get(nomor) + n); }
    }

    // ===== Data store D2: Log Transaksi =====
    static class LogTransaksi {
        private int counter = 0;

        String catat(String asal, String tujuan, long nominal, long biaya) {
            counter++;
            return String.format("TP%05d", counter);
        }
    }

    // ===== Helper Format Nominal Rupiah =====
    static String formatRupiah(long nominal) {
        return "Rp " + String.format("%,d", nominal).replace(',', '.');
    }

    // ===== Helper Kotak Dialog USSD Ponsel =====
    static void tampilkanPopup(String... baris) {
        int contentWidth = 52;
        System.out.println("+------------------------------------------------------+");
        for (String b : baris) {
            if (b.isEmpty()) {
                System.out.println("| " + String.format("%-" + contentWidth + "s", "") + " |");
            } else {
                int start = 0;
                while (start < b.length()) {
                    int end = Math.min(start + contentWidth, b.length());
                    String sub = b.substring(start, end);
                    System.out.println("| " + String.format("%-" + contentWidth + "s", sub) + " |");
                    start = end;
                }
            }
        }
        System.out.println("+------------------------------------------------------+");
    }

    // ===== P1.2 Validasi nomor tujuan =====
    static String normalisasiNomor(String input) {
        // terima 08xxxx atau 628xxxx; simpan format 628xxxx
        if (input == null) return null;
        if (!input.matches("\\d+")) return null;
        String n = input;
        if (n.startsWith("08")) n = "62" + n.substring(1);
        if (!n.startsWith("628")) return null;
        int panjang = n.length();
        if (panjang < 10 || panjang > 15) return null;
        return n;
    }

    // ===== P1.3 Validasi nominal =====
    static final long BIAYA_ADMIN = 1000;
    static final long MIN_TRANSFER = 5000;
    static final long MIN_SISA = 5000;

    static String validasiNominal(long nominal, long saldoPengirim) {
        if (nominal < MIN_TRANSFER) return "Nominal minimal Rp" + MIN_TRANSFER;
        if (nominal % 1000 != 0) return "Nominal harus kelipatan Rp1000";
        if (saldoPengirim - nominal - BIAYA_ADMIN < MIN_SISA) return "Pulsa Anda tidak mencukupi";
        return null; // valid
    }

    // ===== P1.5 Eksekusi transfer =====
    static String eksekusi(DataPelanggan db, LogTransaksi log,
                           String asal, String tujuan, long nominal) {
        db.kurangi(asal, nominal + BIAYA_ADMIN);
        db.tambahSaldo(tujuan, nominal);
        return log.catat(asal, tujuan, nominal, BIAYA_ADMIN);
    }

    // ===== Proses utama USSD =====
    static void jalankan(Scanner in, DataPelanggan db, LogTransaksi log, String nomorSaya) {
        System.out.print("Panggilan (Dial): ");
        String kode = in.nextLine().trim();
        if (kode.isEmpty()) {
            kode = "*858#";
            System.out.println(kode);
        }
        if (!kode.equals("*858#")) {
            System.out.println();
            tampilkanPopup("Sambungan bermasalah atau kode MMI tidak valid.");
            return;
        }
        System.out.println("Menghubungkan...\n");

        // P1.1 Tampilkan menu utama (sesuai dialog promo asli Telkomsel *858#)
        tampilkanPopup(
            "Mau iPhone 18 Plus Max Burgundy dr Zaskia Melmel?",
            "Hub *500*117#",
            "1. Transfer Pulsa",
            "2. Minta Pulsa",
            "3. Auto TP",
            "4. Delete Auto TP",
            "5. List Auto TP",
            "6. Cek Kupon Undian TP"
        );
        System.out.print("Pilih: ");
        String pilih = in.nextLine().trim();
        if (!pilih.equals("1")) {
            System.out.println();
            tampilkanPopup("Layanan " + pilih + " sedang tidak tersedia.");
            return;
        }

        // P1.2 Input & validasi nomor tujuan
        System.out.println();
        tampilkanPopup(
            "Silahkan masukkan nomor tujuan Transfer Pulsa :",
            "(contoh: 08xxxx atau 628xxxx)"
        );
        System.out.print("Nomor Tujuan: ");
        String tujuan = normalisasiNomor(in.nextLine().trim());
        if (tujuan == null) {
            System.out.println();
            tampilkanPopup("Format nomor tujuan salah.");
            return;
        }
        if (tujuan.equals(nomorSaya)) {
            System.out.println();
            tampilkanPopup("Tidak dapat transfer ke nomor sendiri.");
            return;
        }
        if (!db.ada(tujuan)) {
            System.out.println();
            tampilkanPopup("Nomor tujuan tidak terdaftar.");
            return;
        }

        // P1.3 Input & validasi nominal
        System.out.println();
        tampilkanPopup(
            "Silahkan masukkan jumlah pulsa yang akan",
            "ditransfer : (min 5000)"
        );
        System.out.print("Nominal (Rp): ");
        long nominal;
        try {
            nominal = Long.parseLong(in.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println();
            tampilkanPopup("Nominal harus berupa angka.");
            return;
        }
        String err = validasiNominal(nominal, db.getSaldo(nomorSaya));
        if (err != null) {
            System.out.println();
            tampilkanPopup(err);
            return;
        }

        // P1.4 Konfirmasi
        System.out.println();
        tampilkanPopup(
            "Hati2 penipuan. Anda akan Transfer Pulsa",
            formatRupiah(nominal) + " ke " + tujuan + " (biaya " + formatRupiah(BIAYA_ADMIN) + ")?",
            "1. Ya",
            "2. Batal"
        );
        System.out.print("Pilih: ");
        if (!in.nextLine().trim().equals("1")) {
            System.out.println();
            tampilkanPopup("Transaksi dibatalkan.");
            return;
        }

        // P1.5 Eksekusi + notifikasi
        eksekusi(db, log, nomorSaya, tujuan, nominal);
        System.out.println();
        tampilkanPopup(
            "Terima kasih, permintaan Anda sedang diproses.",
            "",
            "Transfer pulsa " + formatRupiah(nominal) + " ke " + tujuan,
            "Sisa pulsa Anda: " + formatRupiah(db.getSaldo(nomorSaya))
        );
    }

    public static void main(String[] args) {
        DataPelanggan db = new DataPelanggan();
        db.tambah("6281234567890", 50000);   // pengirim (pengguna)
        db.tambah("6285711112222", 10000);   // contoh penerima
        LogTransaksi log = new LogTransaksi();

        try (Scanner in = new Scanner(System.in)) {
            jalankan(in, db, log, "6281234567890");
        }
    }
}
