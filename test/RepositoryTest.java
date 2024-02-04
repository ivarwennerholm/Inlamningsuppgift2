import Models.Customer;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;


public class RepositoryTest {

    @Test
    public void fetchCorrectCustomerList() throws Exception {
        Repository repo = new Repository();
        Customer c1 = new Customer(1, 11328, "Akilles", "Hälsena", "Skogaholmsvägen 14", "Stockholm", "aki111", "111aki");
        Customer c2 = new Customer(2, 12340, "Laura", "Pedestrian", "Vättingvägen 18", "Säffle", "lau222", "222lau");
        Customer c3 = new Customer(3, 18722, "Bennedict", "Cucumber", "Stares gränd 5", "Göteborg", "ben333", "333ben");
        Customer c4 = new Customer(4, 18988, "Pernilla", "Valspäck", "Lavavägen 118", "Varberg", "per444", "444per");
        Customer c5 = new Customer(5, 18988, "Lars", "Adaktusdotter", "Lavavägen 118", "Varberg", "lar555", "555lar");
        List<Customer> actual = repo.getCustomers();
        List<Customer> expected = asList(c1, c2, c3, c4, c5);

        assertEquals(actual.size(), 5);
        assertEquals(actual.get(0).getFirstName(), expected.get(0).getFirstName());
        assertEquals(actual.get(1).getId(), expected.get(1).getId());
        assertEquals(actual.get(2).getAddress(), expected.get(2).getAddress());
        assertEquals(actual.get(3).getZipCode(), expected.get(3).getZipCode());
        assertEquals(actual.get(4).getUserName(), expected.get(4).getUserName());

    }

}
