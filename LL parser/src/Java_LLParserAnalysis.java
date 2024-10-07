import java.util.*;

public class Java_LLParserAnalysis
{
    private static ArrayList<String> Terminals = new ArrayList<>(Arrays.asList("{", "}", "if", "while", "(", ")", "then", "else", "ID", "=", ">", "<", ">=", "<=", "==", "+", "-", "*", "/", "NUM", "E"));
    private static ArrayList<String> nonTerminals = new ArrayList<>(Arrays.asList("program", "compoundstmt", "stmt", "stmts", "ifstmt", "whilestmt", "assgstmt", "boolexpr", "arithexpr", "multexpr", "boolop", "arithexprprime", "multexprprime", "simpleexpr"));
    private static String[] production = {"program -> compoundstmt", "stmt ->  ifstmt  |  whilestmt  |  assgstmt  |  compoundstmt", "compoundstmt ->  { stmts }", "stmts ->  stmt stmts   |   E", "ifstmt ->  if ( boolexpr ) then stmt else stmt", "whilestmt ->  while ( boolexpr ) stmt", "assgstmt ->  ID = arithexpr ", "boolexpr  ->  arithexpr boolop arithexpr", "boolop ->   <  |  >  |  <=  |  >=  | ==", "arithexpr  ->  multexpr arithexprprime", "arithexprprime ->  + multexpr arithexprprime  |  - multexpr arithexprprime  |   E", "multexpr ->  simpleexpr  multexprprime", "multexprprime ->  * simpleexpr multexprprime  |  / simpleexpr multexprprime  |   E", "simpleexpr ->  ID  |  NUM  |  ( arithexpr )"};
    private static HashMap<String, Set<String>> First = new HashMap<>();
    private static HashMap<String, Set<String>> Follow = new HashMap<>();
    private static HashMap<String, HashMap<String, String>> ll1 = new HashMap<>();
    private static Deque<String[]> stack = new LinkedList<>();
    private static Deque<String> startSym = new LinkedList<>();
    private static int line = 0;
    private static void read_prog()
    {
        Scanner sc = new Scanner(System.in);
        while(sc.hasNextLine())
        {
            String readLine = sc.nextLine().trim();
            if(readLine.equals("")) continue;
            line++;
            String[] sym = readLine.split("\\s+");
            for (String s : sym) stack.offer(new String[]{s, Integer.toString(line)});
            if(!Objects.equals(sym[0], "{") && !Objects.equals(sym[0], "}") && !Objects.equals(sym[0], "if") && !Objects.equals(sym[0], "while") && !Objects.equals(sym[0], "then") && !Objects.equals(sym[0], "else")) {
                startSym.offer(sym[0]);
                startSym.offer(Integer.toString(line));
                if(!Objects.equals(sym[sym.length - 1], ";")){
                    System.out.println("语法错误,第" + line + "行,缺少\";\"");
                    stack.offer(new String[]{";", Integer.toString(line)});
                }
            }
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
    private static void computeLL1(){
        for(String product: production){
            String[] symbols = product.split("\\s*->\\s*");
            String[] action = symbols[1].split("\\s*\\|\\s*");
            ll1.put(symbols[0], new HashMap<>());
            for(String act: action){
                if(!First.containsKey(act)){
                    First.put(act, new HashSet<>());
                    String[] sym = act.split("\\s+");
                    for(String s: sym){
                        First.get(act).addAll(First.get(s));
                        if(!First.get(s).contains("E")) break;
                    }
                }
                for(String a: First.get(act)){
                    if(Terminals.contains(a))
                        ll1.get(symbols[0]).put(a, act);
                    if(Objects.equals(a, "E")){
                        for(String b: Follow.get(symbols[0]))
                            if(Terminals.contains(b))
                                ll1.get(symbols[0]).put(b, act);
                    }
                    if(Objects.equals(a, "E") && Follow.get(symbols[0]).contains("$"))
                        ll1.get(symbols[0]).put("$", act);
                }
            }
        }
    }
    private static void analysis()
    {
        computeFirst();
        computeFollow();
        computeLL1();
        read_prog();
        Deque<String> result = new LinkedList<>();
        result.push("program");
        int count = 0;
        dfs(count, result);
    }
    private static void dfs(int count,Deque<String> result){
        while(!result.isEmpty()){
            String node = result.pop();
            String[] res = stack.peek();
            if(!startSym.isEmpty() && Objects.equals(node, startSym.peek())) {
                startSym.pop();
                if(Objects.equals(startSym.peek(), res[1])){
                    startSym.pop();
                    result.offer(";");
                }
                else startSym.push(node);
            }
            if(Objects.equals(res[0], node)) stack.pop();
            for(int i = 0; i < count; i++) System.out.print("\t");
            System.out.println(node);
            if(ll1.containsKey(node)){
                if(ll1.get(node).containsKey(res[0])){
                    String[] sym = ll1.get(node).get(res[0]).split("\\s+");
                    Deque<String> result2 = new LinkedList<>();
                    for(int i = sym.length - 1; i >= 0; i--) result2.push(sym[i]);
                    dfs(count + 1, result2);
                }
                else if(ll1.get(node).containsKey("E")) {
                    for(int i = 0; i <= count; i++) System.out.print("\t");
                    System.out.println("E");
                }
            }
        }
    }
    public static void main(String[] args) {
        analysis();
    }
}
