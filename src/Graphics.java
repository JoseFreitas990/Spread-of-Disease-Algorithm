import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public  class Graphics {

    private static final String GNU_PLOT_EXE = "gnuplot";

    /**
     * Gerar o Gráfico com base num ficheiro csv
     *
     *
     *
     * @param fileName
     * @param method  nome do algoritmo utilizado
     * @throws IOException -Exceção lançada por erro de I/O  (Input/Output)
     * @throws InterruptedException - Exceçaõ lançada quando a tarefa é interropida durante a sua execução
     */
    public static void Plot(String fileName, String method) throws IOException, InterruptedException {
        String execute =  GNU_PLOT_EXE + " " + generateGnuPlotConfFile(fileName, method);
        Runtime cmd = Runtime.getRuntime();
        cmd.exec(execute).waitFor();
    }

    /**
     *Grava o ficheiro com as instruções para o gnuplot gerar o gráfico
     *
     * @param fileName nome do ficheiro de imagem a gerar
     * @param method nome do algoritmo utilizado
     * @return nome do ficheiro de configuração  para o gnuplot gerar o gráfico
     * @throws FileNotFoundException  - Lança o erro do ficheiro não encontrado
     */
    public static String  generateGnuPlotConfFile(String fileName, String method) throws FileNotFoundException {
        String fileConf = fileName.replace(".csv", ".gp");
        PrintWriter out = new PrintWriter(new File(fileConf));
        out.println("reset"); // restaura os 'default settings'
        out.println("set terminal pngcairo nocrop enhanced size 640,480 font \"Arial-Bold,14\""); //escoler o format do output tamanho da iamgem e fonte a utilizar
        out.printf("set output \"%s\"%n", fileName.replace(".csv",".png")); // nome do ficheiro de imagem a gerar
        out.println("set autoscale"); //escala automatica para os dois eixos
        out.printf("set datafile separator '%s'%n", Main.FILE_CSV_SEPARATOR); // separador a utililzar
        out.println("set xtic auto");
        out.println("set ytic auto");
        out.println("set xlabel 'Número de dias'"); //Titulo do eixo dos X
        out.println("set ylabel 'População'"); // Titulo do eixo dos Y
        out.printf("set title 'Distribuição da população (%S)'%n", method); //Titulo principal
        out.printf("plot  '%s' using 1:2 title 'S' with lines lt '1' lw '3' ,", fileName); // comando para desenhar o grafico e valores para ao S no ficheiro
        out.printf(" '%s' using 1:3 title 'I' with lines lt '2' lw '3', ", fileName); // valores para ao I no ficheiro
        out.printf(" '%s' using 1:4 title 'R' with lines lt '3' lw '3'%n", fileName); //valores para ao R no ficheiro
        out.println("exit");//sair do gnuplot
        out.close();
        return fileConf;
    }
}
