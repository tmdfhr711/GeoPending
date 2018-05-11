package com.plplim.david.geopending;

public class NotificationModel {
    public String to;

    public Notification notification = new Notification();
    public Data data = new Data();

    public static class Notification{
        public String title;
        public String text;
        public String sender;
        public String category;
        public String sound;
        public String priority;
    }
    public static class Data{
        public String title;
        public String text;
        public String sender;
        public String category;
        public String sound;
        public String priority;
    }
}
