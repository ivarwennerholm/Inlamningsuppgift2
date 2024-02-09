import java.util.*;

public class Test {

    public static void main(String[] args) {
        Test test = new Test();
        Comparator<String[]> comp = Comparator.comparingInt(arr -> Integer.parseInt(arr[arr.length - 1]));
        List<String[]> list = new ArrayList<>();
        list.add(new String[]{"Kanske", "Ja", "11"});
        list.add(new String[]{"Nej", "Ja", "8"});
        list.add(new String[]{"Ja", "Nej", "6"});
        List<String[]> listSorted = test.getSortedList(comp, list);
        // Print the sorted list
        for (String[] arr : listSorted) {
            System.out.println(Arrays.toString(arr));
        }
    }

    List<String[]> getSortedList(Comparator<String[]> comp, List<String[]> list) {
        list.sort(comp);
        return list;
    }
}
