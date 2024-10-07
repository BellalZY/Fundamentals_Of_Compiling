import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class Java_LexAnalysis {
    private static int cnt = 1;
    private static StringBuffer prog = new StringBuffer();
    private static HashMap<String, Integer> hashmap = new HashMap<>();
    private static Map<String[], Integer> outputMap = new LinkedHashMap<>();
    private static void read_prog()
    {
        Scanner sc = new Scanner(System.in);
        while(sc.hasNextLine()) {
            output(sc.nextLine());
        }
        Iterator it = outputMap.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry entry = (Map.Entry)it.next();
            String[] res = (String[])entry.getKey();
            prog.append(res[1] + ": <" + res[0] + "," + entry.getValue() + ">");
            if(it.hasNext()) prog.append("\n");
        }
    }
    private static void output(String line){
        line = line.trim();
        int count = 0, flag = 0;
        String key;
        String pattern1 = "[a-zA-Z0-9_]";
        String pattern2 = "[a-zA-Z]";
        String pattern3 = "[0-9.]";
        while(count < line.length()){
            flag = count;
            if(count > 0 && line.charAt(count) == '"'){
                while(count < line.length() && line.charAt(count + 1) != '"') count++;
                String[] newKey1 = {"\"", String.valueOf(cnt++)};
                key = line.substring(flag + 1, count + 1);
                String[] newKey2 = {key, String.valueOf(cnt++)};
                String[] newKey3 = {"\"", String.valueOf(cnt++)};
                count += 2;
                outputMap.put(newKey1, hashmap.get("\""));
                outputMap.put(newKey2, 81);
                outputMap.put(newKey3, hashmap.get("\""));
                continue;
            }
            if(line.charAt(count) == '/'){
                if(line.charAt(count + 1) == '/'){
                    key = line.substring(count);
                    String[] newKey = {key, String.valueOf(cnt)};
                    cnt++;
                    outputMap.put(newKey, 79);
                    break;
                }
                if(line.charAt(count + 1) == '*'){
                    while(count + 1 < line.length() && line.charAt(count + 1) != '/') count++;
                    count += 2;
                    key = line.substring(flag, count);
                    String[] newKey = {key, String.valueOf(cnt)};
                    cnt++;
                    outputMap.put(newKey, 81);
                    continue;
                }
            }
            if(Pattern.matches(pattern1,Character.toString(line.charAt(count)))){
                while(count < line.length() && Pattern.matches(pattern3,Character.toString(line.charAt(count)))) count++;
                if(flag != count){
                    key = line.substring(flag, count);
                    String[] newKey = {key, String.valueOf(cnt)};
                    cnt++;
                    outputMap.put(newKey, 80);
                    continue;
                }
                while(count < line.length() && Pattern.matches(pattern2,Character.toString(line.charAt(count)))) count++;
                if(flag != count){
                    key = line.substring(flag, count);
                    if(hashmap.containsKey(key)) {
                        String[] newKey = {key, String.valueOf(cnt)};
                        cnt++;
                        outputMap.put(newKey, hashmap.get(key));
                        continue;
                    }
                }
                while(count < line.length() && Pattern.matches(pattern1,Character.toString(line.charAt(count)))) count++;
                if(flag != count){
                    key = line.substring(flag, count);
                    String[] newKey = {key, String.valueOf(cnt)};
                    cnt++;
                    outputMap.put(newKey, 81);
                }
            }
            else{
                while(count < line.length() && line.charAt(count) != ' ' && !Pattern.matches(pattern1,Character.toString(line.charAt(count))) && line.charAt(count) != '/' && line.charAt(count) != '"') count++;
                if(flag != count){
                    key = line.substring(flag, count);
                    if(hashmap.containsKey(key)){
                        String[] newKey = {key, String.valueOf(cnt)};
                        cnt++;
                        outputMap.put(newKey, hashmap.get(key));
                    }
                    else{
                        for(int i = flag; i != count; i++) {
                            if (i + 1 < count && hashmap.containsKey(line.substring(i, i + 2))) {
                                String[] newKey = {key, String.valueOf(cnt)};
                                cnt++;
                                outputMap.put(newKey, hashmap.get(key));
                                i++;
                            }else{
                                key = line.substring(i, i + 1);
                                if (hashmap.containsKey(key)) {
                                    String[] newKey = {key, String.valueOf(cnt)};
                                    cnt++;
                                    outputMap.put(newKey, hashmap.get(key));
                                }
                            }
                        }
                    }
                }
            }
            while(count < line.length() && line.charAt(count) == ' ') count++;
        }
    }
    // make a map from c_keys
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
        System.out.print(prog);
    }
    public static void main(String[] args) throws IOException {
        makeMap();
        analysis();
    }
}