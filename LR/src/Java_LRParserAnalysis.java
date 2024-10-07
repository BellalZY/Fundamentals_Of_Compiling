import java.util.*;

public class Java_LRParserAnalysis
{
    private enum state{
        shift, reduce, accept, error
    }
    private static ArrayList<String> Terminals = new ArrayList<>(Arrays.asList("{", "}", "if", "while", "(", ")", "then", "else", "ID", "=", ">", "<", ">=", "<=", "==", "+", "-", "*", "/", "NUM", "E"));
    private static ArrayList<String> nonTerminals = new ArrayList<>(Arrays.asList("program", "compoundstmt", "stmt", "stmts", "ifstmt", "whilestmt", "assgstmt", "boolexpr", "arithexpr", "multexpr", "boolop", "arithexprprime", "multexprprime", "simpleexpr"));
    private static String[] production = {"program -> compoundstmt", "stmt ->  ifstmt  |  whilestmt  |  assgstmt  |  compoundstmt", "compoundstmt ->  { stmts }", "stmts ->  stmt stmts   |   E", "ifstmt ->  if ( boolexpr ) then stmt else stmt", "whilestmt ->  while ( boolexpr ) stmt", "assgstmt ->  ID = arithexpr ", "boolexpr  ->  arithexpr boolop arithexpr", "boolop ->   <  |  >  |  <=  |  >=  | ==", "arithexpr  ->  multexpr arithexprprime", "arithexprprime ->  + multexpr arithexprprime  |  - multexpr arithexprprime  |   E", "multexpr ->  simpleexpr  multexprprime", "multexprprime ->  * simpleexpr multexprprime  |  / simpleexpr multexprprime  |   E", "simpleexpr ->  ID  |  NUM  |  ( arithexpr )"};
    private static String[] prod2 = {"program -> compoundstmt ", "stmt -> ifstmt ", "stmt -> whilestmt ", "stmt -> assgstmt ", "stmt -> compoundstmt ", "compoundstmt -> { stmts } ", "stmts -> stmt stmts ", "stmts -> E ", "ifstmt -> if ( boolexpr ) then stmt else stmt ", "whilestmt -> while ( boolexpr ) stmt ", "assgstmt -> ID = arithexpr ; ", "boolexpr -> arithexpr boolop arithexpr ", "boolop -> < ", "boolop -> > ", "boolop -> <= ", "boolop -> >= ", "boolop -> == ", "arithexpr -> multexpr arithexprprime ", "arithexprprime -> + multexpr arithexprprime ", "arithexprprime -> - multexpr arithexprprime ", "arithexprprime -> E ", "multexpr -> simpleexpr multexprprime ", "multexprprime -> * simpleexpr multexprprime ", "multexprprime -> / simpleexpr multexprprime ", "multexprprime -> E ", "simpleexpr -> ID ", "simpleexpr -> NUM ", "simpleexpr -> ( arithexpr ) "};
    private static String[][] proSym;
    private static HashMap<String, Set<String>> First = new HashMap<>();
    private static HashMap<String, Set<String>> Follow = new HashMap<>();
    private static HashMap<Integer, HashMap<String, Set>> Canonical_LR = new HashMap<>();
    private static void read_prog()
    {
        Scanner sc = new Scanner(System.in);
        while(sc.hasNextLine())
        {
            String nextLine = sc.nextLine().trim();
        }
    }
    private static void computeFirst(){
        for(String ter: Terminals)
            First.put(ter, new HashSet<>(Collections.singleton(ter)));
        for(String product: production){
            String[] symbols = product.split("\\s+");
            if(!First.containsKey(symbols[0])) First.put(symbols[0], new HashSet<>());
            for(int i = 2; i < symbols.length; i++){
                if(Objects.equals(symbols[i], "E")) First.get(symbols[0]).add("E");
                else if(Terminals.contains(symbols[i]) || nonTerminals.contains(symbols[i])){
                    if(i > 2 && !Objects.equals(symbols[i - 1], "|")) continue;
                    First.get(symbols[0]).add(symbols[i]);
                }
            }
        }
        for(String key: First.keySet()){
            Set<String> nont = First.get(key);
            Deque<String> newNont = new LinkedList<>();
            for(String s: nont)
                if(nonTerminals.contains(s) && !Objects.equals(s, key)) newNont.add(s);
            while(!newNont.isEmpty()){
                String ns = newNont.pop();
                nont.remove(ns);
                for(String s: First.get(ns)){
                    if(nonTerminals.contains(s) && !Objects.equals(s, key)) newNont.add(s);
                    else nont.add(s);
                }
            }
        }
    }
    private static void computeFollow(){
        for(String product: production){
            String[] symbols = product.split("\\s+");
            if(!Follow.containsKey(symbols[0]))
                Follow.put(symbols[0], new HashSet<>(Collections.singleton("$")));
            else
                Follow.get(symbols[0]).add("$");
            for(int i = 2; i < symbols.length; i++)
                if(nonTerminals.contains(symbols[i]) && !Follow.containsKey(symbols[i])) Follow.put(symbols[i], new HashSet<>());
            for(int i = 2; i < symbols.length; i++){
                if(nonTerminals.contains(symbols[i])){
                    if(i + 1 == symbols.length) Follow.get(symbols[i]).add("E");
                    for(int j = i + 1; j < symbols.length; j++){
                        if(Objects.equals(symbols[j], "|")) break;
                        Follow.get(symbols[i]).addAll(First.get(symbols[j]));
                        if(!First.get(symbols[j]).contains("E")) break;
                    }
                    if(Follow.get(symbols[i]).contains("E")) {
                        if(!Objects.equals(symbols[0], symbols[i])) Follow.get(symbols[i]).add(symbols[0]);
                        Follow.get(symbols[i]).remove("E");
                    }
                }
            }
        }
        for(String key: Follow.keySet()){
            Set<String> nont = Follow.get(key);
            String start = "start";
            while(start != null) {
                start = null;
                for(String s: nont)
                    if(nonTerminals.contains(s) && !Objects.equals(s, key)) start = s;
                nonTerminals.remove(start);
                if(start != null) nont.addAll(Follow.get(start));
            }
        }
    }
    private static void canonical() {
        nonTerminals = new ArrayList<>(Arrays.asList("program", "compoundstmt", "stmt", "stmts", "ifstmt", "whilestmt", "assgstmt", "boolexpr", "arithexpr", "multexpr", "boolop", "arithexprprime", "multexprprime", "simpleexpr"));
        proSym = new String[prod2.length][];
        int cnt = 0;
        for (String s : prod2) {
            proSym[cnt++] = s.split("\\s+");
        }
        int flag = 0;
        HashMap<String, Set> i0 = new HashMap<>();
        Set<String> lookHead = new HashSet<>();
        lookHead.add("$");
        i0.put("p' -> . program", lookHead);
        Canonical_LR.put(flag, i0);
        Queue<String[]> closure = new LinkedList<>();
        for (String s : i0.keySet()) {
            String[] sym = s.split("\\s+");
            for (int i = 0; i < sym.length; i++) {
                if (Objects.equals(sym[i], ".")) {
                    StringBuilder beta = new StringBuilder();
                    if (i + 2 < sym.length) {
                        beta.append(sym[i + 2]);
                        for (int j = i + 3; j < sym.length; j++)
                            if (First.getOrDefault(sym[j], new HashSet<>()).contains("E")) beta.append(sym[j]);
                    }
                    closure.offer(new String[]{sym[i + 1], String.valueOf(beta)});
                    break;
                }
            }
        }
        while (!closure.isEmpty()) {
            String[] s = closure.poll();
            Set<String> newLookHead = new HashSet<>();
            if (nonTerminals.contains(s[0])) {
                System.out.println(s[0]);
                for (String[] start : proSym) {
                    String lr1 = "";
                    if (Objects.equals(start[0], s[0])) {
                        lr1 += start[0] + " " + start[1] + " " + "." + " ";
                        for (int i = 2; i < start.length; i++)
                            lr1 += start[i] + " ";
                    }
                    if (First.getOrDefault(s[1], new HashSet<>()).contains("E")) {
                        i0.put(lr1, Collections.singleton(i0.getOrDefault(lr1, new HashSet<>()).addAll(new HashSet<>(lookHead).addAll(First.get(s[1])))));
                    } else {
                        i0.put(lr1, Collections.singleton(i0.getOrDefault(lr1, new HashSet<>()).addAll(First.get(s[1]))));
                    }
                    newLookHead.addAll(i0.get(lr1));
                    String[] sym = lr1.split("\\s+");
                    for (int i = 0; i < sym.length; i++) {
                        if (Objects.equals(sym[i], ".")) {
                            StringBuilder beta = new StringBuilder();
                            if (i + 2 < sym.length) {
                                beta.append(sym[i + 2]);
                                for (int j = i + 3; j < sym.length; j++)
                                    if (First.getOrDefault(sym[j], new HashSet<>()).contains("E")) beta.append(sym[j]);
                            }
                            closure.offer(new String[]{sym[i + 1], String.valueOf(beta)});
                            break;
                        }
                    }
                }
                lookHead = newLookHead;
            }
        }
        System.out.println(i0);
    }
    private static void PAT(){ //create the parsing action table

    }
    private static void PGT(){ //create the parsing goto table

    }
    private static void analysis()
    {
//        read_prog();
        computeFirst();
        computeFollow();
        canonical();

    }
    public static void main(String[] args) {
        analysis();
    }
}