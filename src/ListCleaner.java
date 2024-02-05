import java.util.List;

@FunctionalInterface
public interface ListCleaner {
    List<String> apply (List<String> output);
}
