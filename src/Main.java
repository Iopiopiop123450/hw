import java.util.ArrayList;
import java.util.List;

public class Main  {
    public static void main(String[] args) {
            List<Car> cars = new ArrayList<>();
            cars.add(new Car("al23me", "Mercedes", "White", 0, 8300000));
            cars.add(new Car("b8730f", "Volga", "Black", 0, 673000));
            cars.add(new Car("w487mn", "Lexus", "Grey", 76000, 900000));
            cars.add(new Car("p987hj", "Volga", "Red", 610, 704340));
            cars.add(new Car("c987ss", "Toyota", "White", 254000, 761000));
            cars.add(new Car("09830p", "Toyota", "Black", 698000, 740000));
            cars.add(new Car("p1460p", "BMW", "White", 271000, 850000));
            cars.add(new Car("u893ii", "Toyota", "Purple", 210900, 440000));
            cars.add(new Car("1097df", "Toyota", "Black", 108000, 780000));
            cars.add(new Car("y876wd", "Toyota", "Black", 160000, 1000000));

            String colorToFind = "Black";
            int mileageToFind = 0;

            List<String> filtered = cars.stream()
                    .filter(car -> car.getColor().equals(colorToFind) || car.getMileage() == mileageToFind)
                    .map(Car::getNumber)
                    .toList();
        System.out.println("Номера машин по цвету или пробегу: " + filtered);

            long unique = cars.stream()
                    .filter(car -> car.getPrice() >= 700000 && car.getPrice() <= 800000)
                    .map(Car::getModel)
                    .distinct()
                    .count();
        System.out.println("Уникальные автомобили: " + unique);




        }

}