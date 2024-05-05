import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.*;
import java.util.*;

public class FilaSimulador {

    /*
     * private static int seed = 111750;
     * private static final int a = 1664525;
     * private static final int M = (int) Math.pow(2, 32);
     * private static final int c = 1013904223;
     * static double numerosGerados[];
     */
    static int countNumerosAleatorios;
    static Random rnd = new Random();
    static ArrayList<Fila> listaDeFilas = new ArrayList<>();
    static Fila fila1, fila2, fila3;
    static ArrayList<Passagem> listaDePassagens1, listaDePassagens2, listaDePassagens3;

    public static void main(String[] args) {
        int numFilas = 3;
        // double chegadaInicial = 2.0;
        // File file = new File("modelo.yml");
        // Gerador g1 = new Gerador();

        // numerosGerados = Gerador.geradorCongruenteLinear(seed, a, M, c);
        countNumerosAleatorios = 100000;

        // Inicialização das filas
        listaDeFilas.add(fila1 = new Fila(0, 1, 20, 2.0, 4.0, 1.0, 2.0, listaDePassagens1 = new ArrayList<>())); // G/G/1/INFINITO
        listaDeFilas.add(fila2 = new Fila(1, 2, 5, 1.0, 2.0, 4.0, 8.0, listaDePassagens2 = new ArrayList<>())); // G/G/2/5
        listaDeFilas.add(fila3 = new Fila(2, 2, 10, 1.0, 2.0, 5.0, 15.0, listaDePassagens3 = new ArrayList<>())); // G/G/2/10

        listaDePassagens1.add(new Passagem(fila1, fila2, 0.8));
        listaDePassagens1.add(new Passagem(fila1, fila3, 0.2));
        // listaDePassagens1.add(new Passagem(fila1, null, 0.5));
        listaDePassagens2.add(new Passagem(fila2, fila3, 0.5));
        listaDePassagens2.add(new Passagem(fila2, fila1, 0.3));
        listaDePassagens2.add(new Passagem(fila2, null, 0.2));
        listaDePassagens3.add(new Passagem(fila3, fila2, 0.7));
        listaDePassagens3.add(new Passagem(fila3, null, 0.3));

        PriorityQueue<Evento> escalonador = new PriorityQueue<>();

        double tempoGlobal = 2.0;
        // Inicia a primeira chegada da fila 1
        escalonador.add(new Evento(tempoGlobal, TipoEvento.CHEGADA, null, fila1));
        // escalonador.add(new Evento(tempoGlobal, TipoEvento.PASSAGEM, fila1, fila2));
        // escalonador.add(new Evento(tempoGlobal, TipoEvento.SAIDA, fila2, null));

        while (countNumerosAleatorios > 0) {
            // Recupera o próximo evento da fila prioritária
            Evento eventoAtual = escalonador.poll();
            Fila fila = null;
            // Trate o evento de acordo com o tipo
            if (eventoAtual != null) {
                if (eventoAtual.getTipo() == TipoEvento.CHEGADA) {
                    fila = eventoAtual.getFilaDestino();
                } else {
                    fila = eventoAtual.getFilaOrigem();
                }

                switch (eventoAtual.getTipo()) {
                    case CHEGADA:
                        chegada(fila, eventoAtual.getTempo(), escalonador);
                        break;
                    case SAIDA:
                        saida(fila, eventoAtual.getTempo(), escalonador);
                        break;
                    case PASSAGEM:
                        passagem(fila, eventoAtual.getFilaDestino(), eventoAtual.getTempo(), escalonador);
                        break;
                }
                // countNumerosAleatorios--; // Decrementa o contador de eventos processados
            }
        }

        // Impressão da distribuição de probabilidade dos estados de cada fila
        for (int i = 0; i < numFilas; i++) {

            System.out.println(
                    "*********************************************************");
            System.out.println("   State (Fila " + (i + 1) +
                    ")         Time               Probability");
            double totalSimulado = listaDeFilas.get(i).getTotalSimulado();
            for (int j = 0; j < listaDeFilas.get(i).getTempos().length; j++) {
                double probabilidade = listaDeFilas.get(i).getTempos()[j] / totalSimulado *
                        100;
                System.out.printf("%7d%21.2f%23.2f%%%n", j,
                        listaDeFilas.get(i).getTempos()[j], probabilidade);
            }
            // Printa o número de perdas de clientes na fila
            System.out.println("\nNumber of losses in Fila " + (i + 1) + ": " + listaDeFilas.get(i).getPerda());

            System.out.println(listaDeFilas.get(i).getTotalSimulado());

        }

        // Impressão das contagens de passagens
        System.out.println("Contagens de passagens:");
        for (Fila fila : listaDeFilas) {
            System.out.println("Fila " + (fila.getFilaIndex() + 1) + ":");
            for (Passagem passagem : fila.getListaPassagens()) {
                System.out.println("Origem: " + passagem.getFilaOrigem().getFilaIndex() +
                        ", Destino: "
                        + (passagem.getFilaDestino() != null ? passagem.getFilaDestino().getFilaIndex() : "null") +
                        ", Contagem: " + passagem.getCountPassagem());
            }
        }
    }

    private static void chegada(Fila fila, double tempo, PriorityQueue<Evento> escalonador) {
        fila.contaTempo(tempo);
        if (fila.getClientes() < fila.getCapacidade()) {
            fila.entrada();
            if (fila.getClientes() <= fila.getServidores()) {
                double prob = rnd.nextDouble(0, 1);
                countNumerosAleatorios--;
                double sum = 0.0;
                for (Passagem passagem : fila.getListaPassagens()) {
                    sum += passagem.getProb();
                    if (prob < sum) {
                        if (passagem.getFilaDestino() != null) { // Se a passagem tiver destino, agenda uma passagem.
                            passagem.incrementaCountPassagem();
                            // System.out.println("Chegou na criação de passagem em 'chamada'.");
                            escalonador.add(new Evento(
                                    tempo + fila
                                            .calculaTempoSaida(rnd.nextDouble(fila.getAtendMin(), fila.getAtendMax())),
                                    TipoEvento.PASSAGEM, passagem.getFilaOrigem(),
                                    passagem.getFilaDestino()));
                            countNumerosAleatorios--;
                        } else if (passagem.getFilaDestino() == null) {
                            passagem.incrementaCountPassagem();
                            // System.out.println("Chegou na criação de saída em 'chamada'.");
                            escalonador.add(new Evento(
                                    tempo + fila
                                            .calculaTempoSaida(rnd.nextDouble(fila.getAtendMin(), fila.getAtendMax())),
                                    TipoEvento.SAIDA, passagem.getFilaOrigem(), null));
                            countNumerosAleatorios--;
                        }
                        break;
                    }
                }
            }
        } else {
            fila.perda();
        }
        escalonador.add(new Evento(
                tempo + fila.calculaTempoChegada(rnd.nextDouble(fila.getChegadaMin(), fila.getChegadaMax())),
                TipoEvento.CHEGADA, null, fila));
        countNumerosAleatorios--;
    }

    private static void saida(Fila fila, double tempo, PriorityQueue<Evento> escalonador) {
        fila.contaTempo(tempo);
        fila.saida();
        if (fila.getClientes() >= fila.getServidores()) {
            escalonador.add(new Evento(
                    tempo + fila.calculaTempoSaida(rnd.nextDouble(fila.getChegadaMin(), fila.getChegadaMax())),
                    TipoEvento.SAIDA, fila, null));
        }
    }

    private static void passagem(Fila filaOrigem, Fila filaDestino, double tempo, PriorityQueue<Evento> escalonador) {
        filaOrigem.contaTempo(tempo);
        filaOrigem.saida();

        if (filaOrigem.getClientes() >= filaOrigem.getServidores()) {
            escalonador.add(new Evento(
                    tempo + filaOrigem
                            .calculaTempoSaida(rnd.nextDouble(filaOrigem.getAtendMin(),
                                    filaOrigem.getAtendMax())),
                    TipoEvento.PASSAGEM, filaOrigem, filaDestino));
            countNumerosAleatorios--;
        }

        if (filaDestino != null) {
            if (filaDestino.getClientes() < filaDestino.getCapacidade()) {
                filaDestino.contaTempo(tempo);
                filaDestino.entrada();
                if (filaDestino.getClientes() <= filaDestino.getServidores()) {
                    double prob = rnd.nextDouble(0, 1);
                    countNumerosAleatorios--;
                    double sum = 0.0;
                    for (Passagem passagem : filaDestino.getListaPassagens()) {
                        sum += passagem.getProb();
                        if (prob < sum) {
                            if (passagem.getFilaDestino() == null) {
                                // System.out.println("Chegou na criação de saída em 'PASSAGEM'.");
                                passagem.incrementaCountPassagem();
                                escalonador.add(new Evento(
                                        tempo + filaDestino
                                                .calculaTempoSaida(rnd.nextDouble(filaDestino.getAtendMin(),
                                                        filaDestino.getAtendMax())),
                                        TipoEvento.SAIDA, passagem.getFilaOrigem(), passagem.getFilaDestino()));
                                countNumerosAleatorios--;
                                break;
                            } else {
                                // System.out.println("Chegou na criação de passagem em 'PASSAGEM'.");
                                passagem.incrementaCountPassagem();
                                escalonador.add(new Evento(
                                        tempo + filaDestino.calculaTempoSaida(rnd.nextDouble(filaDestino.getAtendMin(),
                                                filaDestino.getAtendMax())),
                                        TipoEvento.PASSAGEM, passagem.getFilaOrigem(),
                                        passagem.getFilaDestino()));
                                countNumerosAleatorios--;
                                break;
                            }
                        }
                    }
                }
            } else {
                filaDestino.perda();
            }
        }
    }
}

class Fila {
    private int servidores;
    private int capacidade;
    private double chegadaMin;
    private double chegadaMax;
    private double atendMin;
    private double atendMax;
    private int clientes = 0;
    private int perda = 0;
    private double[] tempos;
    private double totalSimulado = 0;
    private int filaIndex;
    private ArrayList<Passagem> listaPassagens;

    public Fila(int filaIndex, int servidores, int capacidade, double chegadaMin, double chegadaMax, double atendMin,
            double atendMax, ArrayList<Passagem> listaPassagens) {
        this.servidores = servidores;
        this.capacidade = capacidade;
        this.chegadaMin = chegadaMin;
        this.chegadaMax = chegadaMax;
        this.atendMin = atendMin;
        this.atendMax = atendMax;
        this.filaIndex = filaIndex;
        tempos = new double[capacidade + 1]; // +1 para incluir o estado de perda
        this.listaPassagens = listaPassagens;
    }

    public int getClientes() {
        return clientes;
    }

    public ArrayList<Passagem> getListaPassagens() {
        return listaPassagens;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public int getServidores() {
        return servidores;
    }

    public int getPerda() {
        return perda;
    }

    public double[] getTempos() {
        return tempos;
    }

    public double getChegadaMin() {
        return chegadaMin;
    }

    public double getChegadaMax() {
        return chegadaMax;
    }

    public double calculaTempoChegada(double numeroAleatorio) {
        double r = ((chegadaMax - chegadaMin) * FilaSimulador.rnd.nextDouble(chegadaMin, chegadaMax)) + chegadaMin;
        FilaSimulador.countNumerosAleatorios--;
        return r;
    }

    public double calculaTempoSaida(double numeroAleatorio) {
        double r = ((atendMax - atendMin) * FilaSimulador.rnd.nextDouble(atendMin, atendMax)) + atendMin;
        FilaSimulador.countNumerosAleatorios--;
        return r;
    }

    public double getAtendMin() {
        return atendMin;
    }

    public double getAtendMax() {
        return atendMax;
    }

    public void contaTempo(double tempoGlobal) {
        // if (getClientes() >= 0) {
        tempos[getClientes()] += (tempoGlobal - totalSimulado);
        totalSimulado = tempoGlobal;
        // }
    }

    public void saida() {
        if (clientes > 0)
            clientes--;
    }

    public double getTotalSimulado() {
        return totalSimulado;
    }

    public void perda() {
        perda++;
    }

    public void entrada() {
        clientes++;
    }

    public int getFilaIndex() {
        return filaIndex;
    }
}

class Passagem {
    private Fila filaOrigem;
    private Fila filaDestino;
    // private TipoEvento evento;
    private double probabilidade;
    int countPassagem = 0;

    public Passagem(Fila filaOrigem, Fila filaDestino, double probabilidade) {
        this.filaDestino = filaDestino;
        this.filaOrigem = filaOrigem;
        this.probabilidade = probabilidade;
    }

    public void incrementaCountPassagem() {
        countPassagem++;
    }

    public int getCountPassagem() {
        return countPassagem;
    }

    public Fila getFilaOrigem() {
        return filaOrigem;
    }

    public Fila getFilaDestino() {
        return filaDestino;
    }

    public double getProb() {
        return probabilidade;
    }

    @Override
    public String toString() {
        return "Origem: " + filaOrigem.getFilaIndex() + ", Destino: "
                + (filaDestino != null ? filaDestino.getFilaIndex() : "null") + ", Probabilidade: " + probabilidade
                + "\n";
    }
}

class Evento implements Comparable<Evento> {
    private double tempo;
    private TipoEvento tipo;
    Fila filaOrigem;
    Fila filaDestino;
    double probabilidade;

    public Evento(double tempo, TipoEvento tipo, Fila filaOrigem, Fila filaDestino) {
        this.tempo = tempo;
        this.tipo = tipo;
        this.filaOrigem = filaOrigem;
        this.filaDestino = filaDestino;
    }

    public double getTempo() {
        return tempo;
    }

    public TipoEvento getTipo() {
        return tipo;
    }

    public Fila getFilaOrigem() {
        return filaOrigem;
    }

    public Fila getFilaDestino() {
        return filaDestino;
    }

    public double getProb() {
        return probabilidade;
    }

    @Override
    public int compareTo(Evento outro) {
        return Double.compare(this.tempo, outro.tempo);
    }
}

enum TipoEvento {
    CHEGADA, SAIDA, PASSAGEM
}
