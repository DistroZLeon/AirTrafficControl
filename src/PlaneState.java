import java.util.Objects;

public record PlaneState(int id, double fuel, boolean emergency) {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PlaneState that = (PlaneState) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
