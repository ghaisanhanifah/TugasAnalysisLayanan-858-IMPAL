import java.util.Arrays;
import java.util.Scanner;

public class Segitiga {

    public static void tampilkanHeader() {
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (Exception e) {
            System.out.println("\n");
        }

        System.out.println();
        System.out.println("\t=======================================================");
        System.out.println("\t|                                                     |");
        System.out.println("\t|         PROGRAM PENENTU JENIS SEGITIGA (INT)        |");
        System.out.println("\t|                                                     |");
        System.out.println("\t=======================================================");
        System.out.println("\t|  Mendeteksi: Sama Sisi, Sama Kaki, Siku-Siku, dll   |");
        System.out.println("\t=======================================================\n");
    }

    public static int inputAngkaValid(Scanner in, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = in.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("\t[!] ERROR: Input tidak valid. Harap masukkan angka bulat!");
            }
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        char ulangi = 'Y';

        while (ulangi == 'y' || ulangi == 'Y') {
            tampilkanHeader();

            int a = inputAngkaValid(in, "\t>> Masukkan panjang sisi pertama : ");
            int b = inputAngkaValid(in, "\t>> Masukkan panjang sisi kedua   : ");
            int c = inputAngkaValid(in, "\t>> Masukkan panjang sisi ketiga  : ");

            System.out.println("\n\t---------------------------------------------------");
            System.out.println("\tHASIL ANALISIS:");
            System.out.println("\tSisi yang diinput : " + a + ", " + b + ", " + c);
            System.out.print("\tKesimpulan        : ");

            if (a <= 0 || b <= 0 || c <= 0) {
                System.out.println("TIDAK BISA MEMBANGUN SEGITIGA");
                System.out.println("\tAlasan            : Panjang sisi tidak boleh <= 0.");
            } else {
                int[] sides = {a, b, c};
                Arrays.sort(sides);

                int x = sides[0]; // Sisi kecil
                int y = sides[1]; // Sisi tengah
                int z = sides[2]; // Sisi terbesar

                if (z >= x + y) {
                    System.out.println("TIDAK BISA MEMBANGUN SEGITIGA");
                    System.out.println("\tAlasan            : Sisi terpanjang (" + z + ") lebih besar atau");
                    System.out.println("\t                    sama dengan jumlah dua sisi lainnya (" + (x + y) + ").");
                } else if (x == y && y == z) {
                    System.out.println("SEGITIGA SAMA SISI (EQUILATERAL)");
                } else if (x == y || y == z) {
                    System.out.println("SEGITIGA SAMA KAKI (ISOSCELES)");
                } else if ((long) z * z == ((long) x * x) + ((long) y * y)) {
                    System.out.println("SEGITIGA SIKU-SIKU (RIGHT TRIANGLE)");
                } else {
                    System.out.println("SEGITIGA BEBAS (FREE TRIANGLE)");
                }
            }
            System.out.println("\t---------------------------------------------------\n");
            System.out.print("\tIngin mencoba angka lain? (y/n): ");
            String jawab = in.nextLine().trim();
            ulangi = jawab.isEmpty() ? 'n' : jawab.charAt(0);
        }
        System.out.println("\n\tTerima kasih telah menggunakan program ini!\n");
        in.close();
    }
}
