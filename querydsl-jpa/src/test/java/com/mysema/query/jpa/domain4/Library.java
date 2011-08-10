package com.mysema.query.jpa.domain4;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA. User: nardonep Date: 09/06/11 Time: 13:36 To change
 * this template use File | Settings | File Templates.
 */

@Entity
@Table(name = "Library")
public class Library implements Serializable {

    private static final long serialVersionUID = 6360420736014459567L;

    private Long identity;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getIdentity() {
        return identity;
    }

    public void setIdentity(Long identity) {
        this.identity = identity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Library library = (Library) o;

        if (identity != null ? !identity.equals(library.identity)
                : library.identity != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return identity != null ? identity.hashCode() : 0;
    }
}