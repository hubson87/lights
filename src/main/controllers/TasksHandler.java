package main.controllers;

import javafx.concurrent.Task;

public class TasksHandler {
    public static void runTask(Task t) {
        Thread tt = new Thread(t);
        tt.setDaemon(true);
        tt.start();
    }
}
