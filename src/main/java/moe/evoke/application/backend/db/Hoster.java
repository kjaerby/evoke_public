package moe.evoke.application.backend.db;

import java.util.Objects;

public class Hoster {

    private long ID;
    private String Name;

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hoster hoster = (Hoster) o;
        return ID == hoster.ID && Objects.equals(Name, hoster.Name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID, Name);
    }
}
