import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;
import java.util.Scanner;

/**
 * PrimalityAllTests.java
 *
 * Contiene en una sola clase múltiples pruebas de primalidad:
 * - Métodos básicos (tipo división / sqrt) — métodos 1..5 (adaptados).
 * - Fermat, Miller-Rabin, Solovay-Strassen, Lehmann (probabilísticos).
 * - Baillie-PSW (simplificado), AKS (simplificado: usa isProbablePrime).
 * - Wilson (determinístico, muy lento), Lucas-Lehmer (para Mersenne).
 *
 * Comentarios y advertencias están incluidos en cada método.
 *
 * 
 */
public class PrimalityAllTests {

    // Constantes y RNG
    private static final BigInteger TWO = BigInteger.valueOf(2);
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Random rand = new Random();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            // Menú
            System.out.println("\n=== MENÚ PRUEBAS DE PRIMALIDAD ===");
            System.out.println(" 1  - Método simple 1 (for i=2..n-1) (int)");
            System.out.println(" 2  - Método simple 2 (hasta n/2) (int)");
            System.out.println(" 3  - Método simple 3 (break) (int)");
            System.out.println(" 4  - Método simple 4 (hasta sqrt(n)) (int)");
            System.out.println(" 5  - Método simple 5 (i*i <= n) (int)");
            System.out.println(" 6  - Fermat (probabilístico)");
            System.out.println(" 7  - Miller-Rabin (probabilístico)");
            System.out.println(" 8  - Solovay-Strassen (probabilístico)");
            System.out.println(" 9  - Lehmann (probabilístico)");
            System.out.println("10  - Baillie-PSW (simplificado)");
            System.out.println("11  - AKS (usando isProbablePrime de Java - simplificación)");
            System.out.println("12  - Wilson (determinístico, muy lento para n grandes)");
            System.out.println("13  - Lucas-Lehmer (solo para Mersenne: 2^p - 1)");
            System.out.println("14  - Ejecutar TODOS (comparar tiempos) [cuidado: incluye pruebas lentas]");
            System.out.println(" 0  - Salir");
            System.out.print("Opción: ");

            int opcion = sc.nextInt();
            if (opcion == 0) break;

            try {
                if (opcion >= 1 && opcion <= 5) {
                    // Para métodos simples pedimos int (mantengo compatibilidad con tus métodos originales)
                    System.out.print("Ingrese entero (32-bit): ");
                    int n = sc.nextInt();
                    long start = System.nanoTime();
                    boolean res = false;
                    switch (opcion) {
                        case 1: res = determinarNumeroPrimo1(n); break;
                        case 2: res = determinarNumeroPrimo2(n); break;
                        case 3: res = determinarNumeroPrimo3(n); break;
                        case 4: res = determinarNumeroPrimo4(n); break;
                        case 5: res = determinarNumeroPrimo5(n); break;
                    }
                    long end = System.nanoTime();
                    printResult(n + "", res, end - start);
                } else if (opcion == 13) {
                    // Lucas-Lehmer requiere p (entero)
                    System.out.print("Ingrese p (entero) para probar Mersenne 2^p - 1: ");
                    int p = sc.nextInt();
                    long start = System.nanoTime();
                    boolean res = lucasLehmer(p);
                    long end = System.nanoTime();
                    printResult("Mersenne 2^" + p + " - 1", res, end - start);
                } else if (opcion == 14) {
                    // Ejecutar todos (cuidado con Wilson, AKS si n grande)
                    System.out.print("Ingrese número (se usará BigInteger): ");
                    BigInteger n = sc.nextBigInteger();
                    runAllTests(n);
                } else {
                    // Resto de pruebas que aceptan BigInteger
                    System.out.print("Ingrese número (BigInteger): ");
                    BigInteger n = sc.nextBigInteger();
                    System.out.print("¿Iteraciones para tests probabilísticos? (ej. 5) : ");
                    int k = sc.nextInt();

                    long start = System.nanoTime();
                    boolean res = false;
                    switch (opcion) {
                        case 6: res = fermat(n, k); break;
                        case 7: res = millerRabin(n, k); break;
                        case 8: res = solovayStrassen(n, k); break;
                        case 9: res = lehmann(n, k); break;
                        case 10: res = bailliePSW(n); break;
                        case 11: res = aks(n); break;
                        case 12: res = wilson(n); break;
                        default:
                            System.out.println("Opción inválida.");
                            continue;
                    }
                    long end = System.nanoTime();
                    printResult(n.toString(), res, end - start);
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                sc.nextLine(); // limpiar buffer
            }
        } // while
        sc.close();
        System.out.println("Programa finalizado.");
    }

    // ============================
    // Helpers
    // ============================
    private static void printResult(String nStr, boolean isPrime, long nano) {
        double ms = nano / 1_000_000.0;
        System.out.println("Número: " + nStr);
        System.out.println("Resultado: " + (isPrime ? "PRIMO (o probablemente primo)" : "COMPUESTO"));
        System.out.println("Tiempo: " + nano + " ns (" + ms + " ms)");
    }

    // Ejecuta todos los tests y muestra tabla de tiempos (salvo pruebas que se quieran evitar)
    private static void runAllTests(BigInteger n) {
        System.out.println("\n=== Ejecutando todos los tests (cuidado: algunos pueden ser lentos) ===");
        // 1..5 no están aquí porque son métodos para int; los omitimos en "todos" salvo conversión cuando posible
        // 6 Fermat
        long t0 = System.nanoTime();
        boolean f = fermat(n, 5);
        long t1 = System.nanoTime();
        // 7 Miller-Rabin
        boolean mr;
        long t2;
        {
            long s = System.nanoTime();
            mr = millerRabin(n, 5);
            t2 = System.nanoTime();
            System.out.printf("Miller-Rabin: %s time=%d ns%n", mr ? "PRIMO?" : "COMPUESTO", t2 - s);
        }
        // 8 Solovay-Strassen
        long sss = System.nanoTime();
        boolean ss = solovayStrassen(n, 5);
        long eSS = System.nanoTime();
        // 9 Lehmann
        long sL = System.nanoTime();
        boolean lhm = lehmann(n, 5);
        long eL = System.nanoTime();
        // 10 Baillie-PSW (simplificado)
        long sB = System.nanoTime();
        boolean bpsw = bailliePSW(n);
        long eB = System.nanoTime();
        // 11 AKS (simplificado)
        long sA = System.nanoTime();
        boolean aksRes = aks(n);
        long eA = System.nanoTime();
        // 12 Wilson (aviso si n grande)
        long sW = System.nanoTime();
        boolean wilsonRes = wilson(n);
        long eW = System.nanoTime();

        System.out.println("\n--- Resultados resumidos ---");
        printResult("Fermat", f, t1 - t0);
        printResult("Miller-Rabin", mr, t2 - t1);
        printResult("Solovay-Strassen", ss, eSS - sss);
        printResult("Lehmann", lhm, eL - sL);
        printResult("Baillie-PSW(simpl)", bpsw, eB - sB);
        printResult("AKS(simpl)", aksRes, eA - sA);
        printResult("Wilson", wilsonRes, eW - sW);
    }

    // ============================
    // Métodos originales (1..5) adaptados a int (copiados y comentados)
    // ============================

    /** Método 1: recorre i=2..n-1 -> O(n) tiempo */
    public static boolean determinarNumeroPrimo1(int numero) {
        if (numero < 2) return false;
        int resultado = 0;
        for (int i = 2; i < numero; i++) {
            if (numero % i == 0) {
                resultado = 1;
            }
        }
        return resultado == 0;
    }

    /** Método 2: recorre hasta n/2 con bandera -> O(n) tiempo (≈ n/2) */
    public static boolean determinarNumeroPrimo2(int numero) {
        if (numero < 2) return false;
        boolean centi = true;
        int i;
        for (i = 2; i <= numero / 2 && centi; i++) {
            if (numero % i == 0) {
                centi = false;
            }
        }
        return centi;
    }

    /** Método 3: rompe cuando encuentra divisor -> O(n) en peor caso */
    public static boolean determinarNumeroPrimo3(int numero) {
        if (numero < 2) return false;
        int i;
        for (i = 2; i <= numero / 2; i++) {
            if (numero % i == 0) {
                break;
            }
        }
        return (numero / 2) < i;
    }

    /** Método 4: prueba hasta sqrt(n) -> O(√n) */
    public static boolean determinarNumeroPrimo4(int numero) {
        if (numero < 2) return false;
        for (int i = 2; i <= (int) Math.sqrt(numero); i++) {
            if (numero % i == 0) {
                return false;
            }
        }
        return true;
    }

    /** Método 5: prueba i*i <= n -> O(√n), evita sqrt() */
    public static boolean determinarNumeroPrimo5(int numero) {
        if (numero < 2) return false;
        for (int i = 2; i * i <= numero; i++) {
            if (numero % i == 0) {
                return false;
            }
        }
        return true;
    }

    // ============================
    // Pruebas probabilísticas y determinísticas (BigInteger versions)
    // ============================

    /**
     * Fermat's primality test (probabilístico).
     * Rápido, pero vulnerable a números de Carmichael.
     * Complejidad: O(k · log^3 n) por iteración (modPow cost).
     */
    public static boolean fermat(BigInteger n, int iterations) {
        if (n.compareTo(TWO) < 0) return false;
        if (n.equals(TWO)) return true;
        if (n.mod(TWO).equals(BigInteger.ZERO)) return false;

        for (int i = 0; i < iterations; i++) {
            BigInteger a = uniformRandom(TWO, n.subtract(BigInteger.ONE));
            // a^(n-1) mod n should be 1 for prime n (Fermat's little theorem)
            if (!a.modPow(n.subtract(BigInteger.ONE), n).equals(BigInteger.ONE)) {
                return false; // compuesto
            }
        }
        return true; // probablemente primo
    }

    /**
     * Miller-Rabin (probabilístico, ampliamente usado).
     * Con suficientes iteraciones es muy fiable.
     */
    public static boolean millerRabin(BigInteger n, int iterations) {
        if (n.compareTo(TWO) < 0) return false;
        if (n.equals(TWO) || n.equals(BigInteger.valueOf(3))) return true;
        if (n.mod(TWO).equals(BigInteger.ZERO)) return false;

        // escribe n-1 = 2^s * d con d impar
        BigInteger d = n.subtract(BigInteger.ONE);
        int s = 0;
        while (d.mod(TWO).equals(BigInteger.ZERO)) {
            d = d.divide(TWO);
            s++;
        }

        for (int i = 0; i < iterations; i++) {
            BigInteger a = uniformRandom(TWO, n.subtract(BigInteger.ONE));
            BigInteger x = a.modPow(d, n);
            if (x.equals(BigInteger.ONE) || x.equals(n.subtract(BigInteger.ONE))) continue;
            boolean composite = true;
            for (int r = 0; r < s - 1; r++) {
                x = x.modPow(TWO, n);
                if (x.equals(n.subtract(BigInteger.ONE))) {
                    composite = false;
                    break;
                }
            }
            if (composite) return false;
        }
        return true; // probablemente primo
    }

    /**
     * Solovay-Strassen (probabilístico).
     * Usa símbolo de Jacobi; más fuerte que Fermat en algunos sentidos.
     */
    public static boolean solovayStrassen(BigInteger n, int iterations) {
        if (n.compareTo(TWO) < 0) return false;
        if (n.equals(TWO)) return true;
        if (n.mod(TWO).equals(BigInteger.ZERO)) return false;

        for (int i = 0; i < iterations; i++) {
            BigInteger a = uniformRandom(TWO, n.subtract(BigInteger.ONE));
            BigInteger exp = n.subtract(BigInteger.ONE).divide(BigInteger.valueOf(2));
            BigInteger modPow = a.modPow(exp, n); // a^{(n-1)/2} mod n
            int jac = jacobi(a, n); // -1, 0, or 1
            BigInteger jacMod = BigInteger.valueOf(jac == -1 ? n.subtract(BigInteger.ONE).longValue() : jac);
            // if jacobi is 0 -> gcd(a,n) != 1 -> composite
            if (jac == 0) return false;
            if (!modPow.equals(jacMod)) return false;
        }
        return true;
    }

    /**
     * Lehmann (probabilístico, parecido a Fermat/solovay).
     * Calcula a^{(n-1)/2} mod n y espera ±1.
     */
    public static boolean lehmann(BigInteger n, int iterations) {
        if (n.compareTo(TWO) < 0) return false;
        if (n.equals(TWO)) return true;
        if (n.mod(TWO).equals(BigInteger.ZERO)) return false;

        for (int i = 0; i < iterations; i++) {
            BigInteger a = uniformRandom(TWO, n.subtract(BigInteger.ONE));
            BigInteger r = a.modPow(n.subtract(BigInteger.ONE).divide(BigInteger.valueOf(2)), n);
            if (!r.equals(BigInteger.ONE) && !r.equals(n.subtract(BigInteger.ONE))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Baillie-PSW (versión simplificada):
     * - En la práctica la versión completa combina Miller-Rabin con una prueba de Lucas fuerte.
     * - Aquí se realiza Miller-Rabin con algunas bases y además se usa isProbablePrime como "refuerzo".
     * Nota: Implementación completa de Baillie-PSW es más larga; esta simplificación es práctica para experimentar.
     */
    public static boolean bailliePSW(BigInteger n) {
        if (n.compareTo(TWO) < 0) return false;
        if (n.equals(TWO)) return true;
        // Paso 1: Miller-Rabin con base 2 (rápido filtro)
        if (!millerRabin(n, 3)) return false;
        // Paso 2: Usamos isProbablePrime con alta certeza como sustituto de la prueba de Lucas completa
        // (esto no es exactamente la definición matemática de Baillie-PSW pero es práctico)
        return n.isProbablePrime(50);
    }

    /**
     * AKS (versión simplificada):
     * - AKS es un algoritmo determinístico polinómico (complejidad alta práctica).
     * - Implementarlo completamente es largo y raro en uso práctico.
     * - Aquí usamos la función isProbablePrime como "atajo" y documentamos que esto NO es AKS real,
     *   solo una simplificación conveniente para pruebas en este programa.
     */
    public static boolean aks(BigInteger n) {
        // Advertencia: este no es AKS. Implementar AKS real excede el alcance práctico aquí.
        return n.isProbablePrime(50);
    }

    /**
     * Wilson: p primo ⇔ (p-1)! ≡ -1 (mod p)
     * Determinístico pero O(n) multiplicaciones modulo n: impráctico para n grandes.
     * Por seguridad, si n > 20000 (configurable), devolvemos false y advertimos (evita bucles enormes).
     */
    public static boolean wilson(BigInteger n) {
        if (n.compareTo(TWO) < 0) return false;
        if (n.equals(TWO)) return true;
        if (!n.isProbablePrime(5)) {
            // si se detecta compuesto rápido, devuelve false
            // (esto evita calcular factoriales innecesariamente)
            // pero si isProbablePrime dijera probable primo, seguimos.
        }

        BigInteger limit = BigInteger.valueOf(20000); // umbral seguro para evitar bucles enormes
        if (n.compareTo(limit) > 0) {
            System.out.println("Wilson: n demasiado grande para calcular factorial eficientemente (umbral " + limit + ").");
            // Para no bloquear, devolvemos false (o podríamos lanzar excepción / pedir confirmación)
            return false;
        }

        BigInteger fact = BigInteger.ONE;
        for (BigInteger i = BigInteger.valueOf(2); i.compareTo(n) < 0; i = i.add(BigInteger.ONE)) {
            fact = fact.multiply(i).mod(n);
        }
        return fact.add(BigInteger.ONE).mod(n).equals(BigInteger.ZERO);
    }

    /**
     * Lucas-Lehmer: específico para números de Mersenne (2^p - 1).
     * Si p es primo, la prueba verifica si 2^p - 1 es primo.
     * Rápido para p moderados y usado por GIMPS.
     */
    public static boolean lucasLehmer(int p) {
        if (p < 2) return false;
        if (p == 2) return true; // 3 es primo (2^2-1 = 3)
        BigInteger m = BigInteger.valueOf(2).pow(p).subtract(BigInteger.ONE);
        BigInteger s = BigInteger.valueOf(4);
        for (int i = 0; i < p - 2; i++) {
            s = s.multiply(s).subtract(BigInteger.valueOf(2)).mod(m);
        }
        return s.equals(BigInteger.ZERO);
    }

    // ============================
    // Math utilities
    // ============================

    // Uniform random BigInteger in [min, max] inclusive
    private static BigInteger uniformRandom(BigInteger min, BigInteger max) {
        BigInteger range = max.subtract(min).add(BigInteger.ONE); // inclusive
        int bitLength = range.bitLength();
        BigInteger r;
        do {
            r = new BigInteger(bitLength, secureRandom);
        } while (r.compareTo(range) >= 0);
        return r.add(min);
    }

    /**
     * Jacobi symbol (a/n) for BigInteger n (odd).
     * Returns -1, 0, or 1.
     * Implementation via repeated quadratic reciprocity and factors of 2.
     */
    public static int jacobi(BigInteger a0, BigInteger n0) {
        if (n0.signum() <= 0 || n0.mod(TWO).equals(BigInteger.ZERO)) {
            throw new IllegalArgumentException("n debe ser positivo e impar para el símbolo de Jacobi");
        }
        BigInteger a = a0.mod(n0);
        BigInteger n = n0;
        int result = 1;

        while (!a.equals(BigInteger.ZERO)) {
            while (a.mod(TWO).equals(BigInteger.ZERO)) {
                a = a.divide(TWO);
                BigInteger r = n.mod(BigInteger.valueOf(8));
                if (r.equals(BigInteger.valueOf(3)) || r.equals(BigInteger.valueOf(5))) {
                    result = -result;
                }
            }
            // swap a and n
            BigInteger tmp = a;
            a = n;
            n = tmp;
            if (a.mod(BigInteger.valueOf(4)).equals(BigInteger.valueOf(3)) &&
                n.mod(BigInteger.valueOf(4)).equals(BigInteger.valueOf(3))) {
                result = -result;
            }
            a = a.mod(n);
        }
        return n.equals(BigInteger.ONE) ? result : 0;
    }
}
