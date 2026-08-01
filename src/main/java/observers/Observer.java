package observers;
import subjects.Subject;

public interface Observer {
    void update(Subject source, Object arg);
}
