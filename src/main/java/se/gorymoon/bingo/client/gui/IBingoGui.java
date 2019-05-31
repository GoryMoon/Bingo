package se.gorymoon.bingo.client.gui;

public interface IBingoGui {

    BingoGui getGui();

    default int getLeft() {
        return 0;
    }

    default int getTop() {
        return 0;
    }
}
