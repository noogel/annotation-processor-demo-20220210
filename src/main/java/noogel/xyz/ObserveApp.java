package noogel.xyz;

public class ObserveApp {

    @Observe
    public void test_1() {
        System.out.println("test");
    }

    public static void main(String[] args) {
        ObserveApp app = new ObserveApp();
        app.test_1();
    }
}
