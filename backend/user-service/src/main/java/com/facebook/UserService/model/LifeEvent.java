package com.facebook.UserService.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class LifeEvent {
    private String date;
    private String event;

    public LifeEvent() {
    }

    public LifeEvent(String date, String event) {
        this.date = date;
        this.event = event;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String date;
        private String event;

        public Builder date(String date) {
            this.date = date;
            return this;
        }

        public Builder event(String event) {
            this.event = event;
            return this;
        }

        public LifeEvent build() {
            return new LifeEvent(date, event);
        }
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }
}
