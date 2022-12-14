package com.github.aursu.souvenirs;

import com.github.aursu.souvenirs.data.*;

import javax.json.JsonObject;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {
    public static final String STORAGE = "souvenirs";
    public static final String[] STORAGE_TABLES = new String[] {"manufacturers", "souvenirs"};
    private DataSource datasource = null;
    private ManufacturersTable manTab;
    private SouvenirsTable sovTab;

    private final InputReader input = new InputReader();

    public static void main(String[] args) {
        new Main().run();
    }

    private void setup() {
        Properties props = new Properties();
        try (BufferedReader reader = new BufferedReader(new FileReader("config.properties"))) {
            props.load(reader);

            // read data source root path from properties file
            String storagePath = props.getProperty("path");
            setupStorage(storagePath);
        } catch (IOException e) {
            System.err.printf("%s\n", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void setupStorage(String storagePath) {
        datasource = new DataSource(storagePath, STORAGE);

        try {
            datasource.setup();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        for (String table : STORAGE_TABLES) {
            datasource.createTable(table);
        }

        manTab = new ManufacturersTable(datasource);

        sovTab = new SouvenirsTable(datasource);
        sovTab.setManufacturersTable(manTab);
    }

    private void run() {
        setup();

        while (true) {
            int action = menuAction();

            switch (action) {
                case 0:
                    return;
                case 1:
                    displayTable(manTab);
                    break;
                case 2:
                    actionAddManufacturer();
                    break;
                case 3:
                    actionEditManufacturer();
                    break;
                case 4:
                    actionDeleteManufacturer();
                    break;
                case 5:
                    displayTable(sovTab);
                    break;
                case 6:
                    actionAddSouvenir();
                    break;
                case 7:
                    actionEditSouvenir();
                    break;
                case 8:
                    actionDeleteSouvenir();
                    break;
                case 9:
                    otherActions();
                    break;
                default: System.exit(0);
            }
        }
    }

    private void otherActions() {
        while (true) {
            int action = menuOther();

            switch (action) {
                case 0:
                    return;
                case 1:
                    actionOtherManufacturerSouvenir();
                    break;
                case 2:
                    actionOtherSouvenirsByCountry();
                    break;
                case 3:
                    actionOtherManufacturersWithPricesLessThen();
                    break;
                case 4:
                    actionOtherManufacturersWithSouvenirs();
                    break;
                case 5:
                    actionOtherManufacturersBySouvenirAndYear();
                    break;
                case 6:
                    actionOtherSouvenirsByYear();
                    break;

            }
        }
    }

    // Для каждого года вывести список сувениров, произведенных в этом году.
    private void actionOtherSouvenirsByYear() {
        List<Integer> sovYears = sovTab.stream()
                .map(e -> DataObject.loadDateTime(e.getString("release")).getYear())
                .distinct()
                .toList();

        while (true) {
            for (int year: sovYears) {
                System.out.printf("Список сувениров, произведенных в %d году:\n", year);

                DataTable sovYear = sovTab.selection("release", StaticFilter.ofYear(year));
                displayTableWithoutIDs(sovYear,false);
            }

            // repeat or exit
            if (menuRepeat() == 0) break;
        }
    }

    // Вывести информацию о производителях заданного сувенира, произведенного
    // в заданном году.
    private void actionOtherManufacturersBySouvenirAndYear() {
        // get souvenirs with unique names
        DataTable uniqSov = sovTab.selection("name", UniqueFilter.stringFilter());

        while (true) {
            displayTableWithoutIDs(uniqSov, false);

            String name = input.readString("Choose Souvenir name to show: ");
            System.out.printf("Заданный сувенир: %s\n", name);

            Integer year = input.readNumber("Enter year of production: ", 2022);
            System.out.printf("Заданный год: %d\n", year);

            DataTable sovNameYear = sovTab.selection("name", StaticFilter.of(name))
                    .selection("release", ComparisonFilter.sameYear(year));

            if (sovNameYear.isEmpty()) {
                System.out.printf("В заданном году (%d) не производили сувенир %s\n", year, name);
            }
            else {
                List<Integer> targetAudience = sovNameYear.stream()
                        .map(e -> e.getInt("manufacturerId"))
                        .toList();

                DataTable sovMan = manTab.selection("id", ListFilter.integerFilter(targetAudience));

                System.out.printf("В заданном году (%d) сувенир %s произвели:\n", year, name);

                sovMan.display();
            }

            // repeat or exit
            if (menuRepeat() == 0) break;
        }
    }

    // Вывести информацию по всем производителям и, для каждого производителя
    // вывести информацию о всех сувенирах, которые он производит.
    private void actionOtherManufacturersWithSouvenirs() {
        while (true) {
            List<Manufacturer> men = manTab.stream().map(e -> Manufacturer.of(e)).toList();

            for (Manufacturer man: men) {
                System.out.printf("Информация для производителя %s:\n", man.getName());
                man.display();

                DataTable manSov = sovTab.selection("manufacturerId", StaticFilter.of(man.getId()));
                if(manSov.isEmpty()) {
                    System.out.println("Производитель пока не производит сувениры");
                }
                else {
                    System.out.println("Сувениры, которые производит этот производитель:");
                    manSov.display(true);
                }
            }
            // repeat or exit
            if (menuRepeat() == 0) break;
        }
    }

    // Вывести информацию о производителях, чьи цены на сувениры
    // меньше заданной.
    private void actionOtherManufacturersWithPricesLessThen() {
        // Map of all manufacturers and count of their souvenirs
        Map<Integer, Long>  manCount = sovTab.stream()
                .map(e -> e.getInt("manufacturerId"))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        while (true) {
            double price = (double) input.readNumber("Задайте цену: ", true);

            // map of manufacturers Ids and count of souvenirs with price less then specified
            Map<Integer, Long> manPriceLessCount = sovTab
                    .stream("price", ComparisonFilter.lessThen(price))
                    .map(e -> e.getInt("manufacturerId"))
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            List<Integer> targetAudience = new ArrayList<>();

            // if count of all souvenirs for manufacturer is equal to count of filtered by price,
            // then save its id
            for (int manId: manPriceLessCount.keySet()) {
                if (manPriceLessCount.get(manId).equals(manCount.get(manId)))
                    targetAudience.add(manId);
            }

            if (targetAudience.isEmpty()) {
                System.out.printf("Нет производителей, чьи цены на сувениры меньше %.2f\n", price);
            }
            else {
                DataTable manPriceLess = manTab.selection("id", ListFilter.integerFilter(targetAudience));

                System.out.printf("Производители, чьи цены на сувениры меньше %.2f:\n", price);
                displayTable(manPriceLess, false);
            }

            // repeat or exit
            if (menuRepeat() == 0) break;
        }
    }

    // Вывести информацию о сувенирах, произведенных в заданной стране.
    private void actionOtherSouvenirsByCountry() {
        // select all manufacturers with unique countries to display them to user
        DataTable uniqCountries = manTab.selection("country", UniqueFilter.stringFilter());

        while (true) {
            displayTableWithoutIDs(uniqCountries, false);

            String country = input.readString("Задайте страну производителя: ");

            // check if entered country exists
            DataTable manCountry = manTab.selection("country", StaticFilter.of(country));
            if (manCountry.isEmpty()) {
                System.out.println("Производитель с такой страной не найден.");
                continue;
            }

            System.out.println("Страна выбрана: " + country);

            // list of manufacturers IDs in specified country
            List<Integer> manufacturers = manCountry.stream()
                    .map(e -> e.getInt("id"))
                    .toList();

            DataTable manSov = sovTab.selection("manufacturerId",
                    ListFilter.integerFilter(manufacturers));

            if (manSov.size() > 0) {
                System.out.println("Сувениры произведенные в заданной стране:");
                displayTableWithoutIDs(manSov, false);
            }
            else {
                System.out.println("В заданной стране еще нет сувениров.");
            }

            // repeat or exit
            if (menuRepeat() == 0) break;
        }
    }

    private void actionOtherManufacturerSouvenir() {
        while (true) {
            displayTable(manTab, false);
            int id = input.readNumber("Задайте производителя (его ID): ");

            JsonObject obj = manTab.getRow(id);
            if (obj == null) {
                System.out.println("Wrong ID. Please try again");
                continue;
            }

            Manufacturer man = Manufacturer.of(obj);
            System.out.println("Производитель выбран: " + man.getName());

            DataTable manSov = sovTab.selection("manufacturerId", StaticFilter.of(id));
            if (manSov.size() > 0) {
                System.out.println("Сувениры заданного производителя:");
                displayTableWithoutIDs(manSov, false);
            }
            else {
                System.out.println("У производителя еще нет сувениров.");
            }

            // repeat or exit
            if (menuRepeat() == 0) break;
        }
    }

    private void actionDeleteSouvenir() {
        while (true) {
            displayTable(sovTab, false);
            int id = input.readNumber("Choose Souvenir ID to delete: ");

            JsonObject obj = sovTab.getRow(id);
            if (obj == null) {
                System.out.println("Wrong ID. Please try again");
                continue;
            }

            Souvenir sov = Souvenir.of(obj);
            System.out.printf("Souvenir %s deleted.\n", sov.getName());

            sovTab.deleteRows(id);

            // repeat or exit
            if (menuRepeat() == 0) break;
        }
    }

    private void actionEditSouvenir() {
        while (true) {
            displayTable(sovTab, false);
            int id = input.readNumber("Enter id: ");

            JsonObject obj = sovTab.getRow(id);

            if (obj == null) {
                System.out.println("Wrong ID. Please try again");
                continue;
            }

            Souvenir current = Souvenir.of(obj),
                    updated = Souvenir.of(obj);

            // Souvenir to edit
            String newName = input.readString("Edit Name", current.getName());
            updated.setName(newName);

            LocalDate newRelease = input.readDate("Edit Release Date", current.getRelease());
            updated.setRelease(newRelease);

            double newPrice = input.readNumber("Edit Price", current.getPrice());
            updated.setPrice(newPrice);

            if (updated.equals(current)) {
                System.out.println("No changes made!");

                // repeat or exit
                if (menuRepeat() == 0) break;
                continue;
            }

            sovTab.insertRow(id, updated.toJson());

            System.out.print("Updated souvenir: ");
            System.out.println(updated.toJson());

            // repeat or exit
            if (menuRepeat() == 0) break;
        }
    }

    private void displayTable(DataTable table) {
        displayTable(table, true);
    }

    private void displayTable(DataTable table, boolean repeat) {
        displayTable(table, false, repeat);
    }

    private void displayTableWithoutIDs(DataTable table, boolean repeat) {
        displayTable(table, true, repeat);
    }

    private void displayTable(DataTable table, boolean hideIds, boolean repeat) {
        while (true) {
            table.display(hideIds);

            if ( ! repeat || menuRepeat() == 0) break;
        }
    }

    private void actionAddManufacturer() {
        while (true) {
            String name = input.readString("Enter name: ");
            String country = input.readString("Enter country: ");

            // avoid duplicates
            DataTable dup = manTab.selection("name", StaticFilter.of(name))
                    .selection("country", StaticFilter.of(country));

            if (dup.size() > 0)
                System.out.println("This manufacturer already exists");
            else
                manTab.insertRow(new Manufacturer(name, country).toJson());

            if (menuRepeat() == 0) break;
        }
    }

    private void actionEditManufacturer() {
        while (true) {
            displayTable(manTab, false);
            int id = input.readNumber("Enter id: ");

            JsonObject obj = manTab.getRow(id);

            if (obj == null) {
                System.out.println("Wrong ID. Please try again");
                continue;
            }
            Manufacturer man = Manufacturer.of(obj),
                    updated = Manufacturer.of(obj);

            // manufacturer to  edit
            String newName = input.readString("Edit Name", man.getName());
            updated.setName(newName);

            String newCountry = input.readString("Edit Country", man.getCountry());
            updated.setCountry(newCountry);

            if (updated.equals(man)) {
                System.out.println("No changes made!");

                // repeat or exit
                if (menuRepeat() == 0) break;
                continue;
            }

            manTab.insertRow(id, updated.toJson());
            System.out.println("Updated manufacturer: " + updated.getName());

            // repeat or exit
            if (menuRepeat() == 0) break;
        }
    }

    private void actionDeleteManufacturer() {
        while (true) {
            displayTable(manTab, false);
            int id = input.readNumber("Choose manufacturer to delete: ");

            JsonObject obj = manTab.getRow(id);
            if (obj == null) {
                System.out.println("Wrong ID. Please try again");
                continue;
            }

            Manufacturer man = Manufacturer.of(obj);
            System.out.println("Manufacturer: " + man.getName());

            // display all souvenirs to delete along with manufacturer
            DataTable manSov = sovTab.selection("manufacturerId", StaticFilter.of(id));
            if (manSov.size() > 0) {
                System.out.println("Souvenirs to delete as well:");
                displayTable(manSov, false);

                if (menuEnsure() == 0) break;

                manSov.deleteRows();
                sovTab.sync();
            }

            manTab.deleteRows(id);

            // repeat or exit
            if (menuRepeat() == 0) break;
        }
    }

    private void actionAddSouvenir() {
        while (true) {
            displayTable(manTab, false);
            int manufacturerId = input.readNumber("Choose manufacturer id for this product: ");

            JsonObject manObj = manTab.getRow(manufacturerId);

            if (manObj == null) {
                System.out.println("Wrong ID chosen. Please try again.");
                continue;
            }

            Manufacturer man = Manufacturer.of(manObj);
            System.out.println("Manufacturer: " + man.getName());

            String name = input.readString("Enter name: ");
            LocalDate releaseDate = input.readDate("Enter production date");
            double price = (double) input.readNumber("Enter price: ", true);

            // avoid duplicates
            DataTable dupCheck = sovTab.selection("name", StaticFilter.of(name))
                    .selection("manufacturerId", StaticFilter.of(manufacturerId));

            if (dupCheck.size() > 0)
                System.out.println("This souvenir already exists");
            else {
                Souvenir sov = new Souvenir(name, man);
                sov.setPrice(price);
                sov.setRelease(releaseDate);

                sovTab.insertRow(sov.toJson());
            }

            if (menuRepeat() == 0) break;
        }
    }

    private int menuAction() {
        List<String> menuActions = List.of(
                "Exit",
                "Display all manufacturers",
                "Add manufacturer",
                "Edit manufacturer",
                "Delete manufacturer (and all his souvenirs)",
                "Display all souvenirs",
                "Add souvenirs",
                "Edit souvenirs",
                "Delete souvenirs",
                "Other actions ...");
        return selectMenuItem(menuActions, 1);
    }

    private int menuOther() {
        List<String> menuActions = List.of(
                "Back...",
                "Вывести информацию о сувенирах заданного производителя",
                "Вывести информацию о сувенирах, произведенных в заданной стране.",
                "Вывести информацию о производителях, чьи цены на сувениры\n" +
                        "меньше заданной.",
                "Вывести информацию по всем производителям и, для каждого производителя\n" +
                        "вывести информацию о всех сувенирах, которые он производит.",
                "Вывести информацию о производителях заданного сувенира, произведенного\n" +
                        "в заданном году.",
                "Для каждого года вывести список сувениров, произведенных в этом году.");
        return selectMenuItem(menuActions, 0);
    }

    private int menuRepeat() {
        List<String> menuActions = List.of(
                "Back...",
                "Repeat");

        return selectMenuItem(menuActions, 0);
    }

    private int menuEnsure() {
        List<String> menuActions = List.of(
                "Back...",
                "Continue?");

        return selectMenuItem(menuActions, 0);
    }

    private int displayMenu(List<String> menuItems) {
        System.out.println("Choose what to do:");

        for (int i = 1; i < menuItems.size(); i++) {
            System.out.printf("%d: %s\n", i, menuItems.get(i));
        }
        System.out.printf("0: %s\n", menuItems.get(0));

        return menuItems.size();
    }

    private int selectMenuItem(List<String> menuItems, int menuSelection) {
        while (true) {
            int menuItemsCount = displayMenu(menuItems);
            menuSelection = input.readNumber("Your choice", menuSelection);
            if (menuSelection >= 0 && menuSelection < menuItemsCount) break;
        }

        return menuSelection;
    }
}