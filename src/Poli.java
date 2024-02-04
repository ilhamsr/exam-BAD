abstract class Hewan {
    protected abstract void munculSuara();
}

class Kucing extends Hewan {
    @Override
    protected void munculSuara() {
        System.out.println("Suara Kucing: Meow...");
    }
}

class Burung extends Hewan {
    @Override
    protected void munculSuara() {
        System.out.println("Suara Burung: Cuit...");
    }
}

public class Poli {
    public static void main(String[] args) {
        Hewan myHewan = new Kucing();
        myHewan.munculSuara();  // Outputs "Suara Kucing: Meow..."

        myHewan = new Burung();
        myHewan.munculSuara();  // Outputs "Suara Burung: Cuit..."
    }
}