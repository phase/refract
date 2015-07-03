import java.io.*;
import java.util.Random;

public class Refract {

    int sid = 0; // The current StackID
    int wait = 0000; // Time to wait between commands in milliseconds
    int stacksize = 64 * 1024; // The size of the stack
    char quote = 0; // The current quote, ' or "
    int[] pos = { -1, 0 }; // The current position
    double register = 0.0; // The value in the register
    boolean reg = false; // Boolean whether the register is set or not
    boolean run = true; // Run while this is true
    boolean skip = false; // When a command has to be skipped
    boolean tick = false; // When I have to tick even on NOP
    boolean debug = false; // Debugging mode
    boolean string = false; // When string is turned on
    Stack[] stacks = new Stack[1024]; // The array of stacks
    char[][] grid = null; // The grid from the file
    Directions dir = Directions.RIGHT; // The current direction
    StringBuilder sb = new StringBuilder(); // The StringBuilder for the string

    public Refract(String[] args) {
        String filename = args.length >= 1 ? args[0] : null;
        String[] data = null;
        stacks[sid] = new Stack(stacksize, this);
        boolean isFile = true;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
            case "-h":
                showHelp();
                return;
            case "-c":
                isFile = false;
                data = args[++i].split("\n");
                break;
            case "-s":
            case "--string":
                String s = args[++i];
                for (char c : s.toCharArray()) {
                    stacks[sid].push(c);
                }
                break;
            case "-v":
            case "--value":
                i++;
                while (i < args.length && isNumber(args[i])) {
                    stacks[sid].push(Double.parseDouble(args[i]));
                    i++;
                }
                i--;
                break;
            case "-t":
            case "--time":
                if (isNumber(args[++i])) {
                    wait = (int) Math.round(Double.parseDouble(args[i]) * 1000);
                }
                break;
            case "-a":
            case "--always-tick":
                tick = true;
                break;
            case "-X":
                debug = true;
                break;
            default:
                filename = args[i];
                break;
            }
        }
        if (isFile) {
            if (filename == null) {
                showHelp();
                System.exit(-1);
                return;
            }
            FileIO fio = new FileIO(filename);
            try {
                data = fio.readData();
            }
            catch (NullPointerException | FileNotFoundException e) {
                System.out.println("File '" + filename + "' not found in currrent directory!");
                e.printStackTrace(System.out);
                return;
            }
            catch (IOException ioe) {
                System.out.println("An unexpected error occured:");
                ioe.printStackTrace(System.out);
                return;
            }
        }
        if (data == null) {
            System.out.println("No code provided!");
            System.exit(-1);
            return;
        }
        if (debug) {
            for (String s : data) {
                System.out.println(s);
            }
        }
        grid = new char[data.length][];
        int i = 0;
        for (String line : data) {
            grid[i++] = line.toCharArray();
        }
        int top = grid[0].length;
        for (char[] line : grid) {
            if (line.length > top) {
                top = line.length;
            }
        }
        for (int a = 0; a < grid.length; a++) {
            char[] line = grid[a];
            char[] newline = new char[top];
            int b = 0;
            for (char c : line) {
                newline[b++] = c;
            }
            grid[a] = newline;
        }
        try {
            while (run) {
                pos = dir.move(pos);
                check();
                if (debug) stacks[sid].print();
                if (skip) {
                    skip = false;
                    continue;
                }
                parse();
                char c = grid[pos[1]][pos[0]];
                boolean isNOP = (c == ' ' || c == 0);
                if (tick || !isNOP) {
                    sleep(wait);
                }
            }
        }
        catch (IOException ioe) {
            System.out.println("An unexpected error happened: ");
            ioe.printStackTrace();
            return;
        }
        System.exit(0);
        return;
    }

    public void showHelp() {
        System.out.println("usage: java Refract [-h] (<script file> | -c <code>) [<options>]");
        System.out.println();
        System.out.println("    Execute a refract script.");
        System.out.println();
        System.out.println("    Executing a script is as easy as:");
        System.out.println("        java Refract <script file>");
        System.out.println();
        System.out.println("    You can also execute code directly using the -c/--code flag:");
        System.out.println("        java Refract -c '1n23nn;'");
        System.out.println("        > 132");
        System.out.println();
        System.out.println("    The -v and -s flags can be used to prepopulate the stack:");
        System.out.println("        java Refract echo.fish -s \"hello, world\" -v 32 49 50 51 -s \"456\"");
        System.out.println("        > hello, world 123456");
        System.out.println();
        System.out.println("optional arguments:");
        System.out.println("  -h, --help            show this help message and exit");
        System.out.println();
        System.out.println("code:");
        System.out.println("  script                .refract file to execute");
        System.out.println("  -c <code>, --code <code>");
        System.out.println("                        string of instructions to execute");
        System.out.println();
        System.out.println("options:");
        System.out.println("  -s <string>, --string <string>");
        System.out.println("  -v <number> [<number> ...], --value <number> [<number> ...]");
        System.out.println("                        push numbers or strings onto the stack before");
        System.out.println("                        execution starts");
        System.out.println("  -t <seconds>, --tick <seconds>");
        System.out.println("                        define a tick time, or a delay between the execution");
        System.out.println("                        of each instruction");
        System.out.println("  -a, --always-tick     make every instruction cause a tick (delay), even");
        System.out.println("                        whitespace and skipped instructions");
    }

    public void sleep(int time) {
        try {
            Thread.sleep(time);
        }
        catch (Exception e) {}
    }

    public void check() {
        int y = pos[1], maxy = grid.length - 1;
        if (y < 0) {
            y = maxy;
        }
        if (y > maxy) {
            y = 0;
        }
        int x = pos[0], maxx = grid[y].length - 1;
        if (x < 0) {
            x = maxx;
        }
        if (x > maxx) {
            x = 0;
        }
        pos = new int[] { x, y };
    }

    public void parse() throws IOException {
        char c = grid[pos[1]][pos[0]];
        if (debug) System.out.println(c);
        if (c == 3) {
            System.exit(0);
        }
        else if (c == '"' || c == '\'') {
            if (string) {
                if (quote == c) {
                    String s = sb.toString();
                    // Reset the builder
                    sb = new StringBuilder();
                    if (debug) System.out.println("STRING " + quote + s + quote + "");
                    for (char x : s.toCharArray()) {
                        stacks[sid].push(x);
                    }
                    string = false;
                    quote = 0;
                }
                else {
                    sb.append(c);
                }
            }
            else {
                string = true;
                quote = c;
            }
        }
        else if (string) {
            sb.append(c);
        }
        else if (c == 0 || c == ' ') {
            return;
        }
        else if (c == '>' || c == '<' || c == '^' || c == 'v' || c == '/' || c == '\\' || c == '|' || c == '_'
                || c == 'x' || c == '#' || c == 'z' || c == 'y') {
            dir = dir.get(c);
        }
        else if (c == '!') {
            if (debug) System.out.println("SKIP");
            skip = true;
        }
        else if (c == '?') {
            if (debug) System.out.println("COND SKIP");
            double x = stacks[sid].pop();
            skip = (x == 0);
        }
        else if (c == '.') {
            if (debug) System.out.println("JUMP");
            double y = stacks[sid].pop();
            double x = stacks[sid].pop();
            if (!isInteger(y) || !isInteger(x)) { throw new NumberFormatException("X or Y is no integer!"); }
            pos = new int[] { (int) x, (int) y };
        }
        else if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f')) {
            if (debug) System.out.println("NUMBER");
            int i = Integer.parseInt(c + "", 16);
            stacks[sid].push(i);
        }
        else if (c == '+' || c == '-' || c == '*' || c == ',' || c == '%' || c == '=' || c == '(' || c == ')') {
            double y = stacks[sid].pop();
            double x = stacks[sid].pop();
            double z = 0;
            if (debug) System.out.println("MATH " + c);
            switch (c) {
            case '+':
                z = x + y;
                break;
            case '-':
                z = x - y;
                break;
            case '*':
                z = x * y;
                break;
            case ',':
                z = x / y;
                break;
            case '%':
                z = x % y;
                break;
            case '=':
                z = (x == y) ? 1 : 0;
                break;
            case '(':
                z = (y > x) ? 1 : 0;
                break;
            case ')':
                z = (y < x) ? 1 : 0;
                break;
            default:
                break;
            }
            stacks[sid].push(z);
        }
        else if (c == ':' || c == '~' || c == '$' || c == '@' || c == '}' || c == '{' || c == 'r' || c == 'l') {
            stacks[sid].action(c);
        }
        else if (c == '[' || c == ']') {
            if (c == '[') {
                if (debug) System.out.println("NEW STACK");
                double x = stacks[sid].pop();
                if (Math.floor(x) != Math.ceil(x)) { throw new NumberFormatException("X is not an Integer!"); }
                sid++;
                stacks[sid] = new Stack(stacksize, this);
                for (int a = 0; a < x; a++) {
                    double item = stacks[sid - 1].pop();
                    stacks[sid].push(item);
                }
                stacks[sid].reverse();
            }
            else if (c == ']') {
                sid--;
                stacks[sid] = merge(stacks[sid], stacks[sid + 1]);
            }
        }
        else if (c == 'n') {
            double i = stacks[sid].pop();
            if (isInteger(i)) {
                System.out.print((int) i);
            }
            else {
                System.out.print(i);
            }
        }
        else if (c == 'o') {
            double i = stacks[sid].pop();
            System.out.print((char) i);
        }
        else if (c == 'i') {
            int i = System.in.read();
            stacks[sid].push(i);
        }
        else if (c == '&') {
            if (debug) System.out.println("REGISTER");
            if (reg) stacks[sid].push(register);
            else register = stacks[sid].pop();
            reg = !reg;
        }
        else if (c == 'g') {
            if (debug) System.out.println("GET");
            double y = stacks[sid].pop();
            double x = stacks[sid].pop();
            if (!isInteger(y) || !isInteger(x)) { throw new NumberFormatException("X or Y is not an Integer!"); }
            char v = grid[(int) y][(int) x];
            stacks[sid].push(v);
        }
        else if (c == 'p') {
            if (debug) System.out.println("PLACE");
            double y = stacks[sid].pop();
            double x = stacks[sid].pop();
            double v = stacks[sid].pop();
            if (!isInteger(y) || !isInteger(x) || !isInteger(v)) { throw new NumberFormatException(
                    "X, Y or V is not an Integer!"); }
            grid[(int) x][(int) y] = (char) v;
        }
        else if (c == ';') {
            run = false;
            return;
        }
        else {
            System.out.println("Unknown command: " + c);
        }
    }

    public boolean isNumber(String s) {
        try {
            Integer.parseInt(s);
            return true;
        }
        catch (Exception e) {}
        try {
            Double.parseDouble(s);
            return true;
        }
        catch (Exception e) {}
        return false;
    }

    public boolean isInteger(double x) {
        return Math.floor(x) == Math.ceil(x) && x > Integer.MIN_VALUE && x < Integer.MAX_VALUE;
    }

    public Stack merge(Stack x, Stack y) {
        if (debug) {
            System.out.println("MERGE");
            System.out.println("~ X ~");
            x.forcePrint();
            System.out.println("~ Y ~");
            y.forcePrint();
        }
        Stack z = new Stack(stacksize, this);
        while (y.length() > 0) {
            z.push(y.pop());
        }
        while (x.length() > 0) {
            z.push(x.pop());
        }
        z.reverse();
        if (debug) {
            System.out.println("~ Z ~");
            z.forcePrint();
        }
        return z;
    }

    public static void main(String[] args) {
        new Refract(args);
    }
}

enum Directions {
    RIGHT, LEFT, DOWN, UP, UP_RIGHT, DOWN_RIGHT, UP_LEFT, DOWN_LEFT;

    /**
     * Move the given position according to the current direction
     * 
     * @param pos
     *            The old position
     * @return The new position
     */
    public int[] move(int[] pos) {
        switch (this) {
        case RIGHT:
            pos[0] += 1;
            break;
        case LEFT:
            pos[0] -= 1;
            break;
        case DOWN:
            pos[1] += 1;
            break;
        case UP:
            pos[1] -= 1;
            break;
        case UP_RIGHT:
            pos[1] -= 1;
            pos[0] += 1;
            break;
        case DOWN_RIGHT:
            pos[1] += 1;
            pos[0] += 1;
            break;
        case UP_LEFT:
            pos[1] -= 1;
            pos[0] -= 1;
            break;
        case DOWN_LEFT:
            pos[1] += 1;
            pos[0] -= 1;
            break;
        default:
            break;
        }
        return pos;
    }

    public Directions get(char c) {
        switch (c) {
        case '>':
            return RIGHT;
        case '<':
            return LEFT;
        case '^':
            return UP;
        case 'v':
            return DOWN;
        case '/':
            switch (this) {
            case RIGHT:
                return UP;
            case LEFT:
                return DOWN;
            case DOWN:
                return LEFT;
            case UP:
                return RIGHT;
            case UP_RIGHT:
            case DOWN_LEFT:
                return this;
            case DOWN_RIGHT:
                return UP_LEFT;
            case UP_LEFT:
                return DOWN_RIGHT;
            default:
                return null;
            }
        case '\\':
            switch (this) {
            case RIGHT:
                return DOWN;
            case LEFT:
                return UP;
            case DOWN:
                return RIGHT;
            case UP:
                return LEFT;
            case DOWN_RIGHT:
            case UP_LEFT:
                return this;
            case UP_RIGHT:
                return DOWN_LEFT;
            case DOWN_LEFT:
                return UP_RIGHT;
            default:
                return null;
            }
        case '|':
            switch (this) {
            case UP:
            case DOWN:
                return this;
            case RIGHT:
                return LEFT;
            case LEFT:
                return RIGHT;
            case UP_RIGHT:
                return DOWN_RIGHT;
            case DOWN_RIGHT:
                return UP_RIGHT;
            case UP_LEFT:
                return DOWN_LEFT;
            case DOWN_LEFT:
                return UP_LEFT;
            default:
                return null;
            }
        case '_':
            switch (this) {
            case UP:
            case DOWN:
                return this;
            case RIGHT:
                return LEFT;
            case LEFT:
                return RIGHT;
            case UP_RIGHT:
                return DOWN_LEFT;
            case DOWN_LEFT:
                return UP_RIGHT;
            case UP_LEFT:
                return DOWN_RIGHT;
            case DOWN_RIGHT:
                return UP_LEFT;
            default:
                return null;
            }
        case 'x':
            int min = 1,
            max = 8;
            int ran = new Random().nextInt(max - min) + min;
            switch (ran) {
            case 1:
                return RIGHT;
            case 2:
                return LEFT;
            case 3:
                return UP;
            case 4:
                return DOWN;
            case 5:
                return UP_RIGHT;
            case 6:
                return DOWN_RIGHT;
            case 7:
                return UP_LEFT;
            case 8:
                return DOWN_LEFT;
            default:
                System.err.println("Weird number in random function: " + ran);
                return null;
            }
        case '#':
            switch (this) {
            case RIGHT:
                return LEFT;
            case LEFT:
                return RIGHT;
            case DOWN:
                return UP;
            case UP:
                return DOWN;
            case UP_RIGHT:
                return DOWN_LEFT;
            case DOWN_RIGHT:
                return UP_LEFT;
            case UP_LEFT:
                return DOWN_RIGHT;
            case DOWN_LEFT:
                return UP_RIGHT;
            default:
                return null;
            }
        case 'y':
            switch (this) {
            case UP:
            case DOWN:
            case DOWN_RIGHT:
            case DOWN_LEFT:
                return this;
            case LEFT:
                return DOWN_LEFT;
            case RIGHT:
                return DOWN_RIGHT;
            case UP_RIGHT:
                return DOWN_LEFT;
            case UP_LEFT:
                return DOWN_RIGHT;
            default:
                return null;
            }
        case 'z':
            switch (this) {
            case UP:
                return DOWN;
            case DOWN:
                return UP;
            case DOWN_RIGHT:
                return UP_LEFT;
            case DOWN_LEFT:
            case UP_RIGHT:
                return this;
            case LEFT:
                return DOWN_RIGHT;
            case RIGHT:
                return UP_LEFT;
            case UP_LEFT:
                return DOWN_RIGHT;
            default:
                return null;
            }
        default:
            return null;
        }
    }
}

class Stack {

    Refract refract;
    double[] stack;
    double[] temp = null;
    int i = -1;

    public Stack(int size, Refract refract) {
        this.stack = new double[size];
        this.refract = refract;
    }

    public void push(double x) {
        if (refract.debug) System.out.println("PUSH " + x + " TO " + (i + 1));
        if (i >= stack.length - 1) { throw new ArrayIndexOutOfBoundsException("Can't push to full stack!"); }
        stack[++i] = x;
    }

    public double pop() {
        if (refract.debug) System.out.println("POP " + stack[i] + " FROM " + i);
        if (i <= -1) { throw new ArrayIndexOutOfBoundsException("Can't pop from empty stack!"); }
        double x = stack[i];
        stack[i] = 0;
        i--;
        return x;
    }

    public void reverse() {
        for (int left = 0, right = i; left < right; left++, right--) {
            double x = stack[left];
            stack[left] = stack[right];
            stack[right] = x;
        }
    }

    public void print() {
        if (!eq(temp, stack)) {
            if (length() == -1) {
                System.out.println("-- EMPTY --");
                return;
            }
            System.out.println("-----");
            int x = 0;
            for (double d : stack) {
                if (x >= length()) break;
                System.out.println(x + "    " + d);
                x++;
            }
            System.out.println("-----");
            temp = stack.clone();
        }
    }

    public void forcePrint() {
        temp = null;
        print();
    }

    public boolean eq(double[] x, double[] y) {
        if (x == null || y == null) return x == null && y == null;
        if (x.length != y.length) return false;
        for (int a = 0; a < x.length; a++) {
            if (x[a] != y[a]) return false;
        }
        return true;
    }

    public int length() {
        return i + 1;
    }

    public void action(char c) {
        double x, y, z;
        switch (c) {
        case ':':
            if (refract.debug) System.out.println("COPY");
            x = pop();
            push(x);
            push(x);
            break;
        case '~':
            if (refract.debug) System.out.println("REMOVE");
            x = pop();
            break;
        case '$':
            if (refract.debug) System.out.println("SWAP 2");
            x = pop();
            y = pop();
            push(x);
            push(y);
            break;
        case '@':
            if (refract.debug) System.out.println("SWAP 3");
            x = pop();
            y = pop();
            z = pop();
            push(x);
            push(z);
            push(y);
            break;
        case '}':
            if (refract.debug) System.out.println("MOVE RIGHT");
            x = stack[i];
            for (int a = i; a > 0; a--) {
                stack[a] = stack[a - 1];
            }
            stack[0] = x;
            break;
        case '{':
            if (refract.debug) System.out.println("MOVE LEFT");
            x = stack[0];
            for (int a = 0; a < i; a++) {
                stack[a] = stack[a + 1];
            }
            stack[i] = x;
            break;
        case 'r':
            if (refract.debug) System.out.println("REVERSE");
            reverse();
            break;
        case 'l':
            if (refract.debug) System.out.println("LENGTH");
            push(length());
            break;
        default:
            break;
        }
    }
}

class FileIO {

    File file;
    int bufSize = 64 * 1024;
    char newLine = '\n';

    public FileIO(String filename) {
        this(new File(filename));
    }

    public FileIO(File file) {
        this.file = file;
    }

    public String[] readData() throws NullPointerException, FileNotFoundException, IOException {
        synchronized (this.file) {
            if (!this.file.exists()) { throw new NullPointerException("File '" + this.file + "' does not exist!"); }
            if (!this.file.canRead()) { throw new IOException("File '" + this.file + "' can't be read!"); }
            try (FileInputStream fis = new FileInputStream(this.file)) {
                byte[] buf = new byte[this.bufSize];
                int bytes = fis.read(buf);
                String data = bytes < 0 ? "" : new String(buf, 0, bytes);
                return data.split(this.newLine + "");
            }
        }
    }

    public void writeData(String[] data) throws FileNotFoundException, IOException {
        synchronized (this.file) {
            StringBuilder sb = new StringBuilder();
            for (String line : data)
                sb.append(line + newLine);
            try (FileOutputStream fos = new FileOutputStream(this.file)) {
                byte[] buf = sb.toString().getBytes();
                fos.write(buf);
            }
        }
    }
}
