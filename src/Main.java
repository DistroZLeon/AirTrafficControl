//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println(Clock.getStartTime());
        Thread.sleep(3000);
        System.out.println(Clock.getCurrentTime());
        ControlTower tower= ControlTower.getInstance("Otopeni",4, 2);
        GateManager gateManager= GateManager.getInstance("Otopeni", 8);
    }
}
