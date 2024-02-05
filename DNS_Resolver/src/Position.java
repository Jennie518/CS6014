public class Position {
    private int currentPosition;

    public Position() {
        currentPosition = 0;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void addCurrentPosition(int currentPosition) {
        this.currentPosition += currentPosition;
    }
}