package com.wordtrail.usecase;

public interface StudyInputBoundary {
    void show();
    void start();
    void known(String token, int index, boolean value);
    void advance(String token);
    void answer(String token, int choice, boolean assessment);
    void assess();
    void adopt(String token);
    void goal(int target);
    void replay(String id);
}
