import java.util.List;

@FunctionalInterface
public interface ListCleaner {
    List<String> apply(List<String> output);

    default ListCleaner andThen(ListCleaner after) {
        return list -> after.apply(apply(list));
    }
}
