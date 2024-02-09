
public class Main {
    Repository repo;

    public Main() throws Exception {
        repo = new Repository();
    }

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        new Gui(main.repo);
    }
}