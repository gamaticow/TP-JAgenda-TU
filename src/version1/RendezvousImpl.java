package version1;

import myrendezvous.Rendezvous;

import java.util.Calendar;

public class RendezvousImpl implements Rendezvous, Cloneable {

    private static int identifier = 1;

    private final int id;
    private String title;
    private String description;
    private int duration;
    private Calendar calendar;

    public RendezvousImpl(String title, String description, Calendar calendar, int duration){
        this.id = RendezvousImpl.identifier++;
        this.title = title;
        this.description = description;
        this.duration = duration;
        calendar = (Calendar) calendar.clone();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        this.calendar = calendar;
    }

    @Override
    public Calendar getTime() {
        return calendar;
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setTime(Calendar calendar) throws IllegalArgumentException {
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        this.calendar = calendar;
    }

    @Override
    public void setDuration(int i) throws IllegalArgumentException {
        this.duration = i;
    }

    @Override
    public void setTitle(String s) throws IllegalArgumentException {
        this.title = s;
    }

    @Override
    public void setDescription(String s) {
        this.description = s;
    }

    @Override
    protected RendezvousImpl clone() throws CloneNotSupportedException {
        //On clone l'objet
        RendezvousImpl clone = (RendezvousImpl) super.clone();
        //On redefini le calendrier du clone sur le calendrier cloné
        clone.calendar = (Calendar) calendar.clone();

        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof RendezvousImpl)
            return this.id == ((RendezvousImpl) o).id;
        else
            return false;
    }

    @Override
    public String toString() {
        return "\nRendezvousImpl{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                "}\n";
    }
}
