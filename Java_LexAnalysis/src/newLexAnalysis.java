import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class newLexAnalysis {
    private enum state{
        reserved,number,indentifier,mark
    }
    private enum charType{
        leftSlash,star,quotation,pattern,symbol,blank
    }
    private static final String pattern1 = "[a-zA-Z0-9_]";
    private static final String pattern2 = "[a-zA-Z]";
    private static final String pattern3 = "[0-9.]*";
    private static final HashMap<String, Integer> hashmap = new HashMap<>();
    private static final Map<state, Map<charType, state>> stateMap= new HashMap<>();
    private static List<String> keyList = new ArrayList<>();
    private static charType dealChar(char c){
        if(c == ' ') return charType.blank;
        if(c == '"') return charType.quotation;
        if(c == '/') return charType.leftSlash;
        if(c == '*') return charType.star;
        if(Pattern.matches(pattern1,Character.toString(c))) return charType.pattern;
        else return charType.symbol;
    }
    private static state dealState(String key){
        int grade = hashmap.get(key) != null ? hashmap.get(key):(Pattern.matches(pattern3,key) ? 80 :(key.charAt(0) == '/' ? 79 : 81));
        switch(grade){
            case 81:
                return state.indentifier;
            case 79:
                return state.mark;
            case 80:
                return state.number;
            default:
                return state.reserved;
        }
    }
    private static void read_prog()
    {
        Scanner sc = new Scanner(System.in);
        while(sc.hasNextLine()) {
            parseStr(sc.nextLine());
        }
    }
    public static void parseStr(String line){
        line = line.trim();
        int count = 0, flag = 0;
        String key1, key2;
        while(count < line.length()){
            flag = count;
            switch(dealChar(line.charAt(count))){
                case quotation:
                    while(count < line.length() && dealChar(line.charAt(count + 1)) != charType.quotation) count++;
                    key1 = "\"";
                    key2 = line.substring(flag + 1, count + 1);
                    keyList.add(key1);
                    keyList.add(key2);
                    keyList.add(key1);
                    count += 2;
                    break;
                case leftSlash:
                    if(dealChar(line.charAt(count + 1)) == charType.leftSlash){
                        key1 = line.substring(count);
                        keyList.add(key1);
                        count = line.length();
                    }
                    else if(dealChar(line.charAt(count + 1)) == charType.star){
                        while(count + 1 < line.length() && line.charAt(count + 1) != '/') count++;
                        count += 2;
                        key1 = line.substring(flag, count);
                        keyList.add(key1);
                    }
                    break;
                case pattern:
                    while(count < line.length() && Pattern.matches(pattern3,Character.toString(line.charAt(count)))) count++;
                    if(flag != count){
                        key1 = line.substring(flag, count);
                        keyList.add(key1);
                        break;
                    }
                    while(count < line.length() && Pattern.matches(pattern2,Character.toString(line.charAt(count)))) count++;
                    key1 = line.substring(flag, count);
                    if(flag != count && hashmap.containsKey(key1)){
                        keyList.add(key1);
                        break;
                    }
                    while(count < line.length() && Pattern.matches(pattern1,Character.toString(line.charAt(count)))) count++;
                    if(flag != count){
                        key1 = line.substring(flag, count);
                        keyList.add(key1);
                    }
                    break;
                case blank:
                    count++;
                    break;
                default:
                    while(count < line.length() && dealChar(line.charAt(count)) != charType.blank && dealChar(line.charAt(count)) != charType.pattern && dealChar(line.charAt(count)) != charType.quotation && dealChar(line.charAt(count)) != charType.leftSlash) count++;
                    if(flag != count){
                        key1 = line.substring(flag, count);
                        if(flag != count && hashmap.containsKey(key1)){
                            keyList.add(key1);
                            continue;
                        }
                        else{
                            for(int i = flag; i != count; i++) {
                                if (i + 1 < count && hashmap.containsKey(line.substring(i, i + 2))) {
                                    key1 = line.substring(i, i + 2);
                                    keyList.add(key1);
                                    i++;
                                }
                                else{
                                    key1 = line.substring(i, i + 1);
                                    if (hashmap.containsKey(key1)) {keyList.add(key1);}
                                }
                            }
                        }
                    }
                    break;
            }
        }
    }
    public static void makeMap() throws IOException {
        String dir = "./c_keys.txt";
        File file = new File(dir);
        if(!file.exists())
            file.createNewFile();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while((line = br.readLine()) != null){
            dealLine(line);
        }
        hashmap.put("//", 79);
        br.close();
    }
    private static void dealLine(String line){
        line = line.trim();
        int pos1 = 0, pos2 = line.length() - 1;
        for(; pos1 < line.length(); pos1++)
            if(line.charAt(pos1) == ' ') break;
        for(; pos2 >= 0; pos2--)
            if(line.charAt(pos2) == ' ') break;
        String key = line.substring(0, pos1);
        int value = Integer.parseInt(line.substring(pos2 + 1));
        hashmap.put(key, value);
    }
    private static void analysis()
    {
        read_prog();
        int cnt = 0;
        Iterator<String> it = keyList.iterator();
        while(it.hasNext()){
            String key = it.next();
            switch(dealState(key)){
                case reserved:
                    System.out.print(++cnt + ": <" + key + "," + hashmap.get(key) + ">");
                    break;
                case mark:
                    System.out.print(++cnt + ": <" + key + "," + 79 + ">");
                    break;
                case number:
                    System.out.print(++cnt + ": <" + key + "," + 80 + ">");
                    break;
                case indentifier:
                    System.out.print(++cnt + ": <" + key + "," + 81 + ">");
                    break;
            }
            if(it.hasNext()) System.out.println();
        }
    }
    public static void main(String[] args) throws IOException {
        makeMap();
        analysis();
    }
}
