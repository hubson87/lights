package main.model.enums;

//car going left, right up or down
public enum DirectionEnum {
    LEFT, RIGHT, UP, DOWN;

    public boolean isHorizontal() {
        return this == LEFT || this == RIGHT;
    }
}
