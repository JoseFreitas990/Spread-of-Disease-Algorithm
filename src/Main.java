import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Main {
    public static String APP_NAME = "S.I.R. CALCULATOR";
    private static final int MENU_MARGIN = 5;
    public static char FILE_CSV_SEPARATOR = ';';
    public static double DECIMAL_PRECISION = 2d;


    //private static Scanner SCAN = new Scanner(System.in);

    /**
     *  Inicio da aplicação
     *
     * @param args -  (opcional) argumentos introduzidos  na linha de comandos
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        if(args.length > 0){
            nonInteractiveMode(args);
        }
        else {
            interactiveMode();
        }
        System.out.printf("%nObrigado!%n%n[programa terminado]%n");
    }

    /**
     * Modo não interativo. Executa com as instruções especificadas nos argumento introduzidos na linha de comandos
     *
     * @param args - parametros
     * @throws IOException -Exceção lançada por erro de I/O  (Input/Output)
     * @throws InterruptedException - Exceçaõ lançada quando a tarefa é interropida durante a sua execução
     */
    private static void  nonInteractiveMode(String[] args) throws IOException, InterruptedException {
        String sirFileName = "-";
        int algorithm = -1; //método(m)  .. 1 - Euler 2 KG4
        int population = -1; //população(t)
        int days = -1;//dias(d)
        double step = -1;//passo (p)

        //Modo não interativo
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                switch (args[i]) {
                    case "-m":
                        algorithm = Integer.parseInt(args[++i]); //método
                        break;
                    case "-p":
                        step = Double.parseDouble(args[++i]); //passo(h)
                        break;
                    case "-t":
                        population = Integer.parseInt(args[++i]); //tamanho população(N)
                        break;
                    case "-d":
                        days = Integer.parseInt(args[++i]); //nr dias
                        break;
                }
            } else {
                sirFileName = args[i];
            }
        }

        if (!isOptionValid(sirFileName, step, population, days, true)) { //Validar  se parametros são validos
            //algorithm = 0;
            System.out.println("Não foi possível executar o cálculo.");
        } else if (algorithm != 1 && algorithm != 2) { //Validar  se o algoritmo é válido 1 ou 2
            System.out.println("O Método indicado desconhecido");
        }
        else{ // Passou na validação executa
            executeAlgorithm(algorithm, sirFileName, step, population, days, false);
            System.out.println("Cálculo Terminado");
        }
    }

    /**
     * Modo Interativo. Mostra o menu no qual o utilizador especifica o valor dos parâmetros
     *
     * @throws IOException -Exceção lançada por erro de I/O  (Input/Output)
     * @throws InterruptedException - Exceçaõ lançada quando a tarefa é interropida durante a sua execução
     */
    private static void  interactiveMode() throws IOException, InterruptedException {
        String sirFileName = "-";
        int algorithm = -1; //método(m)  .. 1 - Euler 2 KG4
        int population = -1; //população(t)
        int days = -1;//dias(d)
        double step = -1;//passo (p)
        boolean exit = false; // indica se vai terminar (true) ou não (false)
        while(!exit) {
            //Modo Interativo
            boolean run = false;
            Scanner scan = new Scanner(System.in);
            do {
                boolean valid = isOptionValid(sirFileName, step, population, days, false);
                printMenu(sirFileName, step, population, days, valid);
                int comando = scan.nextInt();
                switch (comando) {
                    case 0:
                        algorithm = 0;
                        run = true;
                        break;
                    case 1:
                        sirFileName = readParameter("Ficheiro Sir");
                        break;
                    case 2:
                        step = Double.parseDouble(readParameter("passo"));
                        break;
                    case 3:
                        population = Integer.parseInt(readParameter("população"));
                        break;
                    case 4:
                        days = Integer.parseInt(readParameter("dias"));
                        break;
                    case 5:
                        System.out.println();
                        if (isOptionValid(sirFileName, step, population, days, true)) {
                            System.out.println("  Parâmetros Válidos");
                        }
                        System.out.println("\n...Prima ENTER para prosseguir");
                        scan.nextLine();
                        break;
                    case 8:
                        if (valid) {
                            run = true;
                            algorithm = 1;
                        } else {
                            printInvalidOptions();
                        }
                        break;
                    case 9:
                        if (valid) {
                            run = true;
                            algorithm = 2;
                        } else {
                            printInvalidOptions();
                        }
                        break;
                    default:
                        printInvalidOptions();
                }
                scan.nextLine(); //limpar o buffer
            } while (!run);
            if(algorithm == 0){
                exit = true;
            }
            else{
                exit = !executeAlgorithm(algorithm, sirFileName, step, population, days, true);
            }
        }
    }

    /**
     * Executa as instruções conforme as valores dos parâmetros recebidos
     *
     * @param algorithm algoritmo a executar 1 (Euler) 2 (RK4)
     * @param sirFileName - Nome dos ficheiros com as taxas
     * @param step - valor do passo (h)
     * @param population - valor da população (p)
     * @param days - valor dos dias (d)
     * @param interactive Modo interativo (true) não interativo (false)
     * @return No modo  não interativo retorn sem pre false, no modo interativo questiona se o utilizador volta ao menu (true) ou finaliza a aplicação (false)
     * @throws IOException -Exceção lançada por erro de I/O  (Input/Output)
     * @throws InterruptedException - Exceçaõ lançada quando a tarefa é interropida durante a sua execução
     */
    private static boolean executeAlgorithm(int algorithm, String sirFileName, double step, int population, int days, boolean interactive ) throws IOException, InterruptedException {
        String[][] sirDataArray = new String[100][5];
        String algorithmName = getMethodStringName(algorithm);
        int sirMaxRecords = readInputFile(sirFileName, sirDataArray); // total de registo lidos
        printBoxTitle("MÉTODO " + algorithmName);
        for (int i = 0; i < sirMaxRecords; i++) {

            double beta = Double.parseDouble(sirDataArray[i][1].replace(",", "."));
            double gamma = Double.parseDouble(sirDataArray[i][2].replace(",", "."));
            double ro = Double.parseDouble(sirDataArray[i][3].replace(",", "."));
            double alpha = Double.parseDouble(sirDataArray[i][4].replace(",", "."));
            double[][] data = null;
            switch (algorithm) {
                case 1:
                    data = algorithmEuler( step, population, days, alpha, beta, gamma, ro);
                    break;
                case 2:
                    data = algorithmRK4( step, population, days, alpha, beta, gamma, ro);
                    break;
            }
            if (data != null) {
                String fileName = String.format("%sm%dp%st%dd%d.csv", sirDataArray[i][0], algorithm,
                        String.format("%.1f", step).replace(",", "").replace(".", ""),
                        population, days);
                writeOutputFile(data, fileName);
                Graphics.Plot(fileName, algorithmName);//getMethodStringName(algorithm));
            } else {
                System.out.println("Não Foi possivel Calcular os Valores");
            }
        }
        if(interactive){
            Scanner scan = new Scanner(System.in);
            boolean validResponse = false;
            System.out.printf("Algoritmo %s terminou. %n", algorithmName);
            do{
                System.out.print("Deseja voltar ao menu (s/n):");
                switch (scan.next()) {
                case "s":
                case "S":
                    interactive = validResponse = true;
                    break;
                case "n":
                case "N":
                    validResponse = true;
                    interactive = false;
                    break;
                }
            }while(!validResponse);
        }
        return interactive;
    }

    /**
     * Imprime o Menu com as respetivas opções
     *
     * @param sirFileName - Nome dos ficheiros com as taxas
     * @param step - valor do passo (h)
     * @param population - valor da população (p)
     * @param days - valor dos dias (d)
     * @param printExecutionOption -  Imprime (true) ou Não Imprime (false)  as opções de Execução
     */
    private static void printMenu(String sirFileName, double step, int population, int days, boolean printExecutionOption) {
        printBoxTitle("MENU");
        System.out.printf("[1] Ficheiro de dados [%s]%n", sirFileName);
        System.out.printf("[2] Tamanho do passo(0<h<=1) [%.2f]%n", step);
        System.out.printf("[3] Tamanho da população [%d]%n", population);
        System.out.printf("[4] Nr de dias em estudo [%d]%n", days);
        System.out.println("[5] Validar Parametros");
        if (printExecutionOption) {
            //Opções de Exxecução
            System.out.println("[8] Executar Método de Euler");
            System.out.println("[9] Executar Método de Runge Kutta 4ªOrdem (RK4)");
        }
        System.out.println("[0] Sair");
        System.out.print("\nDigite o carater do parâmetro a editar ou ação a executar e prima ENTER: ");
    }

    /**
     * Imprime a caixa de titulo
     *
     * @param title
     */
    private static void printBoxTitle(String title) {
        int menuBoxlenght = (title.length() > APP_NAME.length() ? title.length() : APP_NAME.length()) + MENU_MARGIN * 2 + 2;
        printBoxTopBotton(menuBoxlenght, true);
        printBoxSide(menuBoxlenght, APP_NAME);
        if (title.length() > 0) {
            printBoxSide(menuBoxlenght, title);
        }
        printBoxTopBotton(menuBoxlenght, false);
    }

    /**
     * Imprime o limitador da caixa superior ou inferior
     * @param boxlenght comprimento da caixa
     * @param top indica se o o superior(true) ou inferior (false)
     */
    private static void printBoxTopBotton(int boxlenght, boolean top) {
        for (int i = 0; i < boxlenght; i++) {
            if (i + 1 == boxlenght) System.out.printf("%s%n", top ? "╗" : "╝"); // Cantos da direito da caixa (cima ou baixo)
            else if (i == 0) System.out.printf("%s", top ? "╔" : "╚"); // Cantos da esquerdos da caixa (cima ou baixo)
            else System.out.print("═");// Limitador superior ou infoerior
        }
    }

    /**
     * Imprime para o stdout as laterais das caixa do menu com ou sem text interno
     *
     * @param boxlenght comprimento da caixa
     * @param text text a escrever pode ser vazio
     */
    private static void printBoxSide(int boxlenght, String text) {
        //Ajustar Texto para strings com carateres impares
        if (text.length() > 0 && text.length() % 2 != 0) {
            text += " ";//adicona um carater espaço no fnal tornando a string co comprimento par
        }
        int len = (boxlenght - text.length()) / 2; //Calcular a inicio Da escrita do texto
        for (int i = 0; i < boxlenght; i++) {
            if (i + 1 == boxlenght) System.out.printf("║%n");//limitador direito e mudar de linha
            else if (i == 0) System.out.print("║"); //limitador lateral esquerdo
            else {
                if (text.length() == 0 || i <= len || i > len + text.length()) System.out.print(" ");//espaço internos da caixa vazios
                else {
                    System.out.print(text);// Texto
                    i += text.length() - 1;//aumentar o i com tamanho do texto preenchido
                }
            }
        }
    }

    /**
     * Imprime para o "Standart output" o texto de opção desconhecida
     */
    private static void printInvalidOptions() {
        System.out.println("\n(!) A OPÇÃO É DESCONHECIDA.\nPF, prima ENTER e selecione um comando válido do MENU.");
    }

    /**
     * Testa se opções do menu interativo  são válidas
     * Opcionalmente é impresso para o stdout os inválidos quando existem
     *
     * @param sirFileName - Nome dos ficheiros com as taxas
     * @param step - valor do passo (h)
     * @param population - valor da população (p)
     * @param days - valor dos dias (d)
     * @param printAlerts - Bit a true Imprime os pârametros inválidos
     * @return valor da validação (true/false)
     */
    private static boolean isOptionValid(String sirFileName, double step, int population, int days, boolean printAlerts) {
        boolean valid = true;
        if (!new File(sirFileName).exists()) {
            valid = false;
            if (printAlerts) {
                System.out.println("    O ficheiro indicado não existe.");
            }
        }
        if (step <= 0 || step > 1) {
            valid = false;
            if (printAlerts) {
                System.out.println("    O tamanho do passo está fora dos parâmetros definidos (0<h<=1).");
            }
        }
        if (population < 0) {
            valid = false;
            if (printAlerts) {
                System.out.println("    O tamanho da população é inválida.");
            }
        }
        if (days < 0) {
            valid = false;
            if (printAlerts) {
                System.out.println("    O número de dias em estudo é inválido.");
            }
        }

        return valid;
    }

    /**
     * Devolve o nome do metodo quando invalido devolve "unknow"
     *
     * @param method - numero do metodo a descobrir
     * @return resultado
     */
    private static String getMethodStringName(int method) {
        switch (method) {
            case 1:return "EULER";
            case 2:return"RK4";
        }
        return "Unknow";
    }

    /**
     * Imprime(no stdout) o texto a indicar a introdução do valor do parâmetro
     * @param parameter nome do parametro a questionar
     * @return valor introduzido pelo utilizador
     */
    private static String readParameter(String parameter) {
        Scanner scan = new Scanner(System.in);
        System.out.printf("Digite o  valor do parâmetro (%s) e prima ENTER: ", parameter);
        return scan.next();

    }



    /*Files Managment*/

    /**
     * Guarda o resultados de um array com os dados calculos de um modelo S.I.R. separados pelo File_CSV_SEPARATOR
     * (Nota:   Adicona na 1 lina o título
     *          A 1 coluna será a possição array lido e que corresponde ao dia)
     *
     * @param resultsDataArray -
     * @param fileName - Nome do Ficheiro a guardar os dados
     * @throws FileNotFoundException - Lança o erro do ficheiro não encontrado
     */
    private static void writeOutputFile(double[][] resultsDataArray, String fileName) throws FileNotFoundException {
        PrintWriter out = new PrintWriter(new File(fileName));
        out.println("dia" + FILE_CSV_SEPARATOR + "S" + FILE_CSV_SEPARATOR + "I" + FILE_CSV_SEPARATOR + "R" + FILE_CSV_SEPARATOR + "N");
        for (int i = 0; i < resultsDataArray.length; i++) {
            out.printf("%d", i);
            for (int j = 0; j < resultsDataArray[i].length; j++) {
                out.printf("%s%f", FILE_CSV_SEPARATOR, resultsDataArray[i][j]);
            }
            out.printf("%n");
        }
        out.close();
    }

    /**
     * Lê o conteúdo de uma ficheiro com os valores das taxas de alpha(α), beta(β), gamma(γ) e ro(ρ)
     * em cada linha por cenário
     *
     * @param fileName - ficheiro a abrir
     * @param sirDataArray - Array a polular com os dados lidos do ficheiro
     * @return numero total de registos lidos
     * @throws FileNotFoundException  - Lança o erro do ficheiro não encontrado
     */
    private static int readInputFile(String fileName, String[][] sirDataArray) throws FileNotFoundException {
        File file = new File(fileName);
        int lines = 0;
        if (file.exists()) {
            Scanner scan = new Scanner(file);
            while (scan.hasNextLine()){
                //ler a primeira linha e ignorar - Tem os titulos das colunas
                if(lines ==  0){
                    scan.nextLine();
                }
                else {
                    sirDataArray[lines -1] = scan.nextLine().split( String.valueOf(FILE_CSV_SEPARATOR)); //Dividir a String lida num array
                }
                lines++;
            }
        }
        return --lines; //Devolver a contagem de regitos -(minus) a linha do título que foi ignorada
    }

    /*Algorithms*/

    /**
     * Implementação do Algoritmo de Runge-Kutta de 4ª Ordem  (KG4)
     *
     * @param step h - passo de integração numérica
     * @param population p - Tamanho da população
     * @param days d - Total de dias a analisar
     * @param alpha α – Taxa de recuperados que são reinfectados
     * @param beta  β – Taxa de propagação da notícia
     * @param gamma γ – Taxa de rejeição à notícia
     * @param ro ρ – Taxa de individuos não imune à notícia
     * @return Array de com os valores de S.I.R. e N calculado por dia
     */
    public static double[][] algorithmRK4(double step,  int population, int days, double alpha, double beta, double gamma,
                                          double ro  ) {

        double[][] resultsDataArray = new double[days][4];
        double numberOfSteps = days / step;
        double currentStep = step;
        double s = population-1;
        double i = 1;
        double r = 0;
        resultsDataArray[0] = new double[]{s, i, r, s + i + r};
        int diaatual = 1;

        // Percorrer o número de steps dados
        for (int j = 0; j < numberOfSteps; j++ ) {
            // Calcular k1,l1,m1
            double k1 = calculateSusceptiblesVariationRate(s, i,  beta);
            double l1 = calculateInfectedVariationRate(s, i, r, alpha, beta, gamma, ro);
            double m1 = calculateRecoveredVariationRate(s, i, r, alpha, beta, gamma, ro);

            // Calcular k2,l2,m2
            double k2 = calculateSusceptiblesVariationRate(s + k1 * step / 2, i + l1 * step / 2,  beta);
            double l2 = calculateInfectedVariationRate(s + k1 * step / 2, i + l1 * step / 2, r + m1 * step / 2, alpha, beta, gamma, ro);
            double m2 = calculateRecoveredVariationRate(s + k1 * step / 2, i + l1 * step / 2, r + m1 * step / 2, alpha, beta, gamma, ro);

            // Calcular k3,l3,m3
            double k3 = calculateSusceptiblesVariationRate(s + k2 * step / 2, i + l2 * step / 2,  beta);
            double l3 = calculateInfectedVariationRate(s + k2 * step / 2, i + l2 * step / 2, r + m2 * step / 2, alpha, beta, gamma, ro);
            double m3 = calculateRecoveredVariationRate(s + k2 * step / 2, i + l2 * step / 2, r + m2 * step / 2, alpha, beta, gamma, ro);

            // Calculate k4,l4,m4
            double k4 = calculateSusceptiblesVariationRate(s + k3 * step, i + l3 * step,  beta);
            double l4 = calculateInfectedVariationRate(s + k3 * step, i + l3 * step, r + m3 * step, alpha, beta, gamma, ro);
            double m4 = calculateRecoveredVariationRate(s + k3 * step, i + l3 * step, r + m3 * step, alpha, beta, gamma, ro);

            // Calcular os novos valores de S,I,R
            s += (k1 + 2 * k2 + 2 * k3 + k4) * step / 6;
            i += (l1 + 2 * l2 + 2 * l3 + l4) * step / 6;
            r += (m1 + 2 * m2 + 2 * m3 + m4) * step / 6;


            if(currentStep >= diaatual && diaatual < days){
                resultsDataArray[diaatual++] = new double[]{s, i, r, s + i + r};
            }
            currentStep = roundPrecision(currentStep + step);


        }
        return resultsDataArray;
    }

    /**
     * Implementação do Algoritmo de Euler
     *
     * @param step h - passo de integração numérica
     * @param population p - Tamanho da população
     * @param days d - Total de dias a analisar
     * @param alpha α – Taxa de recuperados que são reinfectados
     * @param beta  β – Taxa de propagação da notícia
     * @param gamma γ – Taxa de rejeição à notícia
     * @param ro ρ – Taxa de individuos não imune à notícia
     * @return Array de com os valores de S.I.R. e N calculado por dia
     */
    public static double[][] algorithmEuler(double step, int population, int days, double alpha, double beta, double gamma,
                                            double ro) {
        double[][] resultsDataArray = new double[days][4];
        double numberOfSteps = days /step;
        double currentStep = step;
        double s = population-1;
        double i = 1;
        double r = 0;
        resultsDataArray[0] = new double[]{s, i, r, s + i + r};
        int diaatual = 1;
        // Percorrer o número de steps dados
        for (int j = 0; j < numberOfSteps; j++) {
            // Calcular Suscetiveis
            double sr = s + step * calculateSusceptiblesVariationRate(s, i, beta);
            // Calcular Infetados
            double ir = i + step * calculateInfectedVariationRate(s, i, r, alpha, beta, gamma, ro);
            // Calcular Recuperados
            double rr = r + step * calculateRecoveredVariationRate(s, i, r, alpha, beta, gamma, ro);
            s=sr;
            i=ir;
            r=rr;
            if(currentStep >= diaatual && diaatual < days){
                resultsDataArray[diaatual++] = new double[]{s, i, r, s + i + r};
            }
            currentStep = roundPrecision(currentStep + step);
        }
        return resultsDataArray;
    }

    /**
     * Arredonda um valor de um double descartando todos os valores à direita da , mediante a precisão
     *
     * ex. DECIMAL_PRECISION = 2
     * precisionMultiplier = 100d - obtido por 10 elevado ao ROUND_PRECISION
     * valor = 0.11101000000789232
     * 1º Puxar os Valores que se prendende manter x digito para a esquerda x = precisionMultiplier
     *  11.101000000789233
     * 2º Buscar a parte inteira
     *  11
     * 3º Colocar os numero na sua posição correta
     *  0.11
     * @param toRound
     * @return
     */
    private static double roundPrecision(double toRound){
        //calcular a potencia
        double precisionMultiplier = 1d;
        for (int i = 1; i <= DECIMAL_PRECISION; i++) {
            precisionMultiplier = precisionMultiplier * 10d;
        }
        return (int)(toRound * precisionMultiplier) / precisionMultiplier;
    }

    /*EDOs*/

    /**
     * Calcular Taxa de variação de Suscetíveis
     *
     * Equação Diferencial Ordinária
     * dS/dt = -β.S.I
     *
     * @param susceptible S – População Suscetíveis a uma notícia falsa
     * @param infected I – População Infectada pela notícia falsa
     * @param beta  β – Taxa de propagação da notícia
     * @return dS/dt
     */
    public static double calculateSusceptiblesVariationRate(double susceptible, double infected, double beta) {
        return -beta * susceptible * infected;
    }

    /**
     * Taxa de variação de Infetados
     *
     * Equação Diferencial Ordinária
     * dI/dt = ρ.β.S.I-γ.I+α.R
     *
     * @param susceptible S – População Suscetíveis a uma notícia falsa
     * @param infected I – População Infectada pela notícia falsa
     * @param recovered R – População Recuperada da notícia falsa
     * @param alpha α – Taxa de recuperados que são reinfectados
     * @param beta  β – Taxa de propagação da notícia
     * @param gamma γ – Taxa de rejeição à notícia
     * @param ro ρ – Taxa de individuos não imune à notícia
     * @return dI/dt
     */
    public static double calculateInfectedVariationRate(double susceptible, double infected, double recovered, double alpha, double beta, double gamma,
                                                        double ro) {
        return ro * beta * susceptible * infected - gamma * infected + alpha * recovered;
    }

    /**
     * Taxa de variação de Recuperados
     *
     * Equação Diferencial Ordinária
     * dR/dt = γ.I−α.R+(1−ρ).β.S.I
     *
     * @param susceptible S – População Suscetíveis
     * @param infected I – População Infectada
     * @param recovered R – População Recuperada
     * @param alpha α – Taxa de recuperados que são reinfectados
     * @param beta  β – Taxa de propagação da notícia
     * @param gamma γ – Taxa de rejeição à notícia
     * @param ro ρ – Taxa de individuos não imune à notícia
     * @return dR/dt
     */
    public static double calculateRecoveredVariationRate(double susceptible, double infected, double recovered, double alpha, double beta, double gamma,
                                                         double ro) {
        return gamma * infected - alpha * recovered + (1 - ro) * beta * susceptible * infected;
    }
}