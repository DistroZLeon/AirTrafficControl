package subjects;
import observers.Observer;
import java.util.concurrent.CopyOnWriteArrayList;

abstract public class Subject {
    CopyOnWriteArrayList<Observer> observers = new CopyOnWriteArrayList<>();
    public void addObserver(Observer observer) {
        this.observers.add(observer);
    }

    public void removeObserver(Observer observer) {
        this.observers.remove(observer);
    }

    public void notifyObservers(Object arg) {
        for (Observer observer : observers) {
            observer.update(this, arg);
        }
    }
}
