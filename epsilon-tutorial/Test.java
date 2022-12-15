public class Test {

    static final int MEGABYTE_EM_BYTES = 1024 * 1024;
    static final int QUANT_INTERACOES = 1024 * 10;

    public static void main(String[] args) {
        System.out.println("Comecando a insercao");

        for (int i = 0; i < QUANT_INTERACOES; i++) {
            byte[] array = new byte[MEGABYTE_EM_BYTES];
        }

        System.out.println("Finalizando");
    }
}
