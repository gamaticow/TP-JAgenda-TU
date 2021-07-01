package version2;

import myrendezvous.Rendezvous;
import myrendezvous.exceptions.RendezvousNotFound;
import org.junit.jupiter.api.*;

import java.util.Calendar;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestVersion2 {

    private RendezvousManagerImpl rdvm;

    private Calendar getDate(int hour, int minute){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        return calendar;
    }

    @BeforeAll
    public void createManager(){
        this.rdvm = new RendezvousManagerImpl();
    }

    @Test
    @Order(1)
    public void testAddRdv(){
        Rendezvous rdv = new RendezvousImpl("RDV1", "1er rendez-vous", getDate(10, 0), 60);
        rdv = rdvm.addRendezvous(rdv);
        Assertions.assertNotNull(rdv);
        Assertions.assertEquals(1, rdvm.size());
    }

    @Test
    @Order(2)
    public void testAddRdvSameCalendar(){
        Rendezvous rdv = new RendezvousImpl("RDV1bis", "Ce rendez vous ne doit pas s'ajouter", getDate(10, 0), 30);
        rdv = rdvm.addRendezvous(rdv);
        Assertions.assertNotNull(rdv);
        Assertions.assertEquals(2, rdvm.size());
    }

    @Test
    @Order(3)
    public void testRemoveRdv() throws RendezvousNotFound {
        int hour = 14, minute = 30;
        Rendezvous rdv = new RendezvousImpl("RDV2", "Rendez vous a supprimé", getDate(hour, minute), 30);
        Rendezvous rendezvousAjouter = rdvm.addRendezvous(rdv);
        Assertions.assertThrows(RendezvousNotFound.class, () -> rdvm.removeRendezvous(rdv));
        rdvm.removeRendezvous(rendezvousAjouter);
        Assertions.assertEquals(2, rdvm.size());
    }

    @Test
    @Order(4)
    public void testRemoveNotExistingRdv() {
        Calendar calendar = getDate(15, 0);
        Rendezvous rdv = new RendezvousImpl("RDV non existant", "", calendar, 10);
        Assertions.assertThrows(RendezvousNotFound.class, () -> rdvm.removeRendezvous(rdv));
    }

    @Test
    @Order(5)
    public void testRemoveByCalendar(){
        Calendar calendar = getDate(18, 0);
        Rendezvous rdv = new RendezvousImpl("RDV", "", calendar, 45);
        rdvm.addRendezvous(rdv);
        Assertions.assertEquals(3, rdvm.size());
        calendar = (Calendar) calendar.clone();
        if(calendar.get(Calendar.MILLISECOND) == 17)
            calendar.set(Calendar.MILLISECOND, 27);
        else
            calendar.set(Calendar.MILLISECOND, 17);
        Assertions.assertTrue(rdvm.removeRendezvous(calendar));
        Assertions.assertEquals(2, rdvm.size());
    }

    @Test
    @Order(6)
    public void testRemoveTwoRdvByCalendar(){
        Rendezvous rdv = new RendezvousImpl("RDV 11", "", getDate(4, 0), 15);
        Rendezvous rdv2 = new RendezvousImpl("RDV 11bis", "", getDate(4, 0), 45);
        rdvm.addRendezvous(rdv);
        rdvm.addRendezvous(rdv2);
        Assertions.assertEquals(4, rdvm.size());
        rdvm.removeRendezvous(getDate(4, 0));
        Assertions.assertEquals(2, rdvm.size());
    }

    @Test
    @Order(7)
    public void testRemoveRdvByNotExistingCalendar(){
        Calendar calendar = getDate(11, 0);
        Calendar calendar1 = getDate(11, 15);
        Rendezvous rdv = new RendezvousImpl("RDV2", "Second rendez vous", calendar, 15);
        rdvm.addRendezvous(rdv);
        Assertions.assertEquals(3, rdvm.size());
        Assertions.assertFalse(rdvm.removeRendezvous(calendar1));
        Assertions.assertEquals(3, rdvm.size());
    }

    @Test
    @Order(8)
    public void testRemoveRdvBefore() {
        Rendezvous rdv = new RendezvousImpl("RDV7", "Rendez vous beaucoup trop tot", getDate(6, 0), 15);
        Rendezvous rdv2 = new RendezvousImpl("RDV8", "Rendez vous trop tot", getDate(7, 30), 30);
        rdvm.addRendezvous(rdv);
        rdvm.addRendezvous(rdv2);
        Assertions.assertEquals(5, rdvm.size());
        rdvm.removeAllRendezvousBefore(getDate(8, 0));
        Assertions.assertEquals(3, rdvm.size());
    }

    @Test
    @Order(9)
    public void testUpdateRdv() throws RendezvousNotFound {
        Calendar calendar = getDate(17, 0);
        Rendezvous rdv = new RendezvousImpl("RDV4", "Troisieme rendez-vous", calendar, 3);
        rdv = rdvm.addRendezvous(rdv);
        rdv.setTitle("RDV3");
        rdv.setDuration(30);
        rdv = rdvm.updateRendezvous(rdv);
        Assertions.assertEquals("RDV3", rdv.getTitle());
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        Assertions.assertEquals(calendar, rdv.getTime());
        Assertions.assertEquals(30, rdv.getDuration());
    }

    @Test
    @Order(10)
    public void testUpdateNotExistingRdv(){
        Rendezvous rdv = new RendezvousImpl("Test", "Non existant dans le manager", getDate(17, 0), 30);
        Assertions.assertThrows(RendezvousNotFound.class, () -> rdvm.updateRendezvous(rdv));
    }

    @Test
    @Order(11)
    public void testUpdateRdvOnExistingCalendar() throws RendezvousNotFound {
        Rendezvous rdv = new RendezvousImpl("RDV4", "Quatrième rendez-vous", getDate(16, 30), 90);
        rdv = rdvm.addRendezvous(rdv);
        rdv.setTime(getDate(17, 0));
        rdv = rdvm.updateRendezvous(rdv);
        Calendar calendar = getDate(17, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Assertions.assertEquals(calendar, rdv.getTime());
        Assertions.assertEquals(2, rdvm.getRendezvousBetween(getDate(17, 0), getDate(17, 0)).size());
    }

    @Test
    @Order(12)
    public void testGetRdvBetween(){
        Assertions.assertEquals(3, rdvm.getRendezvousBetween(getDate(10, 0), getDate(12, 0)).size());
    }

    @Test
    @Order(13)
    public void testGetRdvBefore(){
        Assertions.assertEquals(3, rdvm.getRendezvousBefore(getDate(11, 0)).size());
    }

    @Test
    @Order(14)
    public void testGetRdvAfter(){
        Assertions.assertEquals(3, rdvm.getRendezvousAfter(getDate(11, 0)).size());
    }

    @Test
    @Order(15)
    public void testGetRdvToday(){
        Calendar calendar = getDate(17, 0);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Rendezvous rdv = new RendezvousImpl("RDV Demain", "Rendez vous demain 17 heures", calendar, 120);
        rdvm.addRendezvous(rdv);
        Assertions.assertEquals(6, rdvm.size());
        Assertions.assertEquals(5, rdvm.getRendezvousToday().size());
    }

    @Test
    @Order(16)
    public void testHasOverlap() throws RendezvousNotFound {
        Rendezvous rdv = new RendezvousImpl("RDV Temporaire", "", getDate(20, 30), 30);
        Rendezvous rdv2 = new RendezvousImpl("RDV Temporaire", "", getDate(21, 0), 30);
        rdv = rdvm.addRendezvous(rdv);
        rdv2 = rdvm.addRendezvous(rdv2);
        Assertions.assertTrue(rdvm.hasOverlap(getDate(15, 0), getDate(20, 0)));
        Assertions.assertFalse(rdvm.hasOverlap(getDate(20, 0), getDate(22, 0)));
        rdvm.removeRendezvous(rdv);
        rdvm.removeRendezvous(rdv2);
    }

    @Test
    @Order(17)
    public void testFindFreeTime(){
        Rendezvous rdv = new RendezvousImpl("RDV5", "Ciquième rendez vous", getDate(15, 0), 30);
        rdvm.addRendezvous(rdv);

        Calendar calendar = getDate(15, 30);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Assertions.assertEquals(calendar, rdvm.findFreeTime(60, getDate(14, 30), getDate(18, 30)));
    }

    @Test
    @Order(18)
    public void testFindNotExistingFreeTime(){
        Assertions.assertNull(rdvm.findFreeTime(30, getDate(10, 0), getDate(11, 30)));
    }

    @Test
    @Order(19)
    public void testFindFreeTimeOnEmptyPeriod(){
        Calendar calendar = getDate(7, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Assertions.assertEquals(calendar, rdvm.findFreeTime(60, getDate(7, 0), getDate(9, 0)));
    }

    @Test
    @Order(20)
    public void testFindRdvTitleEquals(){
        Rendezvous rdv = new RendezvousImpl("RDV2", "Extension du second rendez vous", getDate(12, 0), 30);
        Rendezvous rdv2 = new RendezvousImpl("RDV2", "Une autre extension du second rendez vous", getDate(20, 0), 60);
        rdvm.addRendezvous(rdv);
        rdvm.addRendezvous(rdv2);

        Assertions.assertEquals(2, rdvm.findRendezvousByTitleEqual("RDV2", getDate(8, 0), getDate(18, 0)).size());
        Assertions.assertEquals(3, rdvm.findRendezvousByTitleEqual("RDV2", getDate(8, 0), getDate(23, 0)).size());
        Assertions.assertEquals(1, rdvm.findRendezvousByTitleEqual("RDV5", getDate(10, 0), getDate(20, 0)).size());
    }

    @Test
    @Order(21)
    public void testFindRdvTitleALike(){
        Assertions.assertEquals(4, rdvm.findRendezvousByTitleALike("RDV", getDate(10, 0), getDate(15, 30)).size());
    }

}
