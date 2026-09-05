package com.facebook.UserService.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class FamilyMember {
    private String name;
    private String relationship;

    public FamilyMember() {
    }

    public FamilyMember(String name, String relationship) {
        this.name = name;
        this.relationship = relationship;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String relationship;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder relationship(String relationship) {
            this.relationship = relationship;
            return this;
        }

        public FamilyMember build() {
            return new FamilyMember(name, relationship);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }
}
