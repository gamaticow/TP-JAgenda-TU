package version1;

import myrendezvous.Rendezvous;
import myrendezvous.RendezvousManager;
import myrendezvous.exceptions.RendezvousNotFound;
import myrendezvous.utils.StringComparator;

import java.lang.reflect.Array;
import java.util.*;

public class RendezvousManagerImpl extends TreeMap<Calendar, RendezvousImpl> implements RendezvousManager {

    public RendezvousManagerImpl(){
        super(new RendezvousComparator());
    }

    @Override
    public Rendezvous addRendezvous(Rendezvous rendezvous) {
        if(containsKey(rendezvous.getTime()))
            throw new IllegalArgumentException();

        RendezvousImpl rdv = new RendezvousImpl(rendezvous.getTitle(), rendezvous.getDescription(), (Calendar) rendezvous.getTime().clone(), rendezvous.getDuration());
        put(rdv.getTime(), rdv);

        try {
            return rdv.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void removeRendezvous(Rendezvous rendezvous) throws IllegalArgumentException, RendezvousNotFound {
        if(!(rendezvous instanceof RendezvousImpl))
            throw new IllegalArgumentException();
        Calendar time = rendezvous.getTime();
        time.set(Calendar.SECOND, 0);
        time.set(Calendar.MILLISECOND, 0);
        if(!containsKey(time))
            throw new RendezvousNotFound();
        Rendezvous rdv = get(time);
        if(!rdv.equals(rendezvous))
            throw new RendezvousNotFound();
        remove(time);
    }

    @Override
    public boolean removeRendezvous(Calendar calendar) {
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return remove(calendar) != null;
    }

    @Override
    public void removeAllRendezvousBefore(Calendar calendar) throws IllegalArgumentException {
        if(calendar == null)
            throw new IllegalArgumentException();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        headMap(calendar).clear();
    }

    @Override
    public Rendezvous updateRendezvous(Rendezvous rendezvous) throws RendezvousNotFound {
        RendezvousImpl rdvi = findRendezvousByTag(rendezvous);
        if(rdvi == null)
            throw new RendezvousNotFound();

        //La date est déja utilisé
        if(!rdvi.getTime().equals(rendezvous.getTime()) && containsKey(rendezvous.getTime()))
            throw new IllegalArgumentException();

        if(!rdvi.getTime().equals(rendezvous.getTime())){
            remove(rdvi.getTime());
            put(rendezvous.getTime(), rdvi);
        }

        rdvi.setTitle(rendezvous.getTitle());
        rdvi.setDescription(rendezvous.getDescription());
        rdvi.setTime(rendezvous.getTime());
        rdvi.setDuration(rendezvous.getDuration());

        try {
            return rdvi.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<Rendezvous> getRendezvousBetween(Calendar calendar, Calendar calendar1) throws IllegalArgumentException {
        if (calendar == null || calendar1 == null || calendar1.before(calendar))
            throw new IllegalArgumentException();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar1.set(Calendar.SECOND, 0);
        calendar1.set(Calendar.MILLISECOND, 0);
        return cloneList(new ArrayList<>(subMap(calendar, calendar1).values()));
    }

    @Override
    public List<Rendezvous> getRendezvousBefore(Calendar calendar) throws IllegalArgumentException {
        if(calendar == null)
            throw new IllegalArgumentException();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return cloneList(new ArrayList<>(headMap(calendar).values()));
    }

    @Override
    public List<Rendezvous> getRendezvousAfter(Calendar calendar) throws IllegalArgumentException {
        if (calendar == null)
            throw new IllegalArgumentException();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return cloneList(new ArrayList<>(tailMap(calendar).values()));
    }

    @Override
    public List<Rendezvous> getRendezvousToday() {
        Calendar startOfDay = Calendar.getInstance();
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        Calendar endOfDay = Calendar.getInstance();
        endOfDay.set(Calendar.HOUR_OF_DAY, 23);
        endOfDay.set(Calendar.MINUTE, 59);
        return getRendezvousBetween(startOfDay, endOfDay);
    }

    @Override
    public boolean hasOverlap(Calendar calendar, Calendar calendar1) {
        List<Rendezvous> rdvs = new ArrayList<>(values());

        if(calendar != null && calendar1 != null){
            rdvs = getRendezvousBetween(calendar, calendar1);
        }else if(calendar != null){
            rdvs = getRendezvousAfter(calendar);
        }else if (calendar1 != null){
            rdvs = getRendezvousBefore(calendar1);
        }

        for(int i = 0; i < rdvs.size()-1; i++){
            Calendar endTime = rdvs.get(i).getTime();
            endTime.add(Calendar.MINUTE, rdvs.get(i).getDuration());
            if(rdvs.get(i+1).getTime().before(endTime))
                return true;
        }
        return false;
    }

    @Override
    public Calendar findFreeTime(int i, Calendar calendar, Calendar calendar1) throws IllegalArgumentException {
        if(calendar == null || calendar1 == null || i <= 0)
            throw new IllegalArgumentException();
        if(getGap(calendar, calendar1) < i)
            return null;
        List<Rendezvous> rdvBefore = getRendezvousBefore(calendar);
        Calendar last = calendar;
        for(Rendezvous rdv : rdvBefore){
            Calendar c = rdv.getTime();
            c.add(Calendar.MINUTE, rdv.getDuration());
            if(c.after(last))
                last = c;
        }

        List<Rendezvous> rdvs = getRendezvousBetween(calendar, calendar1);
        if(rdvs.isEmpty())
            return getGap(last, calendar1) > i ? last : null;
        for(int j = 0; j < rdvs.size()-1; j++){
            Calendar endTime = rdvs.get(j).getTime();
            endTime.add(Calendar.MINUTE, rdvs.get(j).getDuration());
            int gap = getGap(endTime, rdvs.get(j+1).getTime());
            if(gap >= i)
                return endTime;
        }
        Calendar endTime = rdvs.get(rdvs.size()-1).getTime();
        endTime.add(Calendar.MINUTE, rdvs.get(rdvs.size()-1).getDuration());
        int gap = getGap(endTime, calendar1);
        if(gap >= i)
            return endTime;
        return null;
    }

    @Override
    public List<Rendezvous> findRendezvousByTitleEqual(String s, Calendar calendar, Calendar calendar1) {
        //La liste contient deja des rendez-vous cloné
        List<Rendezvous> rdvs = getRendezvousBetween(calendar, calendar1);
        List<Rendezvous> correspondant = new ArrayList<>();

        for (Rendezvous r : rdvs){
            if(StringComparator.isEqualNoCase(r.getTitle(), s))
                correspondant.add(r);
        }
        return correspondant;
    }

    @Override
    public List<Rendezvous> findRendezvousByTitleALike(String s, Calendar calendar, Calendar calendar1) {
        //La liste contient deja des rendez-vous cloné
        List<Rendezvous> rdvs = getRendezvousBetween(calendar, calendar1);
        List<Rendezvous> correspondant = new ArrayList<>();

        for (Rendezvous r : rdvs){
            if(StringComparator.isAlike(r.getTitle(), s))
                correspondant.add(r);
        }
        return correspondant;
    }

    private RendezvousImpl findRendezvousByTag(Rendezvous rendezvous){
        if(!(rendezvous instanceof RendezvousImpl))
            return null;
        RendezvousImpl rdvi = (RendezvousImpl) rendezvous;
        for(RendezvousImpl rdv : this.values()){
            if(rdv.equals(rdvi))
                return rdv;
        }
        return null;
    }

    private List<Rendezvous> cloneList(List<RendezvousImpl> list) {
        List<Rendezvous> clone = new ArrayList<>();
        try{
            for(RendezvousImpl rdv : list)
                clone.add(rdv.clone());
        }catch (CloneNotSupportedException e){
            e.printStackTrace();
        }

        return clone;
    }

    private int getGap(Calendar calendar, Calendar calendar1){
        return (int) ((calendar1.getTime().getTime() - calendar.getTime().getTime()) / (1000 * 60));
    }

}
