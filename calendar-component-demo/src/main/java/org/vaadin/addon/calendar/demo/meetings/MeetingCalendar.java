package org.vaadin.addon.calendar.demo.meetings;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.addon.calendar.Calendar;
import org.vaadin.addon.calendar.event.BasicItemProvider;
import org.vaadin.addon.calendar.handler.BasicDateClickHandler;
import org.vaadin.addon.calendar.handler.BasicItemMoveHandler;
import org.vaadin.addon.calendar.handler.BasicItemResizeHandler;
import org.vaadin.addon.calendar.ui.CalendarComponentEvents;

import java.util.*;
import java.util.logging.Logger;


public class MeetingCalendar extends CustomComponent {

    private final Random R = new Random(0);

    private MeetingDataProvider eventProvider;

    private Calendar<MeetingItem> calendar;

    public MeetingCalendar() {

        setId("meeting-meetings");
        setSizeFull();

        initCalendar();

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.addComponent(calendar);
        setCompositionRoot(layout);

    }

    public void setMeetings(Collection<Meeting> meetings) {

        // cleanup
        eventProvider.removeAllEvents();
        if (meetings == null || meetings.size() == 0) {
            return;
        }

        // erstelle Kalendereinträge neu
        for (Meeting boo : meetings) {
            MeetingItem i = new MeetingItem(boo);
            eventProvider.addItem(i);
        }
    }

    private void onCalendarRangeSelect(CalendarComponentEvents.RangeSelectEvent event) {

        Meeting meeting = new Meeting();

        meeting.setStart(event.getStart());
        meeting.setEnd(event.getEnd());
        meeting.setName("A Name");
        meeting.setDetails("A Detail");

        // Random state
        meeting.setState(R.nextInt(2) == 1 ? Meeting.State.planned : Meeting.State.confirmed);

        eventProvider.addItem(new MeetingItem(meeting));
	}

    private void onCalendarClick(CalendarComponentEvents.ItemClickEvent event) {

        MeetingItem item = (MeetingItem) event.getCalendarItem();

        final Meeting meeting = item.getMeeting();

        Notification.show(meeting.getName(), meeting.getDetails(), Type.HUMANIZED_MESSAGE);
    }

	private void updateMeeting(MeetingItem item, Date start, Date end) {
		item.setStart(start);
		item.setEnd(end);
	}

    private void initCalendar() {

        eventProvider = new MeetingDataProvider();

        calendar = new Calendar<>(eventProvider);

        calendar.addStyleName("meetings");
        calendar.setLocale(Locale.getDefault());
        calendar.setWidth(100.0f, Unit.PERCENTAGE);
        calendar.setHeight(100.0f, Unit.PERCENTAGE);
        calendar.setItemCaptionAsHtml(true);
        calendar.setResponsive(true);

        calendar.setContentMode(ContentMode.HTML);

        calendar.setFirstVisibleDayOfWeek(1);
        calendar.setLastVisibleDayOfWeek(7);

        addCalendarEventListeners();

        setupBlockedTimeSlots();
    }

    private void setupBlockedTimeSlots() {

        Set<Long> times = new HashSet<>();

        java.util.Calendar cal = (java.util.Calendar)calendar.getInternalCalendar().clone();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(java.util.Calendar.MINUTE);
        cal.clear(java.util.Calendar.SECOND);
        cal.clear(java.util.Calendar.MILLISECOND);

        GregorianCalendar bcal = new GregorianCalendar(UI.getCurrent().getLocale());

        bcal.clear();

        bcal.add(java.util.Calendar.HOUR, 10);
        times.add(bcal.getTimeInMillis());
        bcal.add(java.util.Calendar.MINUTE, 30);
        times.add(bcal.getTimeInMillis());
        bcal.add(java.util.Calendar.MINUTE, 30);
        times.add(bcal.getTimeInMillis());
        bcal.add(java.util.Calendar.MINUTE, 30);
        times.add(bcal.getTimeInMillis());
        bcal.add(java.util.Calendar.MINUTE, 30);
        times.add(bcal.getTimeInMillis());
        bcal.add(java.util.Calendar.MINUTE, 30);
        times.add(bcal.getTimeInMillis());

        calendar.setBlockedTimes(times);

        cal.add(java.util.Calendar.DAY_OF_WEEK, 1);

        bcal.clear();
        bcal.add(java.util.Calendar.HOUR, 4);
        calendar.setBlockedTime(cal.getTime(), bcal.getTimeInMillis());

        Logger.getLogger(getClass().getName()).info("TIME: " + cal.getTime());

        bcal.add(java.util.Calendar.MINUTE, 30);
        calendar.setBlockedTime(cal.getTime(), bcal.getTimeInMillis());

    }

    private void addCalendarEventListeners() {
//        calendar.setHandler(new ExtendedForwardHandler());
//        calendar.setHandler(new ExtendedBackwardHandler());
        calendar.setHandler(new ExtendedBasicItemMoveHandler());
        calendar.setHandler(new ExtendedItemResizeHandler());
        calendar.setHandler(new BasicDateClickHandler(false));
        calendar.setHandler(this::onCalendarClick);
        calendar.setHandler(this::onCalendarRangeSelect);
    }

    private final class ExtendedBasicItemMoveHandler extends BasicItemMoveHandler {

        @Override
        public void itemMove(CalendarComponentEvents.ItemMoveEvent event) {

            MeetingItem item = (MeetingItem) event.getCalendarItem();

            Meeting meeting = item.getMeeting();

            long length = item.getEnd().getTime() - item.getStart().getTime();

            Date newStart = event.getNewStart();

            Date newEnd = new Date(newStart.getTime() + length);

            if (meeting.isEditable()) {
                // TODO remove
                updateMeeting(item, newStart, newEnd);
            } else {
                updateMeeting(item, meeting.getStart(), meeting.getEnd());
            }
        }
    }

    private final class ExtendedItemResizeHandler extends BasicItemResizeHandler {

        @Override
        public void itemResize(CalendarComponentEvents.ItemResizeEvent event) {


            MeetingItem item = (MeetingItem) event.getCalendarItem();
            Meeting meeting = item.getMeeting();

            if (meeting.isEditable()) {
                // TODO remove
                updateMeeting(item, event.getNewStart(), event.getNewEnd());
            }
            else {
                updateMeeting(item, meeting.getStart(), meeting.getEnd());
            }
        }
    }

//    private final class ExtendedForwardHandler extends BasicForwardHandler {
//
//        @Override
//        protected void setDates(CalendarComponentEvents.ForwardEvent event, Date start, Date end) {
//
//            /*
//             * TODO Load entities from next week here
//             */
//
//            super.setDates(event, start, end);
//        }
//    }

//    private final class ExtendedBackwardHandler extends BasicBackwardHandler {
//
//        @Override
//        protected void setDates(CalendarComponentEvents.BackwardEvent event, Date start, Date end) {
//
//            /*
//             * TODO Load entities from prev week here
//             */
//
//            super.setDates(event, start, end);
//        }
//    }

    private final class MeetingDataProvider extends BasicItemProvider<MeetingItem> {

        void removeAllEvents() {
            this.itemList.clear();
            fireItemSetChanged();
        }
    }

}
