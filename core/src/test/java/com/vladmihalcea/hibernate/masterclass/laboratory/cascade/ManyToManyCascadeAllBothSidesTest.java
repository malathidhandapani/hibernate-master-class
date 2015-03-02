package com.vladmihalcea.hibernate.masterclass.laboratory.cascade;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.Session;
import org.junit.Test;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


/**
 * OneToOneCascadeTest - Test to check @OneToOne Cascading
 *
 * @author Vlad Mihalcea
 */
public class ManyToManyCascadeAllBothSidesTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
                Book.class,
                Author.class
        };
    }

    public void addBooks() {
        doInTransaction(session -> {
            Author _John_Smith = new Author("John Smith");
            Author _Michelle_Diangello = new Author("Michelle Diangello");
            Author _Mark_Armstrong = new Author("Mark Armstrong");

            Book _Day_Dreaming = new Book("Day Dreaming");
            Book _Day_Dreaming_2nd = new Book("Day Dreaming, Second Edition");

            _John_Smith.addBook(_Day_Dreaming);
            _Michelle_Diangello.addBook(_Day_Dreaming);

            _John_Smith.addBook(_Day_Dreaming_2nd);
            _Michelle_Diangello.addBook(_Day_Dreaming_2nd);
            _Mark_Armstrong.addBook(_Day_Dreaming_2nd);

            session.persist(_John_Smith);
            session.persist(_Michelle_Diangello);
            session.persist(_Mark_Armstrong);
        });
    }

    @Test
    public void testCascadeTypeDelete() {
        LOGGER.info("Test CascadeType.DELETE");

        addBooks();

        doInTransaction(session -> {
            Author _Mark_Armstrong = getByName(session, "Mark Armstrong");
            session.delete(_Mark_Armstrong);
            Author _John_Smith = getByName(session, "John Smith");
            assertNull(_John_Smith);
        });

    }

    private Author getByName(Session session, String fullName) {
        @SuppressWarnings("unchecked")
        List<Author> authors = (List<Author>) session
                .createQuery("select a from Author a where a.fullName = :fullName")
                .setParameter("fullName", fullName)
                .list();
        return authors.isEmpty() ? null : authors.get(0);
    }



    @Entity(name = "Author")
    public static class Author {

        @Id
        @GeneratedValue(strategy=GenerationType.IDENTITY)
        private Long id;

        @Column(name = "full_name", nullable = false)
        private String fullName;

        @ManyToMany(mappedBy = "authors", cascade = CascadeType.ALL)
        private List<Book> books = new ArrayList<>();

        @Version
        private int version;

        public Author() {
        }

        public Author(String fullName) {
            this.fullName = fullName;
        }

        public Long getId() {
            return id;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public void addBook(Book book) {
            books.add(book);
            book.authors.add(this);
        }

        public void removeBook(Book book) {
            books.remove(book);
            book.authors.remove(this);
        }
    }

    @Entity(name = "Book")
    public static class Book {

        @Id
        @GeneratedValue(strategy=GenerationType.IDENTITY)
        private Long id;

        @Column(name = "title", nullable = false)
        private String title;

        @ManyToMany(cascade = CascadeType.ALL)
        @JoinTable(name = "Book_Author",
            joinColumns = {@JoinColumn(name = "book_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "author_id", referencedColumnName = "id")}
        )
        private List<Author> authors = new ArrayList<>();

        @Version
        private int version;

        public Book() {
        }

        public Book(String title) {
            this.title = title;
        }
    }



}
