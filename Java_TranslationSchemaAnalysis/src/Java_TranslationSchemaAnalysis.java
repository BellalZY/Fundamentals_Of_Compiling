import java.util.*;
import java.util.regex.Pattern;

public class Java_TranslationSchemaAnalysis
{
    private static ArrayList<String> Terminals = new ArrayList<>(Arrays.asList("{", "}", "if", "while", "(", ")", "then", "else", "ID", "INTNUM", "REALNUM", "int", "real",";", "=", ">", "<", ">=", "<=", "==", "+", "-", "*", "/", "E"));
    private static ArrayList<String> nonTerminals = new ArrayList<>(Arrays.asList("program", "compoundstmt", "decl", "decls","stmt", "stmts", "ifstmt", "whilestmt", "assgstmt", "boolexpr", "arithexpr", "multexpr", "boolop", "arithexprprime", "multexprprime", "simpleexpr"));
    private static String[] production = {"program -> decls compoundstmt", "decls -> decl ; decls | E", "decl -> int ID = INTNUM | real ID = REALNUM", "stmt -> ifstmt | assgstmt | compoundstmt", "compoundstmt -> { stmts }", "stmts -> stmt stmts | E", "ifstmt -> if ( boolexpr ) then stmt else stmt","assgstmt -> ID = arithexpr ;","boolexpr -> arithexpr boolop arithexpr","boolop -> < | > | <= | >= | ==","arithexpr -> multexpr arithexprprime","arithexprprime -> + multexpr arithexprprime | - multexpr arithexprprime | E","multexpr -> simpleexpr multexprprime","multexprprime -> * simpleexpr multexprprime | / simpleexpr multexprprime | E","simpleexpr -> ID | INTNUM | REALNUM | ( arithexpr ) } ;"};
    private static HashMap<String, Set<String>> First = new HashMap<>();
    private static HashMap<String, Set<String>> Follow = new HashMap<>();
    private static HashMap<String, HashMap<String, String>> ll1 = new HashMap<>();
    private static Deque<String> stack = new LinkedList<>();
    private static Deque<String> startSym = new LinkedList<>();
    private static int line = 0;
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
    private static boolean isNotDigit(String sym){
        for(int j = 0; j < sym.length(); j++)
            if(!Character.isDigit(sym.charAt(j)))
                return true;
        return false;
    }
    private static void read_prog()
    {
        Scanner sc = new Scanner(System.in);
        while(sc.hasNextLine())
        {
            String readLine = sc.nextLine().trim();
            if(readLine.equals("")) continue;
            line++;
            String[] sym = readLine.split("\\s+");
            for(int i = 0; i < sym.length; i++) {
                if(Objects.equals(sym[i], "int") && isNotDigit(sym[i + 3]))
                    System.out.println("error message:line " + line + ",realnum can not be translated into int type");
                else if(Objects.equals(sym[i], "/") && Objects.equals(sym[i + 1], "0")){
                    System.out.println("error message:line " + line + ",division by zero");
                    stack.clear();
                    break;
                }
                else stack.offer(sym[i]);
            }
        }
    }
    private static void dfs(Deque<String> result, HashMap<String, String[]> ids){
        int flag = 0;
        while(!result.isEmpty()){
            String node = result.pop();
            String res = stack.pop();
            if(Objects.equals(res, "int") && !Terminals.contains(stack.peek())){
                String var = stack.pop();
                stack.pop();
                String val = stack.pop();
                stack.push("INTNUM");
                stack.push("=");
                stack.push("ID");
                ids.put(var, new String[]{val, "Int"});
                stack.push("int");
            }
            else if(Objects.equals(res, "real") && !Terminals.contains(stack.peek())){
                String var = stack.pop();
                stack.pop();
                String val = stack.pop();
                stack.push("REALNUM");
                stack.push("=");
                stack.push("ID");
                ids.put(var, new String[]{val, "Real"});
                stack.push("real");
            }
            else stack.push(res);
            if(ids.containsKey(res) || Objects.equals(res, "then") || Objects.equals(res, "else")){
                String cond = null, s1 = null, s2 = null, sym;
                if(Objects.equals(res, "then") || Objects.equals(res, "else")) cond = stack.pop();
                res = stack.pop();
                Deque<String> newStack = new LinkedList<>();
                Deque<String> newStack2 = new LinkedList<>();
                if(Objects.equals(stack.peek(), "=")){
                    stack.pop();
                    if(ids.containsKey(stack.peek())) s1 = ids.get(stack.pop())[0];
                    else s1 = stack.pop();
                    sym = stack.pop();
                    if(ids.containsKey(stack.peek())) s2 = ids.get(stack.pop())[0];
                    else s2 = stack.pop();
                    while(Objects.equals(stack.peek(), "+") || Objects.equals(stack.peek(), "-") || Objects.equals(stack.peek(), "*") || Objects.equals(stack.peek(), "/")){
                        String sym2 = stack.pop();
                        newStack2.push(sym2);
                        newStack2.push(stack.pop());
                        newStack.push(sym2);
                        newStack.push("INTNUM");
                    }
                    for(String ss : newStack) stack.push(ss);
                    stack.push("ID");
                    stack.push(sym);
                    stack.push("ID");
                    stack.push("=");
                }
                else {
                    sym = stack.pop();
                    String res2 = stack.pop();
                    if(Objects.equals(sym, "==")){
                        if(!Objects.equals(ids.get(res)[0], ids.get(res2)[0])) flag = -1;
                        else flag = 1;
                    }
                    if(Objects.equals(sym, ">=")){
                        if(Double.parseDouble(ids.get(res)[0]) <= Double.parseDouble(ids.get(res2)[0])) flag = -1;
                        else flag = 1;
                    }
                    if(Objects.equals(sym, "<=")){
                        if(Double.parseDouble(ids.get(res)[0]) >= Double.parseDouble(ids.get(res2)[0])) flag = -1;
                        else flag = 1;
                    }
                    if(Objects.equals(sym, "<")){
                        if(Double.parseDouble(ids.get(res)[0]) > Double.parseDouble(ids.get(res2)[0])) flag = -1;
                        else flag = 1;
                    }
                    if(Objects.equals(sym, "<")){
                        if(Double.parseDouble(ids.get(res)[0]) > Double.parseDouble(ids.get(res2)[0])) flag = -1;
                        else flag = 1;
                    }

                }
                stack.push("ID");
                if(cond != null) stack.push(cond);
                if(flag == 0 ||(flag == 1 && Objects.equals(cond, "then")) ||(flag == -1 && Objects.equals(cond, "else"))){
                    if(Objects.equals(ids.get(res)[1], "Real")){
                        switch(sym){
                            case "+": ids.put(res, new String[]{Double.toString(Double.parseDouble(s1) + Double.parseDouble(s2)),"Real"}); break;
                            case "-": ids.put(res, new String[]{Double.toString(Double.parseDouble(s1) - Double.parseDouble(s2)),"Real"}); break;
                            case "*": ids.put(res, new String[]{Double.toString(Double.parseDouble(s1) * Double.parseDouble(s2)),"Real"}); break;
                            case "/": ids.put(res, new String[]{Double.toString(Double.parseDouble(s1) / Double.parseDouble(s2)),"Real"}); break;
                        }
                        while(!newStack2.isEmpty()){
                            String num = newStack2.pop();
                            String sym2 = newStack2.pop();
                            switch(sym2){
                                case "+": ids.put(res, new String[]{Double.toString(Double.parseDouble(ids.get(res)[0]) + Double.parseDouble(num)),"Real"}); break;
                                case "-": ids.put(res, new String[]{Double.toString(Double.parseDouble(ids.get(res)[0]) - Double.parseDouble(num)),"Real"}); break;
                                case "*": ids.put(res, new String[]{Double.toString(Double.parseDouble(ids.get(res)[0]) * Double.parseDouble(num)),"Real"}); break;
                                case "/": ids.put(res, new String[]{Double.toString(Double.parseDouble(ids.get(res)[0]) / Double.parseDouble(num)),"Real"}); break;
                            }
                        }
                    }
                    else if(Objects.equals(ids.get(res)[1], "Int"))
                        switch(sym){
                                case "+": ids.put(res, new String[]{Integer.toString(Integer.parseInt(s1) + Integer.parseInt(s2)),"Int"}); break;
                                case "-": ids.put(res, new String[]{Integer.toString(Integer.parseInt(s1) - Integer.parseInt(s2)),"Int"}); break;
                                case "*": ids.put(res, new String[]{Integer.toString(Integer.parseInt(s1) * Integer.parseInt(s2)),"Int"}); break;
                                case "/": ids.put(res, new String[]{Integer.toString(Integer.parseInt(String.valueOf(Integer.parseInt(s1) / Integer.parseInt(s2)))),"Int"}); break;
                            }
                        while(!newStack2.isEmpty()){
                            String num = newStack2.pop();
                            String sym2 = newStack2.pop();
                            switch(sym2){
                                case "+": ids.put(res, new String[]{Integer.toString(Integer.parseInt(ids.get(res)[0]) + Integer.parseInt(num)),"Int"}); break;
                                case "-": ids.put(res, new String[]{Integer.toString(Integer.parseInt(ids.get(res)[0]) - Integer.parseInt(num)),"Int"}); break;
                                case "*": ids.put(res, new String[]{Integer.toString(Integer.parseInt(ids.get(res)[0]) * Integer.parseInt(num)),"Int"}); break;
                                case "/": ids.put(res, new String[]{Integer.toString(Integer.parseInt(String.valueOf(Integer.parseInt(ids.get(res)[0]) / Integer.parseInt(num)))),"Int"}); break;
                            }
                        }
                }
                if(Objects.equals(cond, "else")) flag = 0;
            }
            res = stack.peek();
            if(Objects.equals(res, node)) stack.pop();
            else if(ll1.containsKey(node)){
                if(ll1.get(node).containsKey(res)){
                    String[] sym = ll1.get(node).get(res).split("\\s+");
                    for(int i = sym.length - 1; i >= 0; i--) result.push(sym[i]);
                }
                else if(ll1.get(node).containsKey("E")) ;
            }
        }
        for(String s: ids.keySet()) {
            System.out.println(s + ": " + ids.get(s)[0]);
        }
    }
    private static void analysis()
    {
        read_prog();
        computeFirst();
        computeFollow();
        computeLL1();
        Deque<String> result = new LinkedList<>();
        HashMap<String, String[]> ids = new HashMap<>();
        result.push("program");
        if(!stack.isEmpty()) dfs(result, ids);
    }
    public static void main(String[] args) {
        analysis();
    }
}
