package ru.itmo.lab8.common.data;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * Музыкальная группа
 */
public class MusicBand implements Comparable<MusicBand>, Serializable {
    private Integer id; //Поле не может быть null, Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private String name; //Поле не может быть null, Строка не может быть пустой
    private Coordinates coordinates; //Поле не может быть null
    private ZonedDateTime creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private Integer numberOfParticipants; //Поле может быть null, Значение поля должно быть больше 0
    private MusicGenre genre; //Поле может быть null
    private Label label; //Поле не может быть null
    public static Integer nextId = 1;
    private String owner;

    public MusicBand(String name, Coordinates coordinates, Integer numberOfParticipants, MusicGenre genre, Label label) {
        this.id = nextId;
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = ZonedDateTime.now();
        this.numberOfParticipants = numberOfParticipants;
        this.genre = genre;
        this.label = label;
        nextId++;
    }

    public MusicBand(Integer id, String name, Coordinates coordinates,
                     Integer numberOfParticipants, MusicGenre genre, Label label) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = ZonedDateTime.now();
        this.numberOfParticipants = numberOfParticipants;
        this.genre = genre;
        this.label = label;

        if (id >= nextId) {
            nextId = id + 1;
        }
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public Integer getNumberOfParticipants() {
        return numberOfParticipants;
    }

    public void setNumberOfParticipants(Integer numberOfParticipants) {
        this.numberOfParticipants = numberOfParticipants;
    }

    public ZonedDateTime getCreationDate() { return creationDate; }

    public MusicGenre getGenre() {
        return genre;
    }

    public void setGenre(MusicGenre genre) {
        this.genre = genre;
    }

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public void setId(Integer id) { this.id = id; }

    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public int compareTo(MusicBand other) {
        int value = this.name.compareTo(other.name);
        if (value == 0) {
            value = this.numberOfParticipants.compareTo((other.numberOfParticipants));
            if (value == 0) {
                value = this.id.compareTo(other.id);
            }
        }
        return value;
    }

    @Override
    public String toString() {
        return "MusicBand { "
                + "id = " + id
                + ", name = '" + name + '\''
                + ", coordinates = " + coordinates
                + ", creationDate = " + creationDate
                + ", numberOfParticipants = " + numberOfParticipants
                + ", genre = " + genre
                + ", label = " + label
                + " }";
    }
}
