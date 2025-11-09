package tourbooking.vietvivu.enumm;

import lombok.Getter;

@Getter
public enum ActionType {
    CREATE("Create"),
    BOOK_TOUR("Book Tour"),
    UPDATE("Update"),
    DELETE("Delete"),
    VIEW("View");

    private final String message;

    ActionType(String message) {
        this.message = message;
    }
}
