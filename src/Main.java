
public class Main {
    final Repository repo;
    final Report report;

    public Main() throws Exception {
        repo = new Repository();
        report = repo.report;
        new Gui(repo, report);
    }

    public static void main(String[] args) throws Exception {
        new Main();
    }
}