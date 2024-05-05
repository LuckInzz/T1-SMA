public class Gerador {

    public static double[] geradorCongruenteLinear(double x0, double a, double m, double c) {
        double congruenteLinear[] = new double[100001];
        double uniformamenteDistribuido[] = new double[100001];

        // atribui a semente
        congruenteLinear[0] = x0;
        uniformamenteDistribuido[0] = x0;

        for (int i = 1; i < 100001; i++) {
            congruenteLinear[i] = ((congruenteLinear[i - 1] * a) + c) % m;
            uniformamenteDistribuido[i] = ((congruenteLinear[i] / m));
        }
        return uniformamenteDistribuido;
    }

    public static void main(String[] args) {
        double x0 = 111750;
        double a = 1664525;
        double m = Math.pow(2, 32);
        double c = 1013904223;

        double numerosGerados[] = geradorCongruenteLinear(x0, a, m, c);

        for (int i = 1; i < 100001; i++) {
            System.out.print(String.format("%d %.10f\n", i, numerosGerados[i]));
        }
    }
}